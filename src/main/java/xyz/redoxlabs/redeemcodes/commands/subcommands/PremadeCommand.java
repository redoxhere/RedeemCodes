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

        if (args.length < 2) {
            xyz.redoxlabs.redeemcodes.guis.GlobalPremadeListGUI gui = new xyz.redoxlabs.redeemcodes.guis.GlobalPremadeListGUI(plugin);
            plugin.openGlobalPremadeGUIs.put(player, gui);
            gui.open(player, 0);
            return true;
        }

        if (args.length < 3 && !args[1].equalsIgnoreCase("list")) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.usage", "&#FF6347Usage: /rc premade <add|remove|view|list> [name]"));
            return true;
        }
        
        String sub = args[1].toLowerCase();
        if (sub.equals("list")) {
            xyz.redoxlabs.redeemcodes.guis.GlobalPremadeListGUI gui = new xyz.redoxlabs.redeemcodes.guis.GlobalPremadeListGUI(plugin);
            plugin.openGlobalPremadeGUIs.put(player, gui);
            gui.open(player, 0);
            return true;
        }
        
        String name = args[2];
            if (sub.equals("add")) {
                if (args.length < 4) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.usage-add", "&#FF6347Usage: /rc premade add &#00BFFF%name% &#FF6347<command line>").replace("%name%", name));
                    return true;
                }

                String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                plugin.getPremadeManager().addCommand(name, command);
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.added", "&#32CD32Added command to premade '&#00BFFF%name%&#32CD32'.").replace("%name%", name));
                MessageUtil.playSound(plugin, player, "sounds.success");
            } else if (sub.equals("remove")) {
                if (args.length < 4) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.usage-remove", "&#FF6347Usage: /rc premade remove &#00BFFF%name% &#FF6347<index>").replace("%name%", name));
                    return true;
                }

                try {
                    int index = Integer.parseInt(args[3]);
                    if (plugin.getPremadeManager().removeCommand(name, index)) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.removed", "&#32CD32Removed command at index &#00BFFF%index% &#32CD32from premade '&#00BFFF%name%&#32CD32'.").replace("%name%", name).replace("%index%", String.valueOf(index)));
                        MessageUtil.playSound(plugin, player, "sounds.success");
                    } else {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.out-of-bounds", "&#FF6347Index out of bounds."));
                    }
                } catch (NumberFormatException e) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.invalid-index", "&#FF6347Invalid index."));
                }
            } else if (sub.equals("view")) {
                List<String> cmds = plugin.getPremadeManager().getPremadeCommands(name);
                if (cmds.isEmpty()) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.not-found", "&#FF6347Premade '&#00BFFF%name%&#FF6347' not found or empty.").replace("%name%", name));
                    return true;
                }

                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.view-header", "&#1E90FF&m        &r &#00BFFFPremade: &#E0E0E0%name% &r&#1E90FF&m        ").replace("%name%", name));

                for (int i = 0; i < cmds.size(); ++i) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.premade.view-item", "  &#00BFFF%index%: &#E0E0E0%cmd%").replace("%index%", String.valueOf(i)).replace("%cmd%", cmds.get(i)));
                }
            }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            return java.util.Arrays.asList("add", "remove", "view", "list");
        } else if (args.length == 3) {
            return new java.util.ArrayList<>(plugin.getPremadeManager().getPremadeNames());
        }
        return new java.util.ArrayList<>();
    }
}
