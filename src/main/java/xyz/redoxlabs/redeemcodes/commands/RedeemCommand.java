package xyz.redoxlabs.redeemcodes.commands;

import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bukkit.Bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RedeemCommand implements CommandExecutor {
   private final Main plugin;
   private final Random random = new Random();

   public RedeemCommand(Main plugin) {
      this.plugin = plugin;
   }

   private String parsePlaceholders(String input, Player player) {
      input = input.replace("%player%", player.getName());
      input = input.replace("%uuid%", player.getUniqueId().toString());
      input = input.replace("%displayname%", player.getDisplayName());
      input = input.replace("%world%", player.getWorld().getName());
      Pattern randomPattern = Pattern.compile("%random-(\\d+)-(\\d+)%");
      Matcher matcher = randomPattern.matcher(input);
      StringBuffer sb = new StringBuffer();

      while(matcher.find()) {
         int min = Integer.parseInt(matcher.group(1));
         int max = Integer.parseInt(matcher.group(2));
         int result = random.nextInt(max - min + 1) + min;
         matcher.appendReplacement(sb, String.valueOf(result));
      }

      matcher.appendTail(sb);
      String resultText = sb.toString();
      if (player != null && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
          resultText = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, resultText);
      }
      return resultText;
   }

   

   

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("§cOnly players can redeem codes!");
         return true;
      }
      Player player = (Player) sender;
      if (args.length != 1) {
         MessageUtil.sendMessage(plugin, player, "usage");
         MessageUtil.playSound(plugin, player, "sounds.failure");
         return true;
      } else {
         String code = args[0];
         plugin.reloadCodesConfig();
         FileConfiguration codes = plugin.getCodesConfig();
         if (!codes.contains("Codes." + code)) {
            MessageUtil.sendMessage(plugin, player, "not-exist");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else if (plugin.getExpirationManager().isExpired(code)) {
            MessageUtil.sendMessage(plugin, player, "code-expired");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else if (!codes.getBoolean("Codes." + code + ".enabled", true)) {
            MessageUtil.sendMessage(plugin, player, "code-disabled");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else {
            if (codes.getBoolean("Codes." + code + ".permisson.required", false)) {
               List<String> perms = codes.getStringList("Codes." + code + ".permisson.list");
               boolean hasPermission = perms.stream().anyMatch(player::hasPermission);
               if (!hasPermission) {
                  MessageUtil.sendMessage(plugin, player, "no-permission");
                  MessageUtil.playSound(plugin, player, "sounds.failure");
                  return true;
               }
            }

            String type = codes.getString("Codes." + code + ".Playerlist.Blacklist.Type", "ENABLED");
            List<String> blacklisted = codes.getStringList("Codes." + code + ".Playerlist.Blacklist.List");
            if ((!type.equalsIgnoreCase("ENABLED") || !blacklisted.contains(player.getName())) && (!type.equalsIgnoreCase("REVERSE") || blacklisted.contains(player.getName()))) {
               String limitType = codes.getString("Codes." + code + ".redeem-limit.Type", "PLAYER");
               int limitCount = codes.getInt("Codes." + code + ".redeem-limit.Count", -1);
               int cooldownMinutes = codes.getInt("Codes." + code + ".redeem-limit.Cooldown", 0);
               RedeemDataManager dataManager = plugin.getRedeemDataManager();
               int playerUses = dataManager.getPlayerUses(code, player.getUniqueId());
               long lastRedeemTime = dataManager.getLastRedeemTime(code, player.getUniqueId());
               long currentTime = System.currentTimeMillis();
               if (limitType.equalsIgnoreCase("PLAYER")) {
                  if (limitCount != -1 && playerUses >= limitCount) {
                     MessageUtil.sendMessage(plugin, player, "already-used");
                     MessageUtil.playSound(plugin, player, "sounds.failure");
                     return true;
                  }
               } else if (limitType.equalsIgnoreCase("CODE")) {
                  if (playerUses > 0) {
                     MessageUtil.sendMessage(plugin, player, "already-used");
                     MessageUtil.playSound(plugin, player, "sounds.failure");
                     return true;
                  }

                  if (limitCount <= 0) {
                     MessageUtil.sendMessage(plugin, player, "out-of-stock");
                     MessageUtil.playSound(plugin, player, "sounds.failure");
                     return true;
                  }
               }

               if (cooldownMinutes > 0) {
                  long cooldownMillis = (long)cooldownMinutes * 60L * 1000L;
                  if (currentTime - lastRedeemTime < cooldownMillis) {
                     long remaining = (cooldownMillis - (currentTime - lastRedeemTime)) / 1000L;
                     long minutes = remaining / 60L;
                     long seconds = remaining % 60L;
                     String formatted = minutes + "m " + seconds + "s";
                     String msg = codes.getString("Codes." + code + ".redeem-limit.Cooldown-message", "&cWait %Cooldown%");
                     player.sendMessage(plugin.color(plugin.getPrefix() + msg.replace("%Cooldown%", formatted)));
                     MessageUtil.playSound(plugin, player, "sounds.failure");
                     return true;
                  }
               }

               processRewards(player, code, codes);
               dataManager.addGlobalUse(code);
               dataManager.addPlayerUse(code, player.getUniqueId());
               dataManager.setLastRedeemTime(code, player.getUniqueId(), currentTime);
               if (limitType.equalsIgnoreCase("CODE")) {
                  codes.set("Codes." + code + ".redeem-limit.Count", limitCount - 1);
                  plugin.saveCodesConfig();
               }

               MessageUtil.sendMessage(plugin, player, "redeem-success");
               MessageUtil.playSound(plugin, player, "sounds.success");
               return true;
            } else {
               MessageUtil.sendMessage(plugin, player, "blacklisted");
               MessageUtil.playSound(plugin, player, "sounds.failure");
               return true;
            }
         }
      }
   }

   private void processRewards(Player player, String code, FileConfiguration codes) {
      String rewardPath = "Codes." + code + ".rewards";
      if (codes.contains(rewardPath) && codes.isList(rewardPath)) {
         for(String cmd : codes.getStringList(rewardPath)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsePlaceholders(cmd, player));
         }
      }

      String type = codes.getString(rewardPath + ".type", "ALL").toUpperCase();
      List<RewardEntry> allRewards = new ArrayList<>();
      if (codes.isConfigurationSection(rewardPath + ".commands")) {
         ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
         if (cmdSection != null) {
            for(String packName : cmdSection.getKeys(false)) {
               List<String> commands = cmdSection.getStringList(packName);
               int weight = 1;
               List<String> cleanCommands = new ArrayList<>();

               for(String line : commands) {
                  if (line.toLowerCase().startsWith("weight:")) {
                     try {
                        weight = Integer.parseInt(line.split(":")[1].trim());
                     } catch (Exception e) {
                     }
                  } else {
                     cleanCommands.add(line);
                  }
               }

               allRewards.add(new RewardEntry(RedeemCommand.RewardType.COMMAND_PACK, packName, weight, cleanCommands));
            }
         }
      }

      if (codes.contains(rewardPath + ".sacks")) {
         for(String entry : codes.getStringList(rewardPath + ".sacks")) {
            String[] parts = entry.split(":");
            String name = parts[0];
            int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
            allRewards.add(new RewardEntry(RedeemCommand.RewardType.SACK, name, weight, (List)null));
         }
      }

      if (codes.contains(rewardPath + ".premades")) {
         for(String entry : codes.getStringList(rewardPath + ".premades")) {
            String[] parts = entry.split(":");
            String name = parts[0];
            int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
            allRewards.add(new RewardEntry(RedeemCommand.RewardType.PREMADE, name, weight, (List)null));
         }
      }

      if (codes.contains(rewardPath + ".events")) {
         for(String entry : codes.getStringList(rewardPath + ".events")) {
            String[] parts = entry.split(":");
            String name = parts[0];
            int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
            allRewards.add(new RewardEntry(RedeemCommand.RewardType.EVENT, name, weight, (List)null));
         }
      }

      if (!allRewards.isEmpty()) {
         if (type.equals("ALL")) {
            for(RewardEntry reward : allRewards) {
               executeReward(player, reward);
            }
         } else if (type.equals("RANDOM")) {
            RewardEntry selected = (RewardEntry)allRewards.get(random.nextInt(allRewards.size()));
            executeReward(player, selected);
         } else if (type.equals("DRAW")) {
            int totalWeight = allRewards.stream().mapToInt((r) -> r.weight).sum();
            if (totalWeight <= 0) {
               executeReward(player, (RewardEntry)allRewards.get(random.nextInt(allRewards.size())));
               return;
            }

            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;

            for(RewardEntry reward : allRewards) {
               currentWeight += reward.weight;
               if (randomValue < currentWeight) {
                  executeReward(player, reward);
                  break;
               }
            }
         } else {
            for(RewardEntry reward : allRewards) {
               executeReward(player, reward);
            }
         }

      }
   }

   private void executeReward(Player player, RewardEntry reward) {
      switch (reward.type) {
         case COMMAND_PACK:
            if (reward.commands != null) {
               for(String cmd : reward.commands) {
                  String parsed = parsePlaceholders(cmd, player);
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
               }
            }
            break;
         case SACK:
            plugin.getSackManager().giveSack(player, reward.key);
            break;
         case PREMADE:
            for(String cmd : plugin.getPremadeManager().getPremadeCommands(reward.key)) {
               String parsed = parsePlaceholders(cmd, player);
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
            break;
         case EVENT:
            plugin.getEventManager().executeEvent(player, reward.key);
      }

   }

   private int parseInt(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException e) {
         return 1;
      }
   }

   private static enum RewardType {
      COMMAND_PACK,
      SACK,
      PREMADE,
      EVENT;


      private static RewardType[] $values() {
         return new RewardType[]{COMMAND_PACK, SACK, PREMADE, EVENT};
      }
   }

   private static class RewardEntry {
      RewardType type;
      String key;
      int weight;
      List<String> commands;

      public RewardEntry(RewardType type, String key, int weight, List<String> commands) {
         this.type = type;
         this.key = key;
         this.weight = weight;
         this.commands = commands;
      }
   }
}




