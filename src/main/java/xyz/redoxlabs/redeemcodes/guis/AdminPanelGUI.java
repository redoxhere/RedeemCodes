package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;

public class AdminPanelGUI {
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8🛡️ ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ");

   public static void open(Player player, Main plugin) {
      GUIHolder holder = new GUIHolder("ADMIN_PANEL");
      Inventory inv = Bukkit.createInventory(holder, 27, GUI_TITLE);
      holder.setInventory(inv);

      inv.setItem(11, createMenuItem(Material.FEATHER, ChatColor.GREEN + "§x§D§5§F§F§F§9D§x§D§1§F§F§F§9u§x§C§E§F§F§F§8p§x§C§A§F§F§F§8l§x§C§6§F§F§F§7i§x§C§2§F§F§F§7c§x§B§F§F§F§F§7a§x§B§B§F§F§F§6t§x§B§7§F§F§F§6e §x§B§3§F§F§F§5C§x§B§0§F§F§F§5o§x§A§C§F§F§F§4d§x§A§8§F§F§F§4e", "", "§x§F§B§8§E§2§F§l| §7§x§F§F§F§3§1§Eᴄ§x§F§F§F§0§1§Fʟ§x§F§F§E§C§2§0ɪ§x§F§F§E§9§2§0ᴄ§x§F§F§E§6§2§1ᴋ §x§F§F§E§2§2§2ᴛ§x§F§F§D§F§2§3ᴏ §x§F§F§D§C§2§3ᴅ§x§F§F§D§9§2§4ᴜ§x§F§F§D§5§2§5ᴘ§x§F§F§D§2§2§6ʟ§x§F§F§C§F§2§6ɪ§x§F§F§C§B§2§7ᴄ§x§F§F§C§8§2§8ᴀ§x§F§F§C§5§2§9ᴛ§x§F§F§C§1§2§9ᴇ §x§F§F§B§E§2§Aᴀ§x§F§F§B§B§2§Bɴ §x§F§F§B§7§2§Cᴇ§x§F§F§B§4§2§Cx§x§F§F§B§1§2§Dɪ§x§F§F§A§D§2§Eꜱ§x§F§F§A§A§2§Fᴛ§x§F§F§A§7§2§Fɪ§x§F§F§A§4§3§0ɴ§x§F§F§A§0§3§1ɢ §x§F§F§9§D§3§2ᴄ§x§F§F§9§A§3§2ᴏ§x§F§F§9§6§3§3ᴅ§x§F§F§9§3§3§4ᴇ", "§x§F§B§8§E§2§F§l| §7§x§F§F§F§3§1§Eꜱ§x§F§F§E§F§1§Fᴇ§x§F§F§E§B§2§0ʟ§x§F§F§E§7§2§1ᴇ§x§F§F§E§4§2§2ᴄ§x§F§F§E§0§2§2ᴛ §x§F§F§D§C§2§3ꜰ§x§F§F§D§8§2§4ʀ§x§F§F§D§4§2§5ᴏ§x§F§F§D§0§2§6ᴍ §x§F§F§C§D§2§7ᴀ§x§F§F§C§9§2§8ʟ§x§F§F§C§5§2§9ʟ §x§F§F§C§1§2§9ᴇ§x§F§F§B§D§2§Ax§x§F§F§B§9§2§Bɪ§x§F§F§B§6§2§Cꜱ§x§F§F§B§2§2§Dᴛ§x§F§F§A§E§2§Eɪ§x§F§F§A§A§2§Fɴ§x§F§F§A§6§3§0ɢ §x§F§F§A§2§3§0ᴄ§x§F§F§9§F§3§1ᴏ§x§F§F§9§B§3§2ᴅ§x§F§F§9§7§3§3ᴇ§x§F§F§9§3§3§4ꜱ"));
      inv.setItem(15, createMenuItem(Material.WRITABLE_BOOK, ChatColor.AQUA + "§x§F§F§9§3§6§7S§x§F§F§9§0§6§2e§x§F§F§8§C§5§En§x§F§F§8§9§5§9d §x§F§F§8§6§5§4R§x§F§F§8§2§5§0e§x§F§F§7§F§4§Bv§x§F§F§7§C§4§6i§x§F§F§7§8§4§2e§x§F§F§7§5§3§Dw", "", "§x§F§B§8§E§2§F§l| §7§x§F§F§F§3§1§Eᴄ§x§F§F§E§F§1§Fʟ§x§F§F§E§C§2§0ɪ§x§F§F§E§8§2§0ᴄ§x§F§F§E§5§2§1ᴋ §x§F§F§E§1§2§2ᴛ§x§F§F§D§E§2§3ᴏ §x§F§F§D§A§2§4ꜱ§x§F§F§D§7§2§5ᴇ§x§F§F§D§3§2§5ɴ§x§F§F§C§F§2§6ᴅ §x§F§F§C§C§2§7ᴀ §x§F§F§C§8§2§8ʀ§x§F§F§C§5§2§9ᴇ§x§F§F§C§1§2§9ᴠ§x§F§F§B§E§2§Aɪ§x§F§F§B§A§2§Bᴇ§x§F§F§B§7§2§Cᴡ §x§F§F§B§3§2§Dᴏ§x§F§F§A§F§2§Dʀ §x§F§F§A§C§2§Eꜰ§x§F§F§A§8§2§Fᴇ§x§F§F§A§5§3§0ᴇ§x§F§F§A§1§3§1ᴅ§x§F§F§9§E§3§2ʙ§x§F§F§9§A§3§2ᴀ§x§F§F§9§7§3§3ᴄ§x§F§F§9§3§3§4ᴋ", "§x§F§B§8§E§2§F§l| §7§x§F§F§F§3§1§Eʏ§x§F§F§F§0§1§Fᴏ§x§F§F§E§E§1§Fᴜ§x§F§F§E§B§2§0ʀ §x§F§F§E§8§2§1ᴍ§x§F§F§E§5§2§1ᴇ§x§F§F§E§3§2§2ꜱ§x§F§F§E§0§2§2ꜱ§x§F§F§D§D§2§3ᴀ§x§F§F§D§A§2§4ɢ§x§F§F§D§8§2§4ᴇ §x§F§F§D§5§2§5ᴡ§x§F§F§D§2§2§6ɪ§x§F§F§C§F§2§6ʟ§x§F§F§C§D§2§7ʟ §x§F§F§C§A§2§7ʙ§x§F§F§C§7§2§8ᴇ §x§F§F§C§4§2§9ꜱ§x§F§F§C§2§2§9ᴇ§x§F§F§B§F§2§Aɴ§x§F§F§B§C§2§Bᴛ §x§F§F§B§9§2§Bᴛ§x§F§F§B§7§2§Cᴏ §x§F§F§B§4§2§Cᴛ§x§F§F§B§1§2§Dʜ§x§F§F§A§E§2§Eᴇ §x§F§F§A§C§2§Eᴅ§x§F§F§A§9§2§Fᴇ§x§F§F§A§6§3§0ᴠ§x§F§F§A§3§3§0ᴇ§x§F§F§A§1§3§1ʟ§x§F§F§9§E§3§1ᴏ§x§F§F§9§B§3§2ᴘ§x§F§F§9§8§3§3ᴇ§x§F§F§9§6§3§3ʀ§x§F§F§9§3§3§4ꜱ"));
      inv.setItem(22, HeadManager.getHead("BACK", ChatColor.RED + "Go Back", ChatColor.GRAY + "ʀᴇᴛᴜʀɴ ᴛᴏ ᴍᴀɪɴ ᴍᴇɴᴜ"));

      for (int i = 0; i < inv.getSize(); i++) {
         ItemStack item = inv.getItem(i);
         if (item != null && item.hasItemMeta()) {
             ItemMeta meta = item.getItemMeta();
             meta.addItemFlags(ItemFlag.values());
             item.setItemMeta(meta);
         }
      }

      player.openInventory(inv);
   }

   private static ItemStack createMenuItem(Material mat, String name, String... lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         if (lore != null) {
            meta.setLore(Arrays.asList(lore));
         }

         item.setItemMeta(meta);
      }

      return item;
   }

   public static void handleClick(InventoryClickEvent event, Main plugin) {
      event.setCancelled(true);
      Player player = (Player)event.getWhoClicked();
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().getDisplayName() != null) {
         switch (ChatColor.stripColor(clicked.getItemMeta().getDisplayName())) {
            case "Duplicate Code":
               SoundUtil.playClick(plugin, player);
               SelectCodeListGUI selectGUI = new SelectCodeListGUI(plugin);
               plugin.openSelectCodeGUIs.put(player, selectGUI);
               selectGUI.open(player);
               break;
            case "Send Review":
               SoundUtil.playClick(plugin, player);
               plugin.getDuplicationHandler().startReviewInput(player);
               break;
            case "Go Back":
               SoundUtil.playClick(plugin, player);
               MainGUI.open(player);
         }
      }
   }
}




