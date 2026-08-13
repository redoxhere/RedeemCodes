package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreateCodeGUI {
    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8✨ ᴄʀᴇᴀᴛᴇ ᴄᴏᴅᴇ");

    public static void open(Player player, Main plugin) {
        GUIHolder holder = new GUIHolder("CREATE_CODE_GUI");
        Inventory inv = Bukkit.createInventory(holder, 27, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(GUI_TITLE));
        holder.setInventory(inv);

        // New Code Button
        inv.setItem(11, HeadManager.getHead("CREATE", "§x§F§B§C§8§C§8C§x§F§B§C§E§C§Er§x§F§C§D§4§D§4e§x§F§C§D§A§D§Aa§x§F§D§E§0§E§0t§x§F§D§E§7§E§7e §x§F§E§E§D§E§DC§x§F§E§F§3§F§3o§x§F§F§F§9§F§9d§x§F§F§F§F§F§Fe", "§7ᴄʀᴇᴀᴛᴇ ᴀ ɴᴇᴡ ᴄᴏᴅᴇ"));
        
        // Copy Code Button
        inv.setItem(15, HeadManager.getHead("ADMIN", "§x§D§5§F§F§F§9C§x§D§1§F§F§F§9o§x§C§E§F§F§F§8p§x§C§A§F§F§F§8y §x§C§6§F§F§F§7C§x§C§2§F§F§F§7o§x§B§F§F§F§F§7d§x§B§B§F§F§F§6e", "§7ᴄᴏᴘʏ ᴀɴ ᴇxɪꜱᴛɪɴɢ ᴄᴏᴅᴇ"));

        // Back Button
        inv.setItem(22, HeadManager.getHead("BACK", ChatColor.RED + "Go Back", ChatColor.GRAY + "ʀᴇᴛᴜʀɴ ᴛᴏ ᴄᴏᴅᴇꜱ ʟɪꜱᴛ"));

        GUIUtils.applyFlags(inv);

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, Main plugin) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (itemName.equals("Create Code")) {
                SoundUtil.playClick(plugin, player);
                plugin.getDuplicationHandler().startCodeCreation(player);
            } else if (itemName.equals("Copy Code")) {
                SoundUtil.playClick(plugin, player);
                SelectCodeListGUI selectGUI = new SelectCodeListGUI(plugin);
                plugin.openSelectCodeGUIs.put(player, selectGUI);
                selectGUI.open(player);
            } else if (itemName.equals("Go Back")) {
                SoundUtil.playClick(plugin, player);
                player.closeInventory();
                CodesListGUI gui = (CodesListGUI) plugin.openCodeGUIs.get(player);
                if (gui == null) {
                    gui = new CodesListGUI(plugin);
                    plugin.openCodeGUIs.put(player, gui);
                }
                gui.open(player);
            }
        }
    }
}
