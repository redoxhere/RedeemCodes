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
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.divider", "&#1E90FF&m                                  "));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.version", "&#00BFFFRedeemCodes &#E0E0E0v") + plugin.getDescription().getVersion());
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, "");
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.author", "&#1E90FF➤ &#E0E0E0Author: &#00BFFF") + String.join(", ", plugin.getDescription().getAuthors()));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.github", "<hover:&#E0E0E0Click to open><click:open_url:https://github.com/redoxhere/RedeemCodes>&#1E90FF➤ &#E0E0E0GitHub: &#00BFFFhttps://github.com/redoxhere/RedeemCodes</click></hover>"));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, "");
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.description", "&#1E90FF➤ &#E0E0E0Description: &#00BFFF") + plugin.getDescription().getDescription());
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.info.divider", "&#1E90FF&m                                  "));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
