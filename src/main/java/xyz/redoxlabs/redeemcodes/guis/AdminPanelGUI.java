package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminPanelGUI {
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8🛡 ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ");

   public static void open(Player player, Main plugin) {
      GUIHolder holder = new GUIHolder("ADMIN_PANEL");
      Inventory inv = Bukkit.createInventory(holder, 27, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(GUI_TITLE));
      holder.setInventory(inv);

      inv.setItem(11, createMenuItem(XMaterial.BUNDLE.parseMaterial(), ChatColor.AQUA + "§x§4§5§D§1§5§8S§x§4§7§D§3§5§Ca§x§4§8§D§4§6§0c§x§4§A§D§6§6§3k§x§4§C§D§8§6§7s", "§7ᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ꜱᴀᴄᴋꜱ"));
      inv.setItem(13, createMenuItem(XMaterial.COMMAND_BLOCK.parseMaterial(), ChatColor.AQUA + "§x§6§E§B§1§D§4P§x§6§C§B§2§D§5r§x§6§A§B§4§D§7e§x§6§8§B§5§D§8m§x§6§6§B§7§D§9a§x§6§3§B§8§D§Ad§x§6§1§B§A§D§Ce§x§5§F§B§B§D§Ds", "§7ᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴘʀᴇᴍᴀᴅᴇꜱ"));
      inv.setItem(15, createMenuItem(XMaterial.WRITABLE_BOOK.parseMaterial(), ChatColor.AQUA + "§x§F§F§9§3§6§7S§x§F§F§9§0§6§2e§x§F§F§8§C§5§En§x§F§F§8§9§5§9d §x§F§F§8§6§5§4R§x§F§F§8§2§5§0e§x§F§F§7§F§4§Bv§x§F§F§7§C§4§6i§x§F§F§7§8§4§2e§x§F§F§7§5§3§Dw", "§7ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇɴᴅ ᴀ ʀᴇᴠɪᴇᴡ ᴏʀ ꜰᴇᴇᴅʙᴀᴄᴋ", "§7ʏᴏᴜʀ ᴍᴇꜱꜱᴀɢᴇ ᴡɪʟʟ ʙᴇ ꜱᴇɴᴛ ᴛᴏ ᴛʜᴇ ᴅᴇᴠᴇʟᴏᴘᴇʀꜱ"));
      inv.setItem(22, HeadManager.getHead("BACK", ChatColor.RED + "Go Back", ChatColor.GRAY + "ʀᴇᴛᴜʀɴ ᴛᴏ ᴍᴀɪɴ ᴍᴇɴᴜ"));

      GUIUtils.applyFlags(inv);

      player.openInventory(inv);
   }

   private static ItemStack createMenuItem(Material mat, String name, String... lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(name));
         if (lore != null) {
            java.util.List<String> formattedLore = new java.util.ArrayList<>();
            for (String l : lore) formattedLore.add(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(l));
            meta.setLore(formattedLore);
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
            case "Sacks":
               SoundUtil.playClick(plugin, player);
               GlobalSackListGUI sackGUI = new GlobalSackListGUI(plugin);
               plugin.openGlobalSackGUIs.put(player, sackGUI);
               sackGUI.open(player, 0);
               break;
            case "Premades":
               SoundUtil.playClick(plugin, player);
               GlobalPremadeListGUI premadeGUI = new GlobalPremadeListGUI(plugin);
               plugin.openGlobalPremadeGUIs.put(player, premadeGUI);
               premadeGUI.open(player, 0);
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




