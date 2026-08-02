package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;

import java.util.List;

public class GUIUtils {

    /**
     * Applies all item flags to hide attributes, enchants, etc. for all items in the inventory.
     */
    public static void applyFlags(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.values());
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Fills the top and bottom rows, and left and right columns with a border pane.
     * Default border uses BLUE_STAINED_GLASS_PANE. Use the overloaded method for custom borders.
     */
    public static void fillBorder(Inventory inv) {
        fillBorder(inv, Material.BLUE_STAINED_GLASS_PANE);
    }

    public static void fillBorder(Inventory inv, Material borderMaterial) {
        ItemStack border = new ItemStack(borderMaterial);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderMeta.addItemFlags(ItemFlag.values());
            border.setItemMeta(borderMeta);
        }

        int size = inv.getSize();
        int rows = size / 9;

        for (int i = 0; i < size; ++i) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                inv.setItem(i, border);
            }
        }
    }

    /**
     * Creates a standard item with a name and lore.
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a custom head with the given skin data, name, and lore using HeadManager.
     */
    public static ItemStack createCodeHead(Main plugin, String code) {
        boolean isExpired = plugin.getExpirationManager().isExpired(code);
        String currentPro = isExpired ? "§x§F§F§7§0§7§0§l| " : "§x§2§D§9§D§F§F§l| ";
        String currentTitleColor = isExpired ? "§c" : "§x§2§D§9§D§F§F";
        String currentValueColor = isExpired ? "§e" : "§b";
        String headKey = isExpired ? "EXPIRED_CODE_ITEM" : "CODE_ITEM";
        ItemStack head = HeadManager.getHead(headKey, currentTitleColor + code.toUpperCase());
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            List<String> lore = new java.util.ArrayList<>();
            String gray = org.bukkit.ChatColor.GRAY.toString();
            lore.add("");
            lore.add(currentPro + gray + "ᴇɴᴀʙʟᴇᴅ: " + (plugin.getCodesConfig().getBoolean("Codes." + code + ".enabled", true) ? org.bukkit.ChatColor.GREEN + "Yes" : org.bukkit.ChatColor.RED + "No"));
            lore.add(currentPro + gray + "ᴘᴇʀᴍɪꜱꜱɪᴏɴ ʀᴇQᴜɪʀᴇᴅ: " + (plugin.getCodesConfig().getBoolean("Codes." + code + ".permisson.required", false) ? org.bukkit.ChatColor.GREEN + "Yes" : org.bukkit.ChatColor.RED + "No"));
            lore.add(currentPro + gray + "ᴄᴏᴏʟᴅᴏᴡɴ: " + currentValueColor + plugin.getCodesConfig().getInt("Codes." + code + ".redeem-limit.Cooldown", 0) + " min");
            lore.add(currentPro + gray + "ʀᴇᴅᴇᴇᴍ ᴛʏᴘᴇ: " + currentValueColor + plugin.getCodesConfig().getString("Codes." + code + ".redeem-limit.Type", "PLAYER"));
            lore.add(currentPro + gray + "ʀᴇᴅᴇᴇᴍ ʟɪᴍɪᴛ: " + currentValueColor + plugin.getCodesConfig().getInt("Codes." + code + ".redeem-limit.Count", 1));
            if (isExpired) {
                lore.add(currentPro + "§cEXPIRED");
            }

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            head.setItemMeta(meta);
        }

        return head;
    }
}
