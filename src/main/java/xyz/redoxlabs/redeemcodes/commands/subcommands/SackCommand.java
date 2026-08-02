package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

public class SackCommand implements Subcommand {
    private final Main plugin;

    public SackCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return true;

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
                        return true;
                    }

                    plugin.getSackManager().giveSack(target, sackName);
                    player.sendMessage(plugin.color("&aGave sack to " + target.getName()));
                    MessageUtil.playSound(plugin, player, "sounds.success");
            }
        }
        return true;
    }
}
