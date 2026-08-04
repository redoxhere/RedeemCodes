package xyz.redoxlabs.redeemcodes.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;

import java.util.List;

public class GUIUtils {

    public static void applyFlags(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
        }
    }

    public static void fillBorder(Inventory inv) {
        fillBorder(inv, XMaterial.BLUE_STAINED_GLASS_PANE);
    }

    public static void fillBorder(Inventory inv, XMaterial borderMaterial) {
        ItemStack border = borderMaterial.parseItem();
        if (border == null) border = new ItemStack(org.bukkit.Material.DIRT);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
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

    public static ItemStack createItem(XMaterial material, String name, List<String> lore) {
        ItemStack item = material.parseItem();
        if (item == null) item = new ItemStack(org.bukkit.Material.DIRT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(MessageUtil.format(name));
            if (lore != null) {
                List<String> formattedLore = new java.util.ArrayList<>();
                for (String l : lore) formattedLore.add(MessageUtil.format(l));
                meta.setLore(formattedLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
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
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            head.setItemMeta(meta);
        }

        return head;
    }
}
