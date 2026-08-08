package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import xyz.redoxlabs.redeemcodes.Main;

import java.util.Collections;
import java.util.List;

public class InfoCommand implements Subcommand {
    private final Main plugin;

    public InfoCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(plugin.color("&e&m--------------------------------------------------"));
        sender.sendMessage(plugin.color("&d&lRedeemCodes &r&7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.color(""));
        sender.sendMessage(plugin.color("&8➤ &7Author: &f" + String.join(", ", plugin.getDescription().getAuthors())));
        sender.sendMessage(plugin.color("&8➤ &7GitHub: &bhttps://github.com/redoxhere/RedeemCodes"));
        sender.sendMessage(plugin.color(""));
        sender.sendMessage(plugin.color("&8➤ &7Description: &f" + plugin.getDescription().getDescription()));
        sender.sendMessage(plugin.color("&e&m--------------------------------------------------"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
