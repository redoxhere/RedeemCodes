package xyz.redoxlabs.redeemcodes.utils;

import xyz.redoxlabs.redeemcodes.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {
    private static Sound getSound(Main plugin, String path, Sound defaultSound) {
        String soundName = plugin.getConfig().getString("sounds." + path);
        if (soundName != null) {
            try {
                return Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound in config: " + soundName);
            }
        }
        return defaultSound;
    }

    public static void playClick(Main plugin, Player player) {
        player.playSound(player.getLocation(), getSound(plugin, "click", Sound.UI_BUTTON_CLICK), 1.0f, 1.0f);
    }

    public static void playPageTurn(Main plugin, Player player) {
        player.playSound(player.getLocation(), getSound(plugin, "page-turn", Sound.ITEM_BOOK_PAGE_TURN), 1.0f, 1.0f);
    }

    public static void playError(Main plugin, Player player) {
        player.playSound(player.getLocation(), getSound(plugin, "error", Sound.ENTITY_VILLAGER_NO), 1.0f, 1.0f);
    }

    public static void playClose(Main plugin, Player player) {
        player.playSound(player.getLocation(), getSound(plugin, "close", Sound.BLOCK_CHEST_CLOSE), 1.0f, 1.0f);
    }

    public static void playSuccess(Main plugin, Player player) {
        player.playSound(player.getLocation(), getSound(plugin, "success", Sound.ENTITY_PLAYER_LEVELUP), 1.0f, 1.0f);
    }
}
