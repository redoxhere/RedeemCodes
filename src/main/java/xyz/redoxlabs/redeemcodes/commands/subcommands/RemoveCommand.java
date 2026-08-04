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
            player.sendMessage("§cUsage: /rc remove <code>");
        } else {
            String codeRemove = args[1];
            if (!codes.contains("Codes." + codeRemove)) {
                player.sendMessage("§cThis code doesn't exist.");
            } else {
                codes.set("Codes." + codeRemove, null);
                plugin.saveCodesConfig();
                player.sendMessage(plugin.color("&aCode removed: &c" + codeRemove));
                MessageUtil.playSound(plugin, player, "sounds.success");
            }
        }
        return true;
    }
}
