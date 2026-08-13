package xyz.redoxlabs.redeemcodes.commands;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.commands.subcommands.*;
import xyz.redoxlabs.redeemcodes.guis.MainGUI;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedeemCodesCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public RedeemCodesCommand(Main plugin) {
        this.plugin = plugin;
        registerSubcommands();
    }

    private void registerSubcommands() {
        subcommands.put("create", new CreateCommand(plugin));
        subcommands.put("remove", new RemoveCommand(plugin));
        subcommands.put("sack", new SackCommand(plugin));
        subcommands.put("premade", new PremadeCommand(plugin));
        subcommands.put("event", new EventCommand(plugin));
        subcommands.put("edit", new EditCommand(plugin));
        subcommands.put("help", new HelpCommand(plugin));
        subcommands.put("list", new ListCommand(plugin));
        subcommands.put("show", new ShowCommand(plugin));
        subcommands.put("redeemed", new RedeemedCommand(plugin));
        subcommands.put("review", new ReviewCommand(plugin));
        subcommands.put("info", new InfoCommand(plugin));
        subcommands.put("test", new TestCommand(plugin));
    }

    private String getMessage(String key) {
        return plugin.getMessagesConfig().getString(key, "&cMessage not found: " + key);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("redeemcodes.admin")) {
            MessageUtil.sendRawMessage(plugin, sender, getMessage("general.no-permission"));
            if (sender instanceof Player) MessageUtil.playSound(plugin, (Player) sender, "sounds.failure");
            return true;
        }

        if (args.length < 1) {
            if (sender instanceof Player) {
                MainGUI.open((Player) sender);
                MessageUtil.playSound(plugin, (Player) sender, "sounds.success");
            } else {
                sender.sendMessage("§cYou must specify a subcommand. (e.g. /redeemcodes help)");
            }
            return true;
        }

        String action = args[0].toLowerCase();
        
        switch (action) {
            case "reload":
                plugin.reloadConfig();
                plugin.reloadCodesConfig();
                plugin.reloadMessagesConfig();
                plugin.getPremadeManager().reloadPremades();
                plugin.getEventManager().reloadEvents();
                MessageUtil.sendRawMessage(plugin, sender, getMessage("commands.reload.success"));
                if (sender instanceof Player) MessageUtil.playSound(plugin, (Player) sender, "sounds.success");
                return true;
            case "gui":
                if (sender instanceof Player) {
                    MainGUI.open((Player) sender);
                    MessageUtil.playSound(plugin, (Player) sender, "sounds.success");
                } else {
                    sender.sendMessage("§cOnly players can open GUIs.");
                }
                return true;
            case "version":
                sender.sendMessage(plugin.color("&bRedeemCodes Version: &f" + plugin.getDescription().getVersion()));
                return true;
        }

        Subcommand subcommand = subcommands.get(action);
        if (subcommand != null) {
            return subcommand.execute(sender, args);
        } else {
            MessageUtil.sendRawMessage(plugin, sender, getMessage("commands.unknown-action").replace("%action%", action));
            if (sender instanceof Player) MessageUtil.playSound(plugin, (Player) sender, "sounds.failure");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "create", "remove", "sack", "premade", "event", "edit", "help", "list", "show", "redeemed", "gui", "review", "info", "test"), completions);
            return completions;
        } else if (args.length > 1) {
            Subcommand subcommand = subcommands.get(args[0].toLowerCase());
            if (subcommand != null) {
                List<String> subCompletions = subcommand.onTabComplete(sender, args);
                if (subCompletions != null) {
                    StringUtil.copyPartialMatches(args[args.length - 1], subCompletions, completions);
                    return completions;
                }
            }
        }
        return completions;
    }
}
