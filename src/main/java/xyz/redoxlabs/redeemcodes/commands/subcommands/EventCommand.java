package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

public class EventCommand implements Subcommand {
    private final Main plugin;

    public EventCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return true;

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
                    return true;
                }

                plugin.getEventManager().executeEvent(player, name);
                player.sendMessage(plugin.color("&aPlaying event '&e" + name + "&a'."));
            } else if (sub.equals("add")) {
                if (args.length < 4) {
                    player.sendMessage(plugin.color("&cUsage: /rc event add " + name + " <firework|command|sound>"));
                    return true;
                }

                if (!plugin.getEventManager().eventExists(name)) {
                    player.sendMessage(plugin.color("&cEvent '&e" + name + "&c' does not exist. Create it first."));
                    return true;
                }

                String type = args[3].toLowerCase();
                if (type.equals("firework")) {
                    plugin.getEventGUI().openFireworkEditor(player, name);
                } else if (type.equals("sound")) {
                    plugin.getEventGUI().openSoundList(player, name);
                } else if (type.equals("command")) {
                    if (args.length < 5) {
                        player.sendMessage(plugin.color("&cUsage: /rc event add " + name + " command <console command>"));
                        return true;
                    }

                    String cmdLine = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
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
        return true;
    }
}
