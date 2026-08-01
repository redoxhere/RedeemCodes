package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExpiredCodesListGUI {
   private final Main plugin;
   private final CodesListGUI parentGUI;
   private List<String> codes;
   private int page = 0;
   private static final int CODES_PER_PAGE = 28;
   private static final String pro = "§x§F§F§7§0§7§0§l| ";
   private static final String titleColor = "§c";

   public ExpiredCodesListGUI(Main plugin, CodesListGUI parentGUI) {
      this.plugin = plugin;
      this.parentGUI = parentGUI;
   }

   public void open(Player player) {
      this.codes = new ArrayList(plugin.getExpirationManager().getExpiredCodes());
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "§cExpired Redeem Codes");
      ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
      ItemMeta borderMeta = border.getItemMeta();
      if (borderMeta != null) {
         borderMeta.setDisplayName(" ");
      }

      border.setItemMeta(borderMeta);

      for(int start = 0; start < 54; ++start) {
         int end = start / 9;
         int index = start % 9;
         if (end == 0 || end == 5 || index == 0 || index == 8) {
            inv.setItem(start, border);
         }
      }

      int start = page * 28;
      int end = Math.min(start + 28, codes.size());
      int index = 0;

      for(int row = 1; row <= 4; ++row) {
         for(int col = 1; col <= 7 && start + index < end; ++col) {
            int slot = row * 9 + col;
            inv.setItem(slot, createCodeHead((String)codes.get(start + index)));
            ++index;
         }
      }

      if (page > 0) {
         inv.setItem(45, HeadManager.getHead("PREV_PAGE", "§7Previous Page"));
      }

      inv.setItem(49, HeadManager.getHead("BACK", "§cGo Back", "§7Click to go back to code list"));
      if (end < codes.size()) {
         inv.setItem(53, HeadManager.getHead("NEXT_PAGE", "§7Next Page"));
      }

      ItemStack pageDisplay = new ItemStack(Material.PAPER);
      ItemMeta pageMeta = pageDisplay.getItemMeta();
      if (pageMeta != null) {
         pageMeta.setDisplayName("Page: §c" + (page + 1) + "/" + ((codes.size() - 1) / 28 + 1));
         pageDisplay.setItemMeta(pageMeta);
      }

      inv.setItem(48, pageDisplay);
      player.openInventory(inv);
   }

   private ItemStack createCodeHead(String code) {
      ItemStack head = HeadManager.getHead("EXPIRED_CODE_ITEM", "§c" + code);
      ItemMeta meta = head.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>();
         lore.add("§7ᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ ᴀɴᴅ ʀᴇᴀᴄᴛɪᴠᴀᴛᴇ");
         meta.setLore(lore);
         head.setItemMeta(meta);
      }

      return head;
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      if (event.getView().getTitle().contains("Expired Redeem Codes")) {
         event.setCancelled(true);
         ItemStack clickedItem = event.getCurrentItem();
         if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            if (itemName.equals("Go Back")) {
               player.closeInventory();
               plugin.openEditorGUIs.remove(player);
               if (parentGUI != null) {
                  plugin.openCodeGUIs.put(player, parentGUI);
                  parentGUI.open(player);
               }
            } else if (itemName.equals("Next Page")) {
               if ((page + 1) * 28 < codes.size()) {
                  ++page;
                  open(player);
               }
            } else if (itemName.equals("Previous Page")) {
               if (page > 0) {
                  --page;
                  open(player);
               }
            } else if (codes.contains(itemName)) {
               CodeEditorGUI editor = new CodeEditorGUI(plugin, itemName, parentGUI);
               plugin.openEditorGUIs.put(player, editor);
               editor.open(player);
            }

         }
      }
   }
}




