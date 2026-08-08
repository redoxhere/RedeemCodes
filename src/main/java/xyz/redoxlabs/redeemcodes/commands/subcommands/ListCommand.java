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

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        FileConfiguration codes = plugin.getCodesConfig();
        
        if (!codes.contains("Codes")) {
            sender.sendMessage(plugin.color("&cNo codes found."));
            return true;
        }
        
        Set<String> codeNames = codes.getConfigurationSection("Codes").getKeys(false);
        sender.sendMessage(plugin.color("&d---- &bList of Existing Codes &d----"));

        for (String code : codeNames) {
            boolean enabled = codes.getBoolean("Codes." + code + ".enabled", true);
            String status = enabled ? "&a(Enabled)" : "&c(Disabled)";
            
            if (sender instanceof Player) {
                Player player = (Player) sender;
                TextComponent component = new TextComponent(plugin.color((enabled ? "&a" : "&c") + "➤ " + code));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redeemcodes show " + code));
                
                int globalLimit = codes.getInt("Codes." + code + ".redeem-limit.global", -1);
                String stock = globalLimit == -1 ? "Infinite" : String.valueOf(globalLimit);
                
                component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(plugin.color("&eClick to view details!\n&7Status: " + status + "\n&7Stock: &f" + stock)).create()));
                player.spigot().sendMessage(component);
            } else {
                sender.sendMessage(plugin.color((enabled ? "&a" : "&c") + "➤ " + code + " " + status));
            }
        }
        return true;
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return new java.util.ArrayList<>();
    }
}
