package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

public class MessageUtil {

    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String text) {
        if (text == null) return "";
        
        if (com.cryptomorin.xseries.reflection.XReflection.supports(1, 16)) {
            return ChatColor.translateAlternateColorCodes('&', text);
        }
        
        // --- 1.8.8 CUSTOM THEME REWRITE ---
        
        // 1. Remove all modern hex colors completely
        text = text.replaceAll("(?i)[§&]x([§&][0-9a-f]){6}", "");
        
        // 2. Strip standard color codes to start with a blank slate
        text = text.replaceAll("(?i)[§&][0-9a-fk-or]", "");
        
        // 3. Remove weird symbols, emojis, and piping
        text = text.replace("✏", "").replace("🛡", "").replace("🎒", "").replace("§l| ", "")
                   .replace("| ", "").replace("§l", "").replace("📜", "").replace("🎁", "")
                   .replace("📅", "").replace("⭐", "").replace("❌", "").replace("✅", "")
                   .replace("⚠️", "").replace("🔊", "").replace("»", "").replace("«", "").trim();

        // 4. Convert small caps to normal caps
        text = text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D")
                   .replace("ᴇ", "E").replace("ꜰ", "F").replace("ɢ", "G").replace("ʜ", "H")
                   .replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L")
                   .replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P")
                   .replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T")
                   .replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W")
                   .replace("ʏ", "Y").replace("ᴢ", "Z");
                   
        // 5. Apply new beautiful legacy colors
        if (text.isEmpty()) return "";
        
        String lower = text.toLowerCase();
        
        // Left Click / Right Click split
        if (lower.contains("left click: ") && lower.contains("right click: ")) {
            return "§eLeft Click: §c-1 §8| §eRight Click: §a+1";
        }
        
        // Instructions
        if (lower.startsWith("click to") || lower.startsWith("left click to") || lower.startsWith("shift +") || lower.startsWith("type 'cancel'")) {
            return "§e" + text; // Yellow instructions
        }
        
        // Key-Value pairs
        if (text.contains(": ")) {
            String[] parts = text.split(": ", 2);
            return "§7" + parts[0] + ": §f" + parts[1]; // Gray Key, White Value
        }
        
        // Titles and Item Names
        if (text.length() <= 30 && !text.contains("  ")) {
           return "§6§l" + text; // Gold Bold
        }

        // Default generic lore
        return "§7" + text;
    }


    public static void sendMessage(Main plugin, CommandSender sender, String key) {
        if (sender == null) return;
        String message = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = (Player) sender;
            fullMessage = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, fullMessage);
        }
        sender.sendMessage(color(fullMessage));
    }

    public static void sendRawMessage(Main plugin, CommandSender sender, String message) {
        if (sender == null) return;
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = (Player) sender;
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
                XSound sound = XSound.matchXSound(normalized).orElse(null);
                if (sound != null) {
                    sound.play(player);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }
    }
}
