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
         MessageUtil.sendMessage(plugin, player, "general.usage");
         MessageUtil.playSound(plugin, player, "sounds.failure");
         return true;
      } else {
         String code = args[0];
         plugin.reloadCodesConfig();
         FileConfiguration codes = plugin.getCodesConfig();
         if (!codes.contains("Codes." + code)) {
            MessageUtil.sendMessage(plugin, player, "general.not-exist");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else if (plugin.getExpirationManager().isExpired(code)) {
            MessageUtil.sendMessage(plugin, player, "general.code-expired");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else if (!codes.getBoolean("Codes." + code + ".enabled", true)) {
            MessageUtil.sendMessage(plugin, player, "general.code-disabled");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else {
            java.util.List<xyz.redoxlabs.redeemcodes.commands.validators.RedeemValidator> validators = java.util.Arrays.asList(
                new xyz.redoxlabs.redeemcodes.commands.validators.PermissionValidator(),
                new xyz.redoxlabs.redeemcodes.commands.validators.BlacklistValidator(),
                new xyz.redoxlabs.redeemcodes.commands.validators.StockValidator(),
                new xyz.redoxlabs.redeemcodes.commands.validators.PlayerLimitValidator(),
                new xyz.redoxlabs.redeemcodes.commands.validators.IpLimitValidator(),
                new xyz.redoxlabs.redeemcodes.commands.validators.CooldownValidator()
            );

            for (xyz.redoxlabs.redeemcodes.commands.validators.RedeemValidator validator : validators) {
                if (!validator.validate(plugin, player, code, codes)) {
                    return true;
                }
            }

            RedeemDataManager dataManager = plugin.getRedeemDataManager();
            long currentTime = System.currentTimeMillis();

            new xyz.redoxlabs.redeemcodes.managers.RewardProcessor(plugin).processRewards(player, code, codes, false);
            
            dataManager.addGlobalUse(code);
            dataManager.addPlayerUse(code, player.getUniqueId());
            dataManager.setLastRedeemTime(code, player.getUniqueId(), currentTime);
            
            int ipLimit = codes.getInt("Codes." + code + ".redeem-limit.ip", 1);
            if (ipLimit != -1) {
                String currentIp = player.getAddress().getAddress().getHostAddress();
                dataManager.addIpUse(code, currentIp);
                dataManager.addPlayerIp(code, player.getUniqueId(), currentIp);
            }

            MessageUtil.sendMessage(plugin, player, "general.redeem-success");
            MessageUtil.playSound(plugin, player, "sounds.success");
            return true;
         }
      }
   }
}
