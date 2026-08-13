package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;
import xyz.redoxlabs.redeemcodes.utils.TimeFormatter;

import java.util.Arrays;
import java.util.List;

public class EditCommand implements Subcommand {
    private final Main plugin;

    public EditCommand(Main plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String key) {
        return plugin.getMessagesConfig().getString("" + key, "&cMessage not found: " + key);
    }

    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (plugin.getCodesConfig().getConfigurationSection("Codes") != null) {
                return new java.util.ArrayList<>(plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false));
            }
        } else if (args.length == 3) {
            return java.util.Arrays.asList("cooldown", "expire", "enabled", "limit", "permission", "blacklist", "reward");
        } else if (args.length == 4) {
            String prop = args[2].toLowerCase();
            if (prop.equals("reward")) return java.util.Arrays.asList("add", "remove", "view", "settype", "setevent");
            if (prop.equals("permission")) return java.util.Arrays.asList("toggle", "add", "remove", "clear");
            if (prop.equals("blacklist")) return java.util.Arrays.asList("toggle", "add", "remove", "clear");
            if (prop.equals("enabled")) return java.util.Arrays.asList("true", "false", "toggle");
            if (prop.equals("limit")) return java.util.Arrays.asList("global", "player", "ip");
        } else if (args.length == 5 && args[2].equalsIgnoreCase("reward") && args[3].equalsIgnoreCase("add")) {
            return java.util.Arrays.asList("COMMAND_PACK", "SACK", "PREMADE", "EVENT");
        }
        return new java.util.ArrayList<>();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 3) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.edit.usage", "&#FF6347Usage: /rc edit <codename> <property> [args...]"));
            return true;
        }

        String codeName = args[1];
        String prop = args[2].toLowerCase();
        FileConfiguration codes = plugin.getCodesConfig();
        
        if (!codes.contains("Codes." + codeName)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, getMessage("general.not-exist"));
            return true;
        }

        switch (prop) {
            case "cooldown":
                handleCooldown(player, args, codeName, codes);
                break;
            case "expire":
                handleExpire(player, args, codeName, codes);
                break;
            case "enabled":
                handleEnabled(player, args, codeName, codes);
                break;
            case "limit":
                handleLimit(player, args, codeName, codes);
                break;
            case "permission":
                handlePermission(player, args, codeName, codes);
                break;
            case "blacklist":
                handleBlacklist(player, args, codeName, codes);
                break;
            case "reward":
                handleReward(player, args, codeName, codes);
                break;
            default:
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.edit.unknown-prop", "&#FF6347Unknown property. Use cooldown, expire, enabled, limit, permission, blacklist, reward."));
                break;
        }
        return true;
    }

    private void handleCooldown(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " cooldown <time (e.g. 1h)>");
            return;
        }
        String timeStr = args[3];
        long minutes = TimeFormatter.parseTimeToMinutes(timeStr);
        if (minutes < 0) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.invalid-format", "&#FF6347Invalid format. Use 1s, 3m, 1h, 1d, 1w, 1mn, 1y."));
            return;
        }
        codes.set("Codes." + codeName + ".redeem-limit.Cooldown", (int)minutes);
        plugin.saveCodesConfig();
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFACooldown set to &#00BFFF" + timeStr + " &#87CEFA(" + minutes + " minutes).");
    }

    private void handleExpire(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " expire <time (e.g. 1w) | never>");
            return;
        }
        String timeStr = args[3];
        if (timeStr.equalsIgnoreCase("never")) {
            plugin.getExpirationManager().setExpiration(codeName, -1L);
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAExpire time disabled (never expires).");
            return;
        }
        long seconds = TimeFormatter.parseTimeToSeconds(timeStr);
        if (seconds < 0) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.invalid-format", "&#FF6347Invalid format. Use 1s, 3m, 1h, 1d, 1w, 1mn, 1y."));
            return;
        }
        plugin.getExpirationManager().setExpiration(codeName, seconds);
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAExpire time set to &#00BFFF" + timeStr + " &#87CEFA(" + seconds + " seconds).");
    }

    private void handleEnabled(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " enabled <true|false|toggle>");
            return;
        }
        String val = args[3].toLowerCase();
        boolean current = codes.getBoolean("Codes." + codeName + ".enabled", true);
        boolean next;
        if (val.equals("toggle")) next = !current;
        else next = Boolean.parseBoolean(val);
        
        codes.set("Codes." + codeName + ".enabled", next);
        plugin.saveCodesConfig();
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAEnabled status for &#00BFFF" + codeName + " &#87CEFAset to: " + (next ? "&#00FF7FTrue" : "&#FF4500False"));
    }

    private void handleLimit(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 5) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " limit <global|player|ip> <amount>");
            return;
        }
        String limitType = args[3].toLowerCase();
        if (!java.util.Arrays.asList("global", "player", "ip").contains(limitType)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Invalid limit type. Use global, player, or ip.");
            return;
        }
        try {
            int amount = Integer.parseInt(args[4]);
            codes.set("Codes." + codeName + ".redeem-limit." + limitType, amount);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFA" + limitType.toUpperCase() + " Limit for &#00BFFF" + codeName + " &#87CEFAset to: &#00FF7F" + amount);
        } catch (NumberFormatException e) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Invalid number format.");
        }
    }

    private void handlePermission(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " permission <toggle|add|remove|clear> [perm]");
            return;
        }
        String action = args[3].toLowerCase();
        if (action.equals("toggle")) {
            boolean req = codes.getBoolean("Codes." + codeName + ".permisson.required", false);
            codes.set("Codes." + codeName + ".permisson.required", !req);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAPermission Required toggle set to: " + (!req ? "&#00FF7FTrue" : "&#FF4500False"));
        } else if (action.equals("clear")) {
            codes.set("Codes." + codeName + ".permisson.list", new java.util.ArrayList<>());
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAAll permissions cleared from &#00BFFF" + codeName);
        } else if (action.equals("add") || action.equals("remove")) {
            if (args.length < 5) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " permission " + action + " <perm>");
                return;
            }
            String perm = args[4];
            List<String> list = codes.getStringList("Codes." + codeName + ".permisson.list");
            if (action.equals("add")) {
                if (!list.contains(perm)) list.add(perm);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAAdded permission &#00BFFF" + perm + " &#87CEFAto " + codeName);
            } else {
                list.remove(perm);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFARemoved permission &#00BFFF" + perm + " &#87CEFAfrom " + codeName);
            }
            codes.set("Codes." + codeName + ".permisson.list", list);
            plugin.saveCodesConfig();
        }
    }

    private void handleBlacklist(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " blacklist <toggle|add|remove|clear> [player]");
            return;
        }
        String action = args[3].toLowerCase();
        if (action.equals("toggle")) {
            String bl = codes.getString("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
            bl = bl.equalsIgnoreCase("ENABLED") ? "DISABLED" : (bl.equalsIgnoreCase("DISABLED") ? "REVERSED" : "ENABLED");
            codes.set("Codes." + codeName + ".Playerlist.Blacklist.Type", bl);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFABlacklist toggle set to: &#00BFFF" + bl);
        } else if (action.equals("clear")) {
            codes.set("Codes." + codeName + ".Playerlist.Blacklist.List", new java.util.ArrayList<>());
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAAll players cleared from blacklist of &#00BFFF" + codeName);
        } else if (action.equals("add") || action.equals("remove")) {
            if (args.length < 5) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " blacklist " + action + " <player>");
                return;
            }
            String pName = args[4];
            List<String> list = codes.getStringList("Codes." + codeName + ".Playerlist.Blacklist.List");
            if (action.equals("add")) {
                if (!list.contains(pName)) list.add(pName);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFAAdded &#00BFFF" + pName + " &#87CEFAto blacklist of " + codeName);
            } else {
                list.remove(pName);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#87CEFARemoved &#00BFFF" + pName + " &#87CEFAfrom blacklist of " + codeName);
            }
            codes.set("Codes." + codeName + ".Playerlist.Blacklist.List", list);
            plugin.saveCodesConfig();
        }
    }

    private void handleReward(Player player, String[] args, String codeName, FileConfiguration codes) {
        if (args.length < 4) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward <add|remove|view|settype|setevent> ...");
            return;
        }
        String sub = args[3].toLowerCase();
        String rewardPath = "Codes." + codeName + ".rewards";
        switch (sub) {
            case "add":
                handleAddReward(player, args, codeName, codes, rewardPath);
                break;
            case "remove":
                handleRemoveReward(player, args, codeName, codes, rewardPath);
                break;
            case "view":
                int page = 1;
                if (args.length > 4) {
                    try {
                        page = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {}
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
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Unknown reward action. Use add, remove, view, settype, setevent.");
        }
    }
    
    // Extracted directly from RewardCommand.java
    private void handleAddReward(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
        if (args.length < 6) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward add <command|sack|premade> <name> [cmd]");
            return;
        }
        String type = args[4].toLowerCase();
        String name = args[5];
        if (type.equals("command")) {
            if (args.length < 7) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&cUsage: /rc edit " + codeName + " reward add command <packname> <console command>");
                return;
            }
            String cmd = String.join(" ", Arrays.copyOfRange(args, 6, args.length));
            List<String> packCmds = codes.getStringList(rewardPath + ".commands." + name);
            packCmds.add(cmd);
            codes.set(rewardPath + ".commands." + name, packCmds);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Added command to pack '&#00BFFF" + name + "&#32CD32' in code '&#00BFFF" + codeName + "&#32CD32'.");
            MessageUtil.playSound(plugin, player, "sounds.success");
        } else if (type.equals("sack")) {
            if (!plugin.getSackManager().sackExists(name)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Sack '&#00BFFF" + name + "&#FF6347' does not exist in sacks folder.");
                return;
            }
            List<String> sackList = codes.getStringList(rewardPath + ".sacks");
            if (sackList.stream().anyMatch((s) -> s.split(":")[0].equals(name))) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Sack '&#00BFFF" + name + "&#FF6347' is already added to rewards.");
                return;
            }
            sackList.add(name + ":1");
            codes.set(rewardPath + ".sacks", sackList);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Added sack '&#00BFFF" + name + "&#32CD32' to code '&#00BFFF" + codeName + "&#32CD32'.");
            MessageUtil.playSound(plugin, player, "sounds.success");
        } else if (type.equals("premade")) {
            if (!plugin.getPremadeManager().premadeExists(name)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Premade '&#00BFFF" + name + "&#FF6347' does not exist in premades.yml.");
                return;
            }
            List<String> premadeList = codes.getStringList(rewardPath + ".premades");
            if (premadeList.stream().anyMatch((p) -> p.split(":")[0].equals(name))) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Premade '&#00BFFF" + name + "&#FF6347' is already added to rewards.");
                return;
            }
            premadeList.add(name + ":1");
            codes.set(rewardPath + ".premades", premadeList);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Added premade '&#00BFFF" + name + "&#32CD32' to code '&#00BFFF" + codeName + "&#32CD32'.");
            MessageUtil.playSound(plugin, player, "sounds.success");
        } else {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Unknown type. Use command, sack, or premade.");
        }
    }

    private void handleRemoveReward(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
        if (args.length < 6) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward remove <command|sack|premade> <name> [id]");
            return;
        }
        String removeType = args[4].toLowerCase();
        String targetName = args[5];
        if (removeType.equals("command")) {
            if (args.length < 7) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward remove command <packname> <index>");
                return;
            }
            String packPath = rewardPath + ".commands." + targetName;
            if (!codes.contains(packPath)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Command pack '&#00BFFF" + targetName + "&#FF6347' does not exist in this code.");
                return;
            }
            List<String> cmds = codes.getStringList(packPath);
            try {
                int index = Integer.parseInt(args[6]);
                if (index >= 0 && index < cmds.size()) {
                    cmds.remove(index);
                    if (cmds.isEmpty()) {
                        codes.set(packPath, null);
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Removed empty command pack '&#00BFFF" + targetName + "&#32CD32'.");
                    } else {
                        codes.set(packPath, cmds);
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Removed command at index " + index + " from pack '&#00BFFF" + targetName + "&#32CD32'.");
                    }
                    plugin.saveCodesConfig();
                    MessageUtil.playSound(plugin, player, "sounds.success");
                } else {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Index out of bounds.");
                }
            } catch (NumberFormatException e) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Invalid index.");
            }
        } else if (removeType.equals("sack")) {
            List<String> sackList = codes.getStringList(rewardPath + ".sacks");
            if (sackList.removeIf((s) -> s.split(":")[0].equals(targetName))) {
                codes.set(rewardPath + ".sacks", sackList);
                plugin.saveCodesConfig();
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Removed sack '&#00BFFF" + targetName + "&#32CD32' from code rewards.");
                MessageUtil.playSound(plugin, player, "sounds.success");
            } else {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Sack '&#00BFFF" + targetName + "&#FF6347' not found in rewards.");
            }
        } else if (removeType.equals("premade")) {
            List<String> premadeList = codes.getStringList(rewardPath + ".premades");
            if (premadeList.removeIf((p) -> p.split(":")[0].equals(targetName))) {
                codes.set(rewardPath + ".premades", premadeList);
                plugin.saveCodesConfig();
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Removed premade '&#00BFFF" + targetName + "&#32CD32' from code rewards.");
                MessageUtil.playSound(plugin, player, "sounds.success");
            } else {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Premade '&#00BFFF" + targetName + "&#FF6347' not found in rewards.");
            }
        }
    }

    private void handleSetType(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
        if (args.length < 5) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward settype <RANDOM|ALL|DRAW>");
            return;
        }
        String newType = args[4].toUpperCase();
        if (Arrays.asList("RANDOM", "ALL", "DRAW").contains(newType)) {
            codes.set(rewardPath + ".type", newType);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Reward type for '&#00BFFF" + codeName + "&#32CD32' set to &#00BFFF" + newType + "&#32CD32.");
            MessageUtil.playSound(plugin, player, "sounds.success");
        } else {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Invalid type. Use RANDOM, ALL, or DRAW.");
        }
    }

    private void handleSetEvent(Player player, String[] args, String codeName, FileConfiguration codes, String rewardPath) {
        if (args.length < 5) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Usage: /rc edit " + codeName + " reward setevent <eventname|none>");
            return;
        }
        String eventTarget = args[4];
        List<String> events = codes.getStringList(rewardPath + ".events");
        if (eventTarget.equalsIgnoreCase("none")) {
            events.clear();
            codes.set(rewardPath + ".events", events);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Cleared all events from code '&#00BFFF" + codeName + "&#32CD32'.");
        } else {
            if (!plugin.getEventManager().eventExists(eventTarget)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#FF6347Event '&#00BFFF" + eventTarget + "&#FF6347' does not exist.");
                return;
            }
            events.clear();
            events.add(eventTarget);
            codes.set(rewardPath + ".events", events);
            plugin.saveCodesConfig();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, "&#32CD32Set event for code '&#00BFFF" + codeName + "&#32CD32' to '&#00BFFF" + eventTarget + "&#32CD32'.");
        }
        MessageUtil.playSound(plugin, player, "sounds.success");
    }

    private void sendRewardsList(Player player, String codeName, int page) {
        player.performCommand("rc show " + codeName);
    }
}
