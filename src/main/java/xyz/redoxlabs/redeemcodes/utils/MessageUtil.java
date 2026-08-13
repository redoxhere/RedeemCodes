package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

public class MessageUtil {

    private static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null) return "";
        java.util.regex.Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            try {
                matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
            } catch (NoSuchMethodError e) {
                // Pre-1.16 fallback
                matcher.appendReplacement(buffer, "&" + getClosestLegacyColor(matcher.group(1)));
            }
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String format(String text) {
        if (text == null) return "";
        
        if (com.cryptomorin.xseries.reflection.XReflection.supports(1, 16)) {
            return ChatColor.translateAlternateColorCodes('&', text);
        }
        

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)[§&]x([§&][0-9a-f]){6}").matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String match = m.group();
            String hex = match.replaceAll("[§&xX]", "");
            m.appendReplacement(sb, "§" + getClosestLegacyColor(hex));
        }
        m.appendTail(sb);
        text = sb.toString();
        
        text = text.replace("✏", "").replace("🛡", "").replace("🎒", "").replace("§l| ", "")
                   .replace("| ", "").replace("§l", "").replace("📜", "").replace("🎁", "")
                   .replace("📅", "").replace("⭐", "").replace("❌", "").replace("✅", "")
                   .replace("⚠️", "").replace("🔊", "").replace("»", "").replace("«", "").trim();

        text = text.replace("ᴀ", "A").replace("ʙ", "B").replace("ᴄ", "C").replace("ᴅ", "D")
                   .replace("ᴇ", "E").replace("ꜰ", "F").replace("ɢ", "G").replace("ʜ", "H")
                   .replace("ɪ", "I").replace("ᴊ", "J").replace("ᴋ", "K").replace("ʟ", "L")
                   .replace("ᴍ", "M").replace("ɴ", "N").replace("ᴏ", "O").replace("ᴘ", "P")
                   .replace("ʀ", "R").replace("ꜱ", "S").replace("ᴛ", "T")
                   .replace("ᴜ", "U").replace("ᴠ", "V").replace("ᴡ", "W")
                   .replace("ʏ", "Y").replace("ᴢ", "Z");

        return ChatColor.translateAlternateColorCodes('&', text);
    }


    public static String getClosestLegacyColor(String hex) {
        if (hex == null || hex.length() != 6) return "f";
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        
        int[] legacyR = {0, 0, 0, 0, 170, 170, 255, 170, 85, 85, 85, 85, 255, 255, 255, 255};
        int[] legacyG = {0, 0, 170, 170, 0, 0, 170, 170, 85, 85, 255, 255, 85, 85, 255, 255};
        int[] legacyB = {0, 170, 0, 170, 0, 170, 0, 170, 85, 255, 85, 255, 85, 255, 85, 255};
        String codes = "0123456789abcdef";
        
        int minDist = Integer.MAX_VALUE;
        char best = 'f';
        for (int i = 0; i < 16; i++) {
            int dr = r - legacyR[i];
            int dg = g - legacyG[i];
            int db = b - legacyB[i];
            int dist = dr*dr + dg*dg + db*db;
            if (dist < minDist) {
                minDist = dist;
                best = codes.charAt(i);
            }
        }
        return String.valueOf(best);
    }

    public static void sendMessage(Main plugin, CommandSender sender, String key) {
        if (sender == null) return;
        String message = plugin.getMessagesConfig().getString(key, "&cMessage not found: " + key);
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = (Player) sender;
            fullMessage = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, fullMessage);
        }
        sendInteractiveMessage(sender, fullMessage);
    }

    public static void sendRawMessage(Main plugin, CommandSender sender, String message) {
        if (sender == null) return;
        String fullMessage = plugin.getPrefix() + message;
        if (sender instanceof Player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = (Player) sender;
            fullMessage = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, fullMessage);
        }
        sendInteractiveMessage(sender, fullMessage);
    }

    public static void sendMenuMessage(Main plugin, CommandSender sender, String message) {
        if (sender == null) return;
        if (sender instanceof Player && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Player player = (Player) sender;
            message = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, message);
        }
        sendInteractiveMessage(sender, message);
    }

    public static void sendInteractiveMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        if (!(sender instanceof Player)) {
            String raw = message.replaceAll("<hover:[^>]+>", "").replaceAll("</hover>", "")
                                .replaceAll("<click:[^>]+>", "").replaceAll("</click>", "");
            sender.sendMessage(color(raw));
            return;
        }

        Player player = (Player) sender;
        player.spigot().sendMessage(parseInteractive(message));
    }

    public static net.md_5.bungee.api.chat.BaseComponent[] parseInteractive(String text) {
        java.util.List<net.md_5.bungee.api.chat.BaseComponent> components = new java.util.ArrayList<>();
        String hoverText = null;
        net.md_5.bungee.api.chat.ClickEvent clickEvent = null;

        java.util.regex.Pattern tagPattern = java.util.regex.Pattern.compile("<(hover|/hover|click|/click)(?::([^>]+))?>");
        java.util.regex.Matcher matcher = tagPattern.matcher(text);
        
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String plain = color(text.substring(lastEnd, matcher.start()));
                for (net.md_5.bungee.api.chat.BaseComponent comp : net.md_5.bungee.api.chat.TextComponent.fromLegacyText(plain)) {
                    if (hoverText != null) {
                        comp.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(color(hoverText))));
                    }
                    if (clickEvent != null) {
                        comp.setClickEvent(clickEvent);
                    }
                    components.add(comp);
                }
            }
            
            String tag = matcher.group(1);
            String value = matcher.group(2);
            
            if (tag.equals("hover")) {
                hoverText = value != null ? value.replace("\\n", "\n") : "";
            } else if (tag.equals("/hover")) {
                hoverText = null;
            } else if (tag.equals("click")) {
                if (value != null) {
                    String[] parts = value.split(":", 2);
                    if (parts.length == 2) {
                        try {
                            net.md_5.bungee.api.chat.ClickEvent.Action action = net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(parts[0].toUpperCase());
                            clickEvent = new net.md_5.bungee.api.chat.ClickEvent(action, parts[1]);
                        } catch (Exception e) {}
                    }
                }
            } else if (tag.equals("/click")) {
                clickEvent = null;
            }
            
            lastEnd = matcher.end();
        }
        
        if (lastEnd < text.length()) {
            String plain = color(text.substring(lastEnd));
            for (net.md_5.bungee.api.chat.BaseComponent comp : net.md_5.bungee.api.chat.TextComponent.fromLegacyText(plain)) {
                if (hoverText != null) {
                    comp.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(color(hoverText))));
                }
                if (clickEvent != null) {
                    comp.setClickEvent(clickEvent);
                }
                components.add(comp);
            }
        }
        
        return components.toArray(new net.md_5.bungee.api.chat.BaseComponent[0]);
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
