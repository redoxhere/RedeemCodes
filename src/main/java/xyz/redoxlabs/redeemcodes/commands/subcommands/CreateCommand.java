package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;
import xyz.redoxlabs.redeemcodes.managers.CreateCodeHandler;

import java.util.ArrayList;

public class CreateCommand implements Subcommand {
    private final Main plugin;

    public CreateCommand(Main plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String key) {
        return plugin.getMessagesConfig().getString("" + key, "&cMessage not found: " + key);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        FileConfiguration codes = plugin.getCodesConfig();

        if (args.length < 3) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.create.usage", "&#FF6347Usage: /rc create <new|copy> <name|exisiting-name> [new-name]"));
        } else {
            String type = args[1].toLowerCase();
            
            if (type.equals("new")) {
                String codeCreate = args[2];
                if (codes.contains("Codes." + codeCreate)) {
                    MessageUtil.sendRawMessage(plugin, player, getMessage("general.code-exists"));
                    MessageUtil.playSound(plugin, player, "sounds.failure");
                } else {
                    String path = "Codes." + codeCreate;
                    codes.set(path + ".enabled", true);
                    codes.set(path + ".permisson.required", false);
                    codes.set(path + ".permisson.list", new ArrayList<>());
                    codes.set(path + ".redeem-limit.player", 1);
                    codes.set(path + ".redeem-limit.ip", 1);
                    codes.set(path + ".redeem-limit.global", -1);
                    codes.set(path + ".redeem-limit.cooldown", 0);
                    codes.set(path + ".expire-time", -1);
                    codes.set(path + ".Playerlist.Blacklist.Type", "ENABLED");
                    codes.set(path + ".Playerlist.Blacklist.List", new ArrayList<>());
                    codes.set(path + ".rewards.type", "ALL");
                    codes.createSection(path + ".rewards.commands");
                    codes.set(path + ".rewards.sacks", new ArrayList<>());
                    codes.set(path + ".rewards.premades", new ArrayList<>());
                    codes.set(path + ".rewards.events", new ArrayList<>());
                    codes.set(path + ".rewards.list", new ArrayList<>());
                    plugin.saveCodesConfig();
                    String msg = plugin.getMessagesConfig().getString("general.code-created", "&aCode %code% has been created!").replace("%code%", codeCreate);
                    MessageUtil.sendMenuMessage(plugin, player, msg);
                    MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.create.editor-available", "&#E0E0E0[*] In-game editor is now available. Use command /rc"));
                    MessageUtil.playSound(plugin, player, "sounds.success");
                }
            } else if (type.equals("copy")) {
                if (args.length < 4) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.create.copy-usage", "&#FF6347Usage: /rc create copy <source-code> <new-code>"));
                    return true;
                }
                String sourceCode = args[2];
                String newCode = args[3];
                if (codes.contains("Codes." + newCode)) {
                    MessageUtil.sendRawMessage(plugin, player, getMessage("general.code-exists"));
                    MessageUtil.playSound(plugin, player, "sounds.failure");
                } else if (!codes.contains("Codes." + sourceCode)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.source-not-found", "&#FF6347Error: Source code configuration not found for '&#E0E0E0") + sourceCode + "&#FF6347'.");
                } else {
                    plugin.getDuplicationHandler().duplicateCode(player, sourceCode, newCode);
                }
            } else {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.create.usage", "&#FF6347Usage: /rc create <new|copy> <name|exisiting-name> [new-name]"));
            }
        }
        return true;
    }
    
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            return java.util.Arrays.asList("new", "copy");
        } else if (args.length == 3 && args[1].equalsIgnoreCase("copy")) {
            java.util.List<String> codes = new ArrayList<>();
            org.bukkit.configuration.ConfigurationSection section = plugin.getCodesConfig().getConfigurationSection("Codes");
            if (section != null) {
                codes.addAll(section.getKeys(false));
            }
            return codes;
        }
        return new java.util.ArrayList<>();
    }
}
