package xyz.redoxlabs.redeemcodes.utils;

import xyz.redoxlabs.redeemcodes.Main;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

public class SoundUtil {
    private static XSound getSound(Main plugin, String path, XSound defaultSound) {
        String soundName = plugin.getConfig().getString("sounds." + path);
        if (soundName != null) {
            try {
                return XSound.matchXSound(soundName.toUpperCase()).orElse(defaultSound);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound in config: " + soundName);
            }
        }
        return defaultSound;
    }

    public static void playClick(Main plugin, Player player) {
        getSound(plugin, "click", XSound.UI_BUTTON_CLICK).play(player);
    }

    public static void playPageTurn(Main plugin, Player player) {
        getSound(plugin, "page-turn", XSound.ITEM_BOOK_PAGE_TURN).play(player);
    }

    public static void playError(Main plugin, Player player) {
        getSound(plugin, "error", XSound.ENTITY_VILLAGER_NO).play(player);
    }

    public static void playClose(Main plugin, Player player) {
        getSound(plugin, "close", XSound.BLOCK_CHEST_CLOSE).play(player);
    }

    public static void playSuccess(Main plugin, Player player) {
        getSound(plugin, "success", XSound.ENTITY_PLAYER_LEVELUP).play(player);
    }
}
