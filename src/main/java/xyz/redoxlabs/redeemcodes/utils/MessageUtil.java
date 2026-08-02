package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

public class MessageUtil {

    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(Main plugin, CommandSender sender, String key) {
        if (sender == null) return;
        String message = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            fullMessage = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, fullMessage);
        }
        sender.sendMessage(color(fullMessage));
    }

    public static void sendRawMessage(Main plugin, CommandSender sender, String message) {
        if (sender == null) return;
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            fullMessage = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, fullMessage);
        }
        sender.sendMessage(color(fullMessage));
    }

    public static void playSound(Main plugin, Player player, String path) {
        if (player == null) return;
        String soundName = plugin.getConfig().getString(path);
        if (soundName != null && !soundName.isEmpty()) {
            try {
                String normalized = soundName.toUpperCase().replace(".", "_");
                Sound sound = Sound.valueOf(normalized);
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }
    }
}
