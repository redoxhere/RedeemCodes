package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.configuration.ConfigurationSection;

public class RewardCommand implements Subcommand {
    private final Main plugin;

    public RewardCommand(Main plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String key) {
        return plugin.color(plugin.getPrefix() + plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

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
                        handleAdd(player, args, codeName, codes, rewardPath);
                        break;
                    case "remove":
                        handleRemove(player, args, codeName, codes, rewardPath);
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
                        handleSetType(player, args, codeName, codes, rewardPath);
                        break;
                    case "setevent":
                        handleSetEvent(player, args, codeName, codes, rewardPath);
                        break;
                    default:
                        player.sendMessage(plugin.color("&cUnknown reward action. Use add, remove, view, settype, setevent."));
                }
            }
        }
        return true;
    }

    private void handleAdd(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
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

            String cmd = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
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
    }

    private void handleRemove(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
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
                        codes.set(packPath, null);
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
    }

    private void handleSetType(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
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
    }

    private void handleSetEvent(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
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
    }

    private void sendRewardsList(Player player, String codeName, int page) {
        FileConfiguration codes = plugin.getCodesConfig();
        String rewardPath = "Codes." + codeName + ".rewards";
        java.util.ArrayList<TextComponent> lines = new java.util.ArrayList<>();
        lines.add(new TextComponent(plugin.color("&d--- Rewards for &b" + codeName + " &7(Page " + page + ") &d---")));
        lines.add(new TextComponent(plugin.color("&7Distribution Type: &e" + codes.getString(rewardPath + ".type", "RANDOM"))));
        ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
        if (cmdSection != null) {
            for (String pack : cmdSection.getKeys(false)) {
                List<String> cmds = cmdSection.getStringList(pack);
                lines.add(new TextComponent(plugin.color("&e[Pack: " + pack + "]")));

                for (int i = 0; i < cmds.size(); ++i) {
                    TextComponent line = new TextComponent(plugin.color("  &7" + i + ": " + cmds.get(i) + " "));
                    TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
                    removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to remove this command").create()));
                    removeBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove command " + pack + " " + i));
                    line.addExtra(removeBtn);
                    lines.add(line);
                }
            }
        }

        List<String> sacks = codes.getStringList(rewardPath + ".sacks");
        if (!sacks.isEmpty()) {
            lines.add(new TextComponent(plugin.color("&e[Sacks]")));

            for (String s : sacks) {
                String name = s.split(":")[0];
                TextComponent line = new TextComponent(plugin.color("  &7- " + s + " "));
                TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
                removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to remove this sack").create()));
                removeBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove sack " + name));
                line.addExtra(removeBtn);
                lines.add(line);
            }
        }

        List<String> premades = codes.getStringList(rewardPath + ".premades");
        if (!premades.isEmpty()) {
            lines.add(new TextComponent(plugin.color("&e[Premades]")));

            for (String p : premades) {
                String name = p.split(":")[0];
                TextComponent line = new TextComponent(plugin.color("  &7- " + p + " "));
                TextComponent removeBtn = new TextComponent(plugin.color("&c[-]"));
                removeBtn.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to remove this premade").create()));
                removeBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rc reward " + codeName + " remove premade " + name));
                line.addExtra(removeBtn);
                lines.add(line);
            }
        }

        List<String> events = codes.getStringList(rewardPath + ".events");
        if (!events.isEmpty()) {
            lines.add(new TextComponent(plugin.color("&e[Events]")));

            for (String e : events) {
                TextComponent line = new TextComponent(plugin.color("  &7- " + e + " "));
                lines.add(line);
            }
        }

        int perPage = 10;
        int total = lines.size();
        int maxPage = (int) Math.ceil((double) total / (double) perPage);
        if (page < 1) {
            page = 1;
        }

        if (page > maxPage) {
            page = maxPage;
        }

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, total);

        for (int i = start; i < end; ++i) {
            player.spigot().sendMessage(lines.get(i));
        }

        player.sendMessage(plugin.color("&e&m                                                                       "));
    }
}
