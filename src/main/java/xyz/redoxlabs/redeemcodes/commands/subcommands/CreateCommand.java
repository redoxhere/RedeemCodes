package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.ArrayList;

public class CreateCommand implements Subcommand {
    private final Main plugin;

    public CreateCommand(Main plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String key) {
        return plugin.color(plugin.getPrefix() + plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        FileConfiguration codes = plugin.getCodesConfig();

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
                player.sendMessage(getMessage("code-created").replace("%code%", codeCreate));
                player.sendMessage(plugin.color("&e[*] In-game editor is now available. Use command /rc"));
                MessageUtil.playSound(plugin, player, "sounds.success");
            }
        }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return new java.util.ArrayList<>();
    }
}
