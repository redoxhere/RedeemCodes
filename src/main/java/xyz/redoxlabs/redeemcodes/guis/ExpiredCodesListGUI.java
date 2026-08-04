package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExpiredCodesListGUI {
   private final Main plugin;
   private final CodesListGUI parentGUI;
   private List<String> codes;
   private int page = 0;
   private static final int CODES_PER_PAGE = 28;
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8⏳ ᴇxᴘɪʀᴇᴅ ᴄᴏᴅᴇꜱ");
   private static final String pro = "§x§F§F§7§0§7§0§l| ";
   private static final String titleColor = "§c";

   public ExpiredCodesListGUI(Main plugin, CodesListGUI parentGUI) {
      this.plugin = plugin;
      this.parentGUI = parentGUI;
   }

   public void open(Player player) {
      this.codes = new ArrayList<>(plugin.getExpirationManager().getExpiredCodes());
      GUIHolder holder = new GUIHolder("EXPIRED_CODES_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(GUI_TITLE));
      holder.setInventory(inv);
      
      GUIUtils.fillBorder(inv, XMaterial.RED_STAINED_GLASS_PANE);

      int start = page * 28;
      int end = Math.min(start + 28, codes.size());
      int index = 0;

      for(int row = 1; row <= 4; ++row) {
         for(int col = 1; col <= 7 && start + index < end; ++col) {
            int slot = row * 9 + col;
            inv.setItem(slot, GUIUtils.createCodeHead(plugin, codes.get(start + index)));
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

      ItemStack pageDisplay = XMaterial.PAPER.parseItem();
      ItemMeta pageMeta = pageDisplay.getItemMeta();
      if (pageMeta != null) {
         int maxPage = codes.isEmpty() ? 1 : (codes.size() - 1) / 28 + 1;
         pageMeta.setDisplayName("Page: §c" + (page + 1) + "/" + maxPage);
         pageDisplay.setItemMeta(pageMeta);
      }
      inv.setItem(48, pageDisplay);

      GUIUtils.applyFlags(inv);

      player.openInventory(inv);
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      event.setCancelled(true);
      ItemStack clickedItem = event.getCurrentItem();
      if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
         String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
         if (itemName.equals("Go Back")) {
            SoundUtil.playClick(plugin, player);
            player.closeInventory();
            plugin.openEditorGUIs.remove(player);
            if (parentGUI != null) {
               plugin.openCodeGUIs.put(player, parentGUI);
               parentGUI.open(player);
            }
         } else if (itemName.equals("Next Page")) {
            if ((page + 1) * 28 < codes.size()) {
               SoundUtil.playPageTurn(plugin, player);
               ++page;
               open(player);
            } else {
               SoundUtil.playError(plugin, player);
            }
         } else if (itemName.equals("Previous Page")) {
            if (page > 0) {
               SoundUtil.playPageTurn(plugin, player);
               --page;
               open(player);
            } else {
               SoundUtil.playError(plugin, player);
            }
         } else {
            String matchedCode = null;
            for (String code : codes) {
               if (code.equalsIgnoreCase(itemName)) {
                  matchedCode = code;
                  break;
               }
            }
            if (matchedCode != null) {
               SoundUtil.playClick(plugin, player);
               CodeEditorGUI editor = new CodeEditorGUI(plugin, matchedCode, parentGUI);
               plugin.openEditorGUIs.put(player, editor);
               editor.open(player);
            }
         }
      }
   }
}




