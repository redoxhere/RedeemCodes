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
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 3) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.event.usage-create");
        } else {
            String sub = args[1].toLowerCase();
            String name = args[2];
            if (sub.equals("create")) {
                if (plugin.getEventManager().createEvent(name)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.created", "&aEvent '&e%event%&a' created.").replace("%event%", name));
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.playSound(plugin, player, "sounds.success");
                } else {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.event.exists");
                }
            } else if (sub.equals("remove")) {
                if (plugin.getEventManager().deleteEvent(name)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.removed", "&aEvent '&e%event%&a' removed.").replace("%event%", name));
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.playSound(plugin, player, "sounds.success");
                } else {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.event.not-found");
                }
            } else if (sub.equals("play")) {
                if (!plugin.getEventManager().eventExists(name)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.event.not-found");
                    return true;
                }

                plugin.getEventManager().executeEvent(player, name);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.playing", "&aPlaying event '&e%event%&a'.").replace("%event%", name));
            } else if (sub.equals("add")) {
                if (args.length < 4) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.usage-add", "&cUsage: /rc event add %event% <firework|command|sound>").replace("%event%", name));
                    return true;
                }

                if (!plugin.getEventManager().eventExists(name)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.event.not-found");
                    return true;
                }

                String type = args[3].toLowerCase();
                if (type.equals("firework")) {
                    plugin.getEventGUI().openFireworkEditor(player, name);
                } else if (type.equals("sound")) {
                    plugin.getEventGUI().openSoundList(player, name);
                } else if (type.equals("command")) {
                    if (args.length < 5) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.usage-cmd", "&cUsage: /rc event add %event% command <console command>").replace("%event%", name));
                        return true;
                    }

                    String cmdLine = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
                    FileConfiguration config = plugin.getEventManager().getEventConfig(name);
                    List<String> cmds = config.getStringList("commands");
                    cmds.add(cmdLine);
                    config.set("commands", cmds);
                    plugin.getEventManager().saveEvent(name);
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("commands.event.cmd-added", "&aCommand added to event '&e%event%&a'.").replace("%event%", name));
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.playSound(plugin, player, "sounds.success");
                }
            }
        }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            return java.util.Arrays.asList("create", "remove", "add", "play");
        } else if (args.length == 3 && !args[1].equalsIgnoreCase("create")) {
            return new java.util.ArrayList<>(plugin.getEventManager().getEventNames());
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            return java.util.Arrays.asList("firework", "sound", "command");
        }
        return new java.util.ArrayList<>();
    }
}
