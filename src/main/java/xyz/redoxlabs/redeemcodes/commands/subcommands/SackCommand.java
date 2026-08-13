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
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 2) {
            xyz.redoxlabs.redeemcodes.guis.GlobalSackListGUI gui = new xyz.redoxlabs.redeemcodes.guis.GlobalSackListGUI(plugin);
            plugin.openGlobalSackGUIs.put(player, gui);
            gui.open(player, 0);
            return true;
        }

        if (args.length < 3 && !args[1].equalsIgnoreCase("list")) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.usage", "&#FF6347Usage: /rc sack <create|remove|edit|give|list> [name]"));
            return true;
        }
        
        String sackAction = args[1].toLowerCase();
        if (sackAction.equals("list")) {
            xyz.redoxlabs.redeemcodes.guis.GlobalSackListGUI gui = new xyz.redoxlabs.redeemcodes.guis.GlobalSackListGUI(plugin);
            plugin.openGlobalSackGUIs.put(player, gui);
            gui.open(player, 0);
            return true;
        }
        
        String sackName = args[2];
            switch (sackAction) {
                case "create":
                    if (plugin.getSackManager().createSack(sackName)) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.created", "&#32CD32Sack \'&#00BFFF") + sackName + "&#32CD32\' created successfully!");
                        MessageUtil.playSound(plugin, player, "sounds.success");
                    } else {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.sack.already-exists", "&#FF6347Sack \'&#00BFFF") + sackName + "&#FF6347\' already exists.");
                    }
                    break;
                case "remove":
                case "delete":
                    if (plugin.getSackManager().deleteSack(sackName)) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.removed", "&#32CD32Sack removed."));
                        MessageUtil.playSound(plugin, player, "sounds.success");
                    } else {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.not-found", "&#FF6347Sack not found."));
                    }
                    break;
                case "edit":
                case "open":
                    plugin.getSackManager().openEditGUI(player, sackName);
                    break;
                case "give":
                    Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : player;
                    if (target == null) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.player-not-found", "&#FF6347Player not found."));
                        return true;
                    }

                    plugin.getSackManager().giveSack(target, sackName);
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.sack.given", "&#32CD32Gave sack to &#00BFFF") + target.getName());
                    MessageUtil.playSound(plugin, player, "sounds.success");
            }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            return java.util.Arrays.asList("create", "delete", "edit", "give", "list");
        } else if (args.length == 3 && !args[1].equalsIgnoreCase("create")) {
            return java.util.Arrays.asList(plugin.getSackManager().getSackNames());
        }
        return new java.util.ArrayList<>();
    }
}
