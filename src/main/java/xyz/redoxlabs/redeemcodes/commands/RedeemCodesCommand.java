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
        subcommands.put("reward", new RewardCommand(plugin));
        subcommands.put("help", new HelpCommand(plugin));
        subcommands.put("list", new ListCommand(plugin));
        subcommands.put("show", new ShowCommand(plugin));
        subcommands.put("redeemed", new RedeemedCommand(plugin));
        subcommands.put("review", new ReviewCommand(plugin));
    }

    private String getMessage(String key) {
        return plugin.color(plugin.getPrefix() + plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use admin commands.");
            return true;
        }

        if (!player.isOp() && !player.hasPermission("redeemcodes.admin")) {
            player.sendMessage(getMessage("no-permission"));
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
        }

        if (args.length < 1) {
            MainGUI.open(player);
            MessageUtil.playSound(plugin, player, "sounds.success");
            return true;
        }

        String action = args[0].toLowerCase();
        

        switch (action) {
            case "reload":
                plugin.reloadConfig();
                plugin.reloadCodesConfig();
                plugin.getPremadeManager().reloadPremades();
                plugin.getEventManager().reloadEvents();
                player.sendMessage(getMessage("reload-success"));
                MessageUtil.playSound(plugin, player, "sounds.success");
                return true;
            case "gui":
                MainGUI.open(player);
                MessageUtil.playSound(plugin, player, "sounds.success");
                return true;
            case "version":
                player.sendMessage(plugin.color("&bRedeemCodes Version: &f" + plugin.getDescription().getVersion()));
                return true;
        }

        Subcommand subcommand = subcommands.get(action);
        if (subcommand != null) {
            return subcommand.execute(sender, args);
        } else {
            player.sendMessage(getMessage("unknown-action").replace("%action%", action));
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "create", "remove", "sack", "premade", "event", "reward", "help", "list", "show", "redeemed", "gui", "review"), completions);
            return completions;
        } else {
            return completions;
        }
    }
}
