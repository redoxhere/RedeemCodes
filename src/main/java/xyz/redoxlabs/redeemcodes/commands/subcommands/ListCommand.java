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
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        FileConfiguration codes = plugin.getCodesConfig();
        Set<String> codeNames = codes.getConfigurationSection("Codes").getKeys(false);
        player.sendMessage(plugin.color("&d---- &bList of Existing Codes &d----"));

        for (String code : codeNames) {
            boolean enabled = codes.getBoolean("Codes." + code + ".enabled", true);
            TextComponent component = new TextComponent(plugin.color((enabled ? "&a" : "&c") + "➤ " + code));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rc show " + code));
            component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click details").create()));
            player.spigot().sendMessage(component);
        }
        return true;
    }
}
