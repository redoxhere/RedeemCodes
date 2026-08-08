package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

public class PremadeCommand implements Subcommand {
    private final Main plugin;

    public PremadeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage(plugin.color("&cUsage: /rc premade <add|remove|view> <name> [command/index]"));
        } else {
            String sub = args[1].toLowerCase();
            String name = args[2];
            if (sub.equals("add")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.color("&cUsage: /rc premade add " + name + " <command line>"));
                    return true;
                }

                String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                plugin.getPremadeManager().addCommand(name, command);
                player.sendMessage(plugin.color("&aAdded command to premade '&e" + name + "&a'."));
                MessageUtil.playSound(plugin, player, "sounds.success");
            } else if (sub.equals("remove")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.color("&cUsage: /rc premade remove " + name + " <index>"));
                    return true;
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
                    return true;
                }

                player.sendMessage(plugin.color("&d--- Premade: &e" + name + " &d---"));

                for (int i = 0; i < cmds.size(); ++i) {
                    player.sendMessage(plugin.color("&b" + i + ": &7" + cmds.get(i)));
                }
            }
        }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            return java.util.Arrays.asList("add", "remove", "view");
        } else if (args.length == 3) {
            return new java.util.ArrayList<>(plugin.getPremadeManager().getPremadeNames());
        }
        return new java.util.ArrayList<>();
    }
}
