package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

public class PAPIUtil {
    public static String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
