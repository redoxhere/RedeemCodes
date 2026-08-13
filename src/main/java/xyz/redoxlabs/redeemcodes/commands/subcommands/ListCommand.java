package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

import java.util.Set;

public class ListCommand implements Subcommand {

    private final Main plugin;

    public ListCommand(Main plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        FileConfiguration codes = plugin.getCodesConfig();
        
        if (!codes.contains("Codes") || codes.getConfigurationSection("Codes").getKeys(false).isEmpty()) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, sender, "commands.list.empty");
            return true;
        }
        
        Set<String> codeNames = codes.getConfigurationSection("Codes").getKeys(false);
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.list.header", "&#1E90FF&m                                                                                &r &#00BFFFExisting Codes &r&#1E90FF&m                                                                                "));

        String itemTemplate = plugin.getMessagesConfig().getString("commands.list.item", "<hover:&eClick to view details!\n&7Status: %status_formatted%\n&7Stock: &f%stock%><click:run_command:/rc show %code%>&#00BFFF➤ &#E0E0E0%code% %status_formatted%</click></hover>");

        for (String code : codeNames) {
            boolean enabled = codes.getBoolean("Codes." + code + ".enabled", true);
            String status = enabled ? plugin.getMessagesConfig().getString("commands.list.status-enabled", "&#00FF7F(Enabled)") : plugin.getMessagesConfig().getString("commands.list.status-disabled", "&#FF4500(Disabled)");
            
            int globalLimit = codes.getInt("Codes." + code + ".redeem-limit.global", -1);
            String stock = globalLimit == -1 ? "Infinite" : String.valueOf(globalLimit);
            
            String line = itemTemplate.replace("%code%", code)
                                      .replace("%status_formatted%", status)
                                      .replace("%stock%", stock);
                                      
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, line);
        }
        
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, sender, plugin.getMessagesConfig().getString("commands.list.footer", "&#1E90FF&m                                                                                "));
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return new java.util.ArrayList<>();
    }
}
