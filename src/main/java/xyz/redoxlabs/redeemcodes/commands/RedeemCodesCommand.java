package xyz.redoxlabs.redeemcodes.commands;

import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.guis.MainGUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class RedeemCodesCommand implements CommandExecutor, TabCompleter {
   private final Main plugin;

   public RedeemCodesCommand(Main plugin) {
      this.plugin = plugin;
   }

   

   private String getMessage(String key) {
      return plugin.color(plugin.getPrefix() + plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (sender instanceof Player player) {
         if (!player.isOp() && !player.hasPermission("redeemcodes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
         } else if (args.length < 1) {
            sendgui(player);
            return true;
         } else {
            String action = args[0].toLowerCase();
            FileConfiguration codes = plugin.getCodesConfig();
            switch (action) {
               case "reload":
                  plugin.reloadConfig();
                  plugin.reloadCodesConfig();
                  plugin.getPremadeManager().reloadPremades();
                  plugin.getEventManager().reloadEvents();
                  player.sendMessage(getMessage("reload-success"));
                  MessageUtil.playSound(plugin, player, "sounds.success");
                  break;
               case "gui":
                  sendgui(player);
                  break;
               case "create":
                  handleCreate(player, args, codes);
                  break;
               case "remove":
                  handleRemove(player, args, codes);
                  break;
               case "sack":
                  handleSack(player, args);
                  break;
               case "premade":
                  handlePremade(player, args);
                  break;
               case "event":
                  handleEvent(player, args);
                  break;
               case "reward":
                  handleReward(player, args);
                  break;
               case "help":
                  sendHelp(player, args);
                  break;
               case "list":
                  sendCodeList(player);
                  break;
               case "version":
                  player.sendMessage(plugin.color("&bRedeemCodes Version: &f" + plugin.getDescription().getVersion()));
                  break;
               case "show":
                  showCodeDetails(player, args);
                  break;
               case "redeemed":
                  sendRedeemedList(player, args);
                  break;
               case "review":
                  handleReview(player, args);
                  break;
               default:
                  player.sendMessage(getMessage("unknown-action").replace("%action%", action));
                  MessageUtil.playSound(plugin, player, "sounds.failure");
            }

            return true;
         }
      } else {
         sender.sendMessage("§cOnly players can use admin commands.");
         return true;
      }
   }

   private void handleCreate(Player player, String[] args, FileConfiguration codes) {
      if (args.length < 2) {
         player.sendMessage("§cUsage: /rc create <code>");
      } else {
         String codeCreate = args[1];
         if (codes.contains("Codes." + codeCreate)) {
            player.sendMessage(getMessage("code-exists"));
            MessageUtil.playSound(plugin, player, "sounds.failure");
         } else {
            String path = "Codes." + codeCreate;
            codes.set(path + ".enabled", true);
            codes.set(path + ".permisson.required", false);
            codes.set(path + ".permisson.list", new ArrayList<>());
            codes.set(path + ".redeem-limit.Type", "PLAYER");
            codes.set(path + ".redeem-limit.Count", 1);
            codes.set(path + ".redeem-limit.Cooldown", 0);
            codes.set(path + ".Playerlist.Used", new ArrayList<>());
            codes.set(path + ".Playerlist.Blacklist.Type", "ENABLED");
            codes.set(path + ".Playerlist.Blacklist.List", new ArrayList<>());
            codes.set(path + ".rewards.type", "ALL");
            codes.createSection(path + ".rewards.commands");
            codes.set(path + ".rewards.sacks", new ArrayList<>());
            codes.set(path + ".rewards.premades", new ArrayList<>());
            codes.set(path + ".rewards.events", new ArrayList<>());
            codes.set(path + ".rewards.list", new ArrayList<>());
            plugin.saveCodesConfig();
            player.sendMessage(getMessage("code-created").replace("%code%", codeCreate));
            player.sendMessage(plugin.color("&e[*] In-game editor is now available. Use command /rc"));
            MessageUtil.playSound(plugin, player, "sounds.success");
         }

      }
   }

   private void handleRemove(Player player, String[] args, FileConfiguration codes) {
      if (args.length < 2) {
         player.sendMessage("§cUsage: /rc remove <code>");
      } else {
         String codeRemove = args[1];
         if (!codes.contains("Codes." + codeRemove)) {
            player.sendMessage("§cThis code doesn't exist.");
         } else {
            codes.set("Codes." + codeRemove, (Object)null);
            plugin.saveCodesConfig();
            player.sendMessage(plugin.color("&aCode removed: &c" + codeRemove));
            MessageUtil.playSound(plugin, player, "sounds.success");
         }
      }
   }

   private void handleSack(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage(plugin.color("&cUsage: /rc sack <create|remove|edit|give> <name>"));
      } else {
         String sackAction = args[1].toLowerCase();
         String sackName = args[2];
         switch (sackAction) {
            case "create":
               if (plugin.getSackManager().createSack(sackName)) {
                  player.sendMessage(plugin.color("&aSack '&e" + sackName + "&a' created successfully!"));
                  MessageUtil.playSound(plugin, player, "sounds.success");
               } else {
                  player.sendMessage(plugin.color("&cSack '&e" + sackName + "&c' already exists."));
               }
               break;
            case "remove":
            case "delete":
               if (plugin.getSackManager().deleteSack(sackName)) {
                  player.sendMessage(plugin.color("&aSack removed."));
                  MessageUtil.playSound(plugin, player, "sounds.success");
               } else {
                  player.sendMessage(plugin.color("&cSack not found."));
               }
               break;
            case "edit":
            case "open":
               plugin.getSackManager().openEditGUI(player, sackName);
               break;
            case "give":
               Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : player;
               if (target == null) {
                  player.sendMessage(plugin.color("&cPlayer not found."));
                  return;
               }

               plugin.getSackManager().giveSack(target, sackName);
               player.sendMessage(plugin.color("&aGave sack to " + target.getName()));
               MessageUtil.playSound(plugin, player, "sounds.success");
         }

      }
   }

   private void handlePremade(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage(plugin.color("&cUsage: /rc premade <add|remove|view> <name> [command/index]"));
      } else {
         String sub = args[1].toLowerCase();
         String name = args[2];
         if (sub.equals("add")) {
            if (args.length < 4) {
               player.sendMessage(plugin.color("&cUsage: /rc premade add " + name + " <command line>"));
               return;
            }

            String command = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 3, args.length));
            plugin.getPremadeManager().addCommand(name, command);
            player.sendMessage(plugin.color("&aAdded command to premade '&e" + name + "&a'."));
            MessageUtil.playSound(plugin, player, "sounds.success");
         } else if (sub.equals("remove")) {
            if (args.length < 4) {
               player.sendMessage(plugin.color("&cUsage: /rc premade remove " + name + " <index>"));
               return;
            }

            try {
               int index = Integer.parseInt(args[3]);
               if (plugin.getPremadeManager().removeCommand(name, index)) {
                  player.sendMessage(plugin.color("&aRemoved command at index " + index + " from premade '&e" + name + "&a'."));
                  MessageUtil.playSound(plugin, player, "sounds.success");
               } else {
                  player.sendMessage(plugin.color("&cIndex out of bounds."));
               }
            } catch (NumberFormatException e) {
               player.sendMessage(plugin.color("&cInvalid index."));
            }
         } else if (sub.equals("view")) {
            List<String> cmds = plugin.getPremadeManager().getPremadeCommands(name);
            if (cmds.isEmpty()) {
               player.sendMessage(plugin.color("&cPremade '&e" + name + "&c' not found or empty."));
               return;
            }

            player.sendMessage(plugin.color("&d--- Premade: &e" + name + " &d---"));

            for(int i = 0; i < cmds.size(); ++i) {
               player.sendMessage(plugin.color("&b" + i + ": &7" + (String)cmds.get(i)));
            }
         }

      }
   }

   private void handleEvent(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage(plugin.color("&cUsage: /rc event <create|remove|add|play> <name> [type]"));
      } else {
         String sub = args[1].toLowerCase();
         String name = args[2];
         if (sub.equals("create")) {
            if (plugin.getEventManager().createEvent(name)) {
               player.sendMessage(plugin.color("&aEvent '&e" + name + "&a' created."));
               MessageUtil.playSound(plugin, player, "sounds.success");
            } else {
               player.sendMessage(plugin.color("&cEvent already exists."));
            }
         } else if (sub.equals("remove")) {
            if (plugin.getEventManager().deleteEvent(name)) {
               player.sendMessage(plugin.color("&aEvent '&e" + name + "&a' removed."));
               MessageUtil.playSound(plugin, player, "sounds.success");
            } else {
               player.sendMessage(plugin.color("&cEvent not found."));
            }
         } else if (sub.equals("play")) {
            if (!plugin.getEventManager().eventExists(name)) {
               player.sendMessage(plugin.color("&cEvent '&e" + name + "&c' does not exist."));
               return;
            }

            plugin.getEventManager().executeEvent(player, name);
            player.sendMessage(plugin.color("&aPlaying event '&e" + name + "&a'."));
         } else if (sub.equals("add")) {
            if (args.length < 4) {
               player.sendMessage(plugin.color("&cUsage: /rc event add " + name + " <firework|command|sound>"));
               return;
            }

            if (!plugin.getEventManager().eventExists(name)) {
               player.sendMessage(plugin.color("&cEvent '&e" + name + "&c' does not exist. Create it first."));
               return;
            }

            String type = args[3].toLowerCase();
            if (type.equals("firework")) {
               plugin.getEventGUI().openFireworkEditor(player, name);
            } else if (type.equals("sound")) {
               plugin.getEventGUI().openSoundList(player, name);
            } else if (type.equals("command")) {
               if (args.length < 5) {
                  player.sendMessage(plugin.color("&cUsage: /rc event add " + name + " command <console command>"));
                  return;
               }

               String cmdLine = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 4, args.length));
               FileConfiguration config = plugin.getEventManager().getEventConfig(name);
               List<String> cmds = config.getStringList("commands");
               cmds.add(cmdLine);
               config.set("commands", cmds);
               plugin.getEventManager().saveEvent(name);
               player.sendMessage(plugin.color("&aCommand added to event '&e" + name + "&a'."));
               MessageUtil.playSound(plugin, player, "sounds.success");
            }
         }

      }
   }

   private void handleReward(Player player, String[] args) {
      if (args.length < 3) {
         player.sendMessage(plugin.color("&cUsage: /rc reward <codename> <add|remove|view|settype|setevent> ..."));
      } else {
         String codeName = args[1];
         String sub = args[2].toLowerCase();
         FileConfiguration codes = plugin.getCodesConfig();
         if (!codes.contains("Codes." + codeName)) {
            player.sendMessage(getMessage("not-exist"));
         } else {
            String rewardPath = "Codes." + codeName + ".rewards";
            switch (sub) {
               case "add":
                  if (args.length < 5) {
                     player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " add <command|sack|premade> <name> [cmd]"));
                     return;
                  }

                  String type = args[3].toLowerCase();
                  String name = args[4];
                  if (type.equals("command")) {
                     if (args.length < 6) {
                        player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " add command <packname> <console command>"));
                        return;
                     }

                     String cmd = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 5, args.length));
                     List<String> packCmds = codes.getStringList(rewardPath + ".commands." + name);
                     packCmds.add(cmd);
                     codes.set(rewardPath + ".commands." + name, packCmds);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aAdded command to pack '&e" + name + "&a' in code '&e" + codeName + "&a'."));
                     MessageUtil.playSound(plugin, player, "sounds.success");
                  } else if (type.equals("sack")) {
                     if (!plugin.getSackManager().sackExists(name)) {
                        player.sendMessage(plugin.color("&cSack '&e" + name + "&c' does not exist in sacks folder."));
                        return;
                     }

                     List<String> sackList = codes.getStringList(rewardPath + ".sacks");
                     boolean exists = sackList.stream().anyMatch((s) -> s.split(":")[0].equals(name));
                     if (exists) {
                        player.sendMessage(plugin.color("&cSack '&e" + name + "&c' is already added to rewards."));
                        return;
                     }

                     sackList.add(name + ":1");
                     codes.set(rewardPath + ".sacks", sackList);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aAdded sack '&e" + name + "&a' to code '&e" + codeName + "&a'."));
                     MessageUtil.playSound(plugin, player, "sounds.success");
                  } else if (type.equals("premade")) {
                     if (!plugin.getPremadeManager().premadeExists(name)) {
                        player.sendMessage(plugin.color("&cPremade '&e" + name + "&c' does not exist in premades.yml."));
                        return;
                     }

                     List<String> premadeList = codes.getStringList(rewardPath + ".premades");
                     boolean exists = premadeList.stream().anyMatch((p) -> p.split(":")[0].equals(name));
                     if (exists) {
                        player.sendMessage(plugin.color("&cPremade '&e" + name + "&c' is already added to rewards."));
                        return;
                     }

                     premadeList.add(name + ":1");
                     codes.set(rewardPath + ".premades", premadeList);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aAdded premade '&e" + name + "&a' to code '&e" + codeName + "&a'."));
                     MessageUtil.playSound(plugin, player, "sounds.success");
                  } else {
                     player.sendMessage(plugin.color("&cUnknown type. Use command, sack, or premade."));
                  }
                  break;
               case "remove":
                  if (args.length < 5) {
                     player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " remove <command|sack|premade> <name> [id]"));
                     return;
                  }

                  String removeType = args[3].toLowerCase();
                  String targetName = args[4];
                  if (removeType.equals("command")) {
                     if (args.length < 6) {
                        player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " remove command <packname> <index>"));
                        return;
                     }

                     String packPath = rewardPath + ".commands." + targetName;
                     if (!codes.contains(packPath)) {
                        player.sendMessage(plugin.color("&cCommand pack '&e" + targetName + "&c' does not exist in this code."));
                        return;
                     }

                     List<String> cmds = codes.getStringList(packPath);

                     try {
                        int index = Integer.parseInt(args[5]);
                        if (index >= 0 && index < cmds.size()) {
                           cmds.remove(index);
                           if (cmds.isEmpty()) {
                              codes.set(packPath, (Object)null);
                              player.sendMessage(plugin.color("&aRemoved empty command pack '&e" + targetName + "&a'."));
                           } else {
                              codes.set(packPath, cmds);
                              player.sendMessage(plugin.color("&aRemoved command at index " + index + " from pack '&e" + targetName + "&a'."));
                           }

                           plugin.saveCodesConfig();
                           MessageUtil.playSound(plugin, player, "sounds.success");
                        } else {
                           player.sendMessage(plugin.color("&cIndex out of bounds."));
                        }
                     } catch (NumberFormatException e) {
                        player.sendMessage(plugin.color("&cInvalid index."));
                     }
                  } else if (removeType.equals("sack")) {
                     List<String> sackList = codes.getStringList(rewardPath + ".sacks");
                     boolean removed = sackList.removeIf((s) -> s.split(":")[0].equals(targetName));
                     if (removed) {
                        codes.set(rewardPath + ".sacks", sackList);
                        plugin.saveCodesConfig();
                        player.sendMessage(plugin.color("&aRemoved sack '&e" + targetName + "&a' from code rewards."));
                        MessageUtil.playSound(plugin, player, "sounds.success");
                     } else {
                        player.sendMessage(plugin.color("&cSack '&e" + targetName + "&c' not found in rewards."));
                     }
                  } else if (removeType.equals("premade")) {
                     List<String> premadeList = codes.getStringList(rewardPath + ".premades");
                     boolean removed = premadeList.removeIf((p) -> p.split(":")[0].equals(targetName));
                     if (removed) {
                        codes.set(rewardPath + ".premades", premadeList);
                        plugin.saveCodesConfig();
                        player.sendMessage(plugin.color("&aRemoved premade '&e" + targetName + "&a' from code rewards."));
                        MessageUtil.playSound(plugin, player, "sounds.success");
                     } else {
                        player.sendMessage(plugin.color("&cPremade '&e" + targetName + "&c' not found in rewards."));
                     }
                  }
                  break;
               case "view":
                  int page = 1;
                  if (args.length > 3) {
                     try {
                        page = Integer.parseInt(args[3]);
                     } catch (NumberFormatException e) {
                     }
                  }

                  sendRewardsList(player, codeName, page);
                  break;
               case "settype":
                  if (args.length < 4) {
                     player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " settype <RANDOM|ALL|DRAW>"));
                     return;
                  }

                  String newType = args[3].toUpperCase();
                  if (Arrays.asList("RANDOM", "ALL", "DRAW").contains(newType)) {
                     codes.set(rewardPath + ".type", newType);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aReward type for '&e" + codeName + "&a' set to &e" + newType + "&a."));
                     MessageUtil.playSound(plugin, player, "sounds.success");
                  } else {
                     player.sendMessage(plugin.color("&cInvalid type. Use RANDOM, ALL, or DRAW."));
                  }
                  break;
               case "setevent":
                  if (args.length < 4) {
                     player.sendMessage(plugin.color("&cUsage: /rc reward " + codeName + " setevent <eventname|none>"));
                     return;
                  }

                  String eventTarget = args[3];
                  List<String> events = codes.getStringList(rewardPath + ".events");
                  if (eventTarget.equalsIgnoreCase("none")) {
                     events.clear();
                     codes.set(rewardPath + ".events", events);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aCleared all events from code '&e" + codeName + "&a'."));
                  } else {
                     if (!plugin.getEventManager().eventExists(eventTarget)) {
                        player.sendMessage(plugin.color("&cEvent '&e" + eventTarget + "&c' does not exist."));
                        return;
                     }

                     events.clear();
                     events.add(eventTarget);
                     codes.set(rewardPath + ".events", events);
                     plugin.saveCodesConfig();
                     player.sendMessage(plugin.color("&aSet event for code '&e" + codeName + "&a' to '&e" + eventTarget + "&a'."));
                  }

                  MessageUtil.playSound(plugin, player, "sounds.success");
                  break;
               default:
                  player.sendMessage(plugin.color("&cUnknown reward action. Use add, remove, view, settype, setevent."));
            }

         }
      }
   }

   private void sendRewardsList(Player player, String codeName, int page) {
      FileConfiguration codes = plugin.getCodesConfig();
      String rewardPath = "Codes." + codeName + ".rewards";
      List<TextComponent> lines = new ArrayList<>();
      lines.add(new TextComponent(plugin.color("&d--- Rewards for &b" + codeName + " &7(Page " + page + ") &d---")));
      lines.add(new TextComponent(plugin.color("&7Distribution Type: &e" + codes.getString(rewardPath + ".type", "RANDOM"))));
      ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
      if (cmdSection != null) {
         for(String pack : cmdSection.getKeys(false)) {
            List<String> cmds = cmdSection.getStringList(pack);
            lines.add(new TextComponent(plugin.color("&e[Pack: " + pack + "]")));

            for(int i = 0; i < cmds.size(); ++i) {
               TextComponent line = new TextComponent(plugin.color("  &7" + i + ": " + (String)cmds.get(i) + " "));
               TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
               removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, (new ComponentBuilder("Click to remove this command")).create()));
               removeBtn.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove command " + pack + " " + i));
               line.addExtra(removeBtn);
               lines.add(line);
            }
         }
      }

      List<String> sacks = codes.getStringList(rewardPath + ".sacks");
      if (!sacks.isEmpty()) {
         lines.add(new TextComponent(plugin.color("&e[Sacks]")));

         for(String s : sacks) {
            String name = s.split(":")[0];
            TextComponent line = new TextComponent(plugin.color("  &7- " + s + " "));
            TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
            removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, (new ComponentBuilder("Click to remove this sack")).create()));
            removeBtn.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove sack " + name));
            line.addExtra(removeBtn);
            lines.add(line);
         }
      }

      List<String> premades = codes.getStringList(rewardPath + ".premades");
      if (!premades.isEmpty()) {
         lines.add(new TextComponent(plugin.color("&e[Premades]")));

         for(String p : premades) {
            String name = p.split(":")[0];
            TextComponent line = new TextComponent(plugin.color("  &7- " + p + " "));
            TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
            removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, (new ComponentBuilder("Click to remove this premade")).create()));
            removeBtn.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove premade " + name));
            line.addExtra(removeBtn);
            lines.add(line);
         }
      }

      List<String> events = codes.getStringList(rewardPath + ".events");
      if (!events.isEmpty()) {
         lines.add(new TextComponent(plugin.color("&e[Events]")));

         for(String e : events) {
            TextComponent line = new TextComponent(plugin.color("  &7- " + e + " "));
            lines.add(line);
         }
      }

      int perPage = 10;
      int total = lines.size();
      int maxPage = (int)Math.ceil((double)total / (double)perPage);
      if (page < 1) {
         page = 1;
      }

      if (page > maxPage) {
         page = maxPage;
      }

      int start = (page - 1) * perPage;
      int end = Math.min(start + perPage, total);

      for(int i = start; i < end; ++i) {
         player.spigot().sendMessage((BaseComponent)lines.get(i));
      }

      player.sendMessage(plugin.color("&e&m                                                                       "));
   }

   private void sendRewardsView(Player player, String codeName, FileConfiguration codes, String rewardPath) {
      player.sendMessage(plugin.color("&d--- Rewards for &b" + codeName + " &d---"));
      String type = codes.getString(rewardPath + ".type", "RANDOM");
      player.sendMessage(plugin.color("&7Type: &e" + type));
      ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
      if (cmdSection != null) {
         player.sendMessage(plugin.color("&7Command Packs:"));

         for(String pack : cmdSection.getKeys(false)) {
            player.sendMessage(plugin.color("  &e" + pack + " &7(" + cmdSection.getStringList(pack).size() + ")"));
         }
      }

      List<String> sacks = codes.getStringList(rewardPath + ".sacks");
      if (!sacks.isEmpty()) {
         player.sendMessage(plugin.color("&7Sacks:"));

         for(String s : sacks) {
            player.sendMessage(plugin.color("  &b- " + s));
         }
      }

      List<String> premades = codes.getStringList(rewardPath + ".premades");
      if (!premades.isEmpty()) {
         player.sendMessage(plugin.color("&7Premades:"));

         for(String p : premades) {
            player.sendMessage(plugin.color("  &b- " + p));
         }
      }

      List<String> events = codes.getStringList(rewardPath + ".events");
      if (!events.isEmpty()) {
         player.sendMessage(plugin.color("&7Events:"));

         for(String e : events) {
            player.sendMessage(plugin.color("  &b- " + e));
         }
      }

      if (codes.isList(rewardPath)) {
         List<String> oldRewards = codes.getStringList(rewardPath);
         if (!oldRewards.isEmpty()) {
            player.sendMessage(plugin.color("&7Legacy Rewards: &7(" + oldRewards.size() + ")"));
         }
      }

   }

   private void handleReview(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage("§cUsage: /rc review <message>");
      } else {
         if (plugin.getConfig().getBoolean("send-review.review-message", true)) {
            String message = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 1, args.length));
            String playerName = player.getName();
            String webhookUrl = "https://discord.com/api/webhooks/1415900269459542048/FNd7DLkVp2x2zdTs2b7f0UwbHA96MgeM0MpLne4Wst1k818HaarInJaSz2oNO_pcg6VJ";
            String jsonPayload = "{\"embeds\": [{\"title\": \"New Plugin Review\",\"color\": 15844367,\"fields\": [  {\"name\": \"Player\",\"value\": \"" + playerName + "\",\"inline\": true},  {\"name\": \"Message\",\"value\": \"" + message.replace("\"", "\\\"") + "\",\"inline\": false}],\"footer\": {\"text\": \"RedeemCodes Review System\"}}]}";
            plugin.sendToWebhook(webhookUrl, jsonPayload);
            player.sendMessage(plugin.color("&aThank you! Your review has been sent."));
            MessageUtil.playSound(plugin, player, "sounds.success");
         } else {
            player.sendMessage(plugin.color("&cThe server owner has disabled this feature."));
         }

      }
   }

   private void sendgui(Player player) {
      if (!player.isOp() && !player.hasPermission("redeemcodes.admin")) {
         player.sendMessage(getMessage("no-permission"));
         MessageUtil.playSound(plugin, player, "sounds.failure");
      } else {
         MainGUI.open(player);
         MessageUtil.playSound(plugin, player, "sounds.success");
      }

   }

   private void showCodeDetails(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage("§cUsage: /rc show <code>");
      } else {
         String code = args[1];
         FileConfiguration codes = plugin.getCodesConfig();
         if (!codes.contains("Codes." + code)) {
            player.sendMessage("§cThis code doesn't exist.");
         } else {
            player.sendMessage(plugin.color("&d---- &bCode Details: &f" + code + " &d----"));
            player.sendMessage(plugin.color("&7Enabled: " + (codes.getBoolean("Codes." + code + ".enabled", true) ? "&aTrue" : "&cFalse")));
            sendRewardsView(player, code, codes, "Codes." + code + ".rewards");
         }
      }
   }

   private void sendHelp(Player player, String[] args) {
      List<String> helpCommands = Arrays.asList("&b/rc create <code> &7- Create a new code", "&b/rc remove <code> &7- Remove a code", "&b/rc reward <code> <action> &7- Manage rewards", "&b/rc sack create <name> &7- Create sack", "&b/rc sack edit <name> &7- Edit sack", "&b/rc sack give <name> &7- Give sack", "&b/rc event create <name> &7- Create event", "&b/rc event add <name> <type> &7- Add actions to event", "&b/rc event play <name> &7- Play/Test event", "&b/rc premade add <name> <cmd> &7- Manage premades", "&b/rc show <code> &7- Show details", "&b/rc list &7- List codes", "&b/rc redeemed <code> &7- List usage", "&b/rc reload &7- Reload config", "&b/rc gui &7- Open GUI");
      int commandsPerPage = 6;
      int totalPages = (int)Math.ceil((double)helpCommands.size() / (double)commandsPerPage);
      int page = 1;
      if (args.length > 1) {
         try {
            page = Integer.parseInt(args[1]);
         } catch (NumberFormatException e) {
         }
      }

      if (page < 1) {
         page = 1;
      }

      if (page > totalPages) {
         page = totalPages;
      }

      player.sendMessage(plugin.color("&e&m                                                                       "));
      player.sendMessage(plugin.color(" &d&lRedeemCodes Help &r&d- Page " + page + "/" + totalPages));
      int startIndex = (page - 1) * commandsPerPage;

      for(int i = 0; i < commandsPerPage; ++i) {
         int commandIndex = startIndex + i;
         if (commandIndex >= helpCommands.size()) {
            break;
         }

         player.sendMessage(plugin.color((String)helpCommands.get(commandIndex)));
      }

      player.sendMessage(plugin.color("&e&m                                                                       "));
   }

   private void sendCodeList(Player player) {
      FileConfiguration codes = plugin.getCodesConfig();
      Set<String> codeNames = codes.getConfigurationSection("Codes").getKeys(false);
      player.sendMessage(plugin.color("&d---- &bList of Existing Codes &d----"));

      for(String code : codeNames) {
         boolean enabled = codes.getBoolean("Codes." + code + ".enabled", true);
         TextComponent component = new TextComponent(plugin.color((enabled ? "&a" : "&c") + "➤ " + code));
         component.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/rc show " + code));
         component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, (new ComponentBuilder("Click details")).create()));
         player.spigot().sendMessage(component);
      }

   }

   private void sendRedeemedList(Player player, String[] args) {
      if (args.length < 2) {
         player.sendMessage("§cUsage: /rc redeemed <code> [page]");
      } else {
         String codeName = args[1];
         List<String> redeemedPlayersUuids = plugin.getRedeemDataManager().getRedeemedPlayers(codeName);
         if (redeemedPlayersUuids.isEmpty()) {
            player.sendMessage(plugin.color("&cNo one has redeemed the code '&e" + codeName + "&c' yet."));
         } else {
            player.sendMessage(plugin.color("&dRedeemed count: " + redeemedPlayersUuids.size()));
            int limit = Math.min(redeemedPlayersUuids.size(), 10);

            for(int i = 0; i < limit; ++i) {
               String uuid = (String)redeemedPlayersUuids.get(i);

               try {
                  String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                  player.sendMessage(plugin.color("&7- " + (name != null ? name : uuid)));
               } catch (Exception e) {
                  player.sendMessage(plugin.color("&7- " + uuid));
               }
            }

            if (redeemedPlayersUuids.size() > 10) {
               player.sendMessage(plugin.color("&7... and " + (redeemedPlayersUuids.size() - 10) + " more."));
            }

         }
      }
   }

   private int parseInt(String str, int def) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException e) {
         return def;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "create", "remove", "sack", "premade", "event", "reward", "help", "list", "show", "redeemed", "gui", "review"), completions);
         return completions;
      } else {
         String action = args[0].toLowerCase();
         Set<String> codes = plugin.getCodesConfig().isConfigurationSection("Codes") ? plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false) : Collections.emptySet();
         if (action.equals("premade")) {
            if (args.length == 2) {
               StringUtil.copyPartialMatches(args[1], Arrays.asList("add", "remove", "view"), completions);
            }

            if (args.length == 3) {
               StringUtil.copyPartialMatches(args[2], plugin.getPremadeManager().getPremadeNames(), completions);
            }
         } else if (action.equals("event")) {
            if (args.length == 2) {
               StringUtil.copyPartialMatches(args[1], Arrays.asList("create", "remove", "add", "play"), completions);
            }

            if (args.length == 3) {
               StringUtil.copyPartialMatches(args[2], new ArrayList(plugin.getEventManager().getEventNames()), completions);
            }

            if (args.length == 4 && args[1].equals("add")) {
               StringUtil.copyPartialMatches(args[3], Arrays.asList("firework", "sound", "command"), completions);
            }
         } else if (action.equals("reward")) {
            if (args.length == 2) {
               StringUtil.copyPartialMatches(args[1], codes, completions);
            }

            if (args.length == 3) {
               StringUtil.copyPartialMatches(args[2], Arrays.asList("add", "remove", "view", "settype", "setevent"), completions);
            }

            if (args.length == 4) {
               String sub = args[2].toLowerCase();
               if (sub.equals("add")) {
                  StringUtil.copyPartialMatches(args[3], Arrays.asList("command", "sack", "premade"), completions);
               } else if (sub.equals("remove")) {
                  StringUtil.copyPartialMatches(args[3], Arrays.asList("command", "sack", "premade"), completions);
               } else if (sub.equals("settype")) {
                  StringUtil.copyPartialMatches(args[3], Arrays.asList("RANDOM", "ALL", "DRAW"), completions);
               } else if (sub.equals("setevent")) {
                  List<String> events = new ArrayList(plugin.getEventManager().getEventNames());
                  events.add("none");
                  StringUtil.copyPartialMatches(args[3], events, completions);
               }
            }

            if (args.length == 5) {
               String sub = args[2].toLowerCase();
               String type = args[3].toLowerCase();
               String codeName = args[1];
               if (sub.equals("add")) {
                  if (type.equals("sack")) {
                     StringUtil.copyPartialMatches(args[4], Arrays.asList(plugin.getSackManager().getSackNames()), completions);
                  } else if (type.equals("premade")) {
                     StringUtil.copyPartialMatches(args[4], plugin.getPremadeManager().getPremadeNames(), completions);
                  } else if (type.equals("command")) {
                     FileConfiguration config = plugin.getCodesConfig();
                     String path = "Codes." + codeName + ".rewards.commands";
                     if (config.isConfigurationSection(path)) {
                        Set<String> packs = config.getConfigurationSection(path).getKeys(false);
                        StringUtil.copyPartialMatches(args[4], packs, completions);
                     }
                  }
               } else if (sub.equals("remove")) {
                  if (type.equals("sack")) {
                     StringUtil.copyPartialMatches(args[4], Arrays.asList(plugin.getSackManager().getSackNames()), completions);
                  } else if (type.equals("premade")) {
                     StringUtil.copyPartialMatches(args[4], plugin.getPremadeManager().getPremadeNames(), completions);
                  } else if (type.equals("command")) {
                     FileConfiguration config = plugin.getCodesConfig();
                     String path = "Codes." + codeName + ".rewards.commands";
                     if (config.isConfigurationSection(path)) {
                        Set<String> packs = config.getConfigurationSection(path).getKeys(false);
                        StringUtil.copyPartialMatches(args[4], packs, completions);
                     }
                  }
               }
            }
         } else if (Arrays.asList("show", "remove", "redeemed").contains(action)) {
            if (args.length == 2) {
               StringUtil.copyPartialMatches(args[1], codes, completions);
            }
         } else if (action.equals("sack")) {
            if (args.length == 2) {
               StringUtil.copyPartialMatches(args[1], Arrays.asList("create", "edit", "give", "remove"), completions);
            }

            if (args.length == 3) {
               StringUtil.copyPartialMatches(args[2], Arrays.asList(plugin.getSackManager().getSackNames()), completions);
            }
         }

         return completions;
      }
   }
}



