package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

public class RemoveCommand implements Subcommand {
    private final Main plugin;

    public RemoveCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        FileConfiguration codes = plugin.getCodesConfig();

        if (args.length < 2) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.remove.usage", "&#FF6347Usage: /rc remove <code>"));
        } else {
            String codeRemove = args[1];
            if (!codes.contains("Codes." + codeRemove)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "general.not-exist");
            } else {
                codes.set("Codes." + codeRemove, null);
                plugin.saveCodesConfig();
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.remove.success", "&#32CD32Code removed: &#00BFFF") + codeRemove);
                MessageUtil.playSound(plugin, player, "sounds.success");
            }
        }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (plugin.getCodesConfig().getConfigurationSection("Codes") != null) {
                return new java.util.ArrayList<>(plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false));
            }
        }
        return new java.util.ArrayList<>();
    }
}
