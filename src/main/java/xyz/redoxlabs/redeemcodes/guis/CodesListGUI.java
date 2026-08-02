package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.CodeExpirationManager;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CodesListGUI {
   private final Main plugin;
   private List<String> codes;
   private int page = 0;
   private static final int TOTAL_SIZE = 54;
   private static final int COLUMNS = 7;
   private static final int ROW_START = 1;
   private static final int ROW_END = 4;
   private static final int CODES_PER_PAGE = 28;
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8📜 ᴄᴏᴅᴇꜱ ʟɪꜱᴛ");
   private static final String pro = "§x§2§D§9§D§F§F§l| ";
   private static final String titleColor = "§x§2§D§9§D§F§F";
   private static final String pro_expired = "§x§F§F§7§0§7§0§l| ";
   private static final String titleColor_expired = "§c";

   public CodesListGUI(Main plugin) {
      this.plugin = plugin;
   }

   public void open(Player player) {
      plugin.reloadCodesConfig();
      Set<String> codeKeys = plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false);
      this.codes = new ArrayList<>();
      CodeExpirationManager expManager = plugin.getExpirationManager();

      for(String code : codeKeys) {
         if (!expManager.isExpired(code)) {
            codes.add(code);
         }
      }

      GUIHolder holder = new GUIHolder("CODES_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, GUI_TITLE);
      holder.setInventory(inv);

      GUIUtils.fillBorder(inv, Material.LIGHT_BLUE_STAINED_GLASS_PANE);

      int startIndex = page * 28;
      int end = Math.min(startIndex + 28, codes.size());
      int index = 0;

      for(int row = 1; row <= 4 && startIndex + index < end; ++row) {
         for(int col = 1; col <= 7 && startIndex + index < end; ++col) {
            int slot = row * 9 + col;
            inv.setItem(slot, GUIUtils.createCodeHead(plugin, codes.get(startIndex + index)));
            ++index;
         }
      }

      if (page > 0) {
         inv.setItem(45, HeadManager.getHead("PREV_PAGE", ChatColor.GRAY + "Previous Page"));
      }

      inv.setItem(47, HeadManager.getHead("EXPIRED_LIST", "§x§F§B§F§2§6§DE§x§F§B§F§3§7§4x§x§F§C§F§3§7§Ap§x§F§C§F§4§8§1i§x§F§C§F§5§8§8r§x§F§D§F§6§8§Fe§x§F§D§F§6§9§5d §x§F§E§F§7§9§CC§x§F§E§F§8§A§3o§x§F§E§F§9§A§Ad§x§F§F§F§9§B§0e§x§F§F§F§A§B§7s", "§7ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ᴇxᴘɪʀᴇᴅ ᴄᴏᴅᴇꜱ"));
      inv.setItem(49, HeadManager.getHead("BACK", ChatColor.RED + "Go Back", "§x§A§2§A§2§A§2ᴄ§x§A§5§A§5§A§5ʟ§x§A§8§A§8§A§8ɪ§x§A§A§A§A§A§Aᴄ§x§A§D§A§D§A§Dᴋ §x§B§0§B§0§B§0ᴛ§x§B§3§B§3§B§3ᴏ §x§B§6§B§6§B§6ɢ§x§B§9§B§9§B§9ᴏ §x§B§B§B§B§B§Bʙ§x§B§E§B§E§B§Eᴀ§x§C§1§C§1§C§1ᴄ§x§C§4§C§4§C§4ᴋ §x§C§7§C§7§C§7ᴛ§x§C§9§C§9§C§9ᴏ §x§C§C§C§C§C§Cᴍ§x§C§F§C§F§C§Fᴀ§x§D§2§D§2§D§2ɪ§x§D§5§D§5§D§5ɴ §x§D§8§D§8§D§8ᴍ§x§D§A§D§A§D§Aᴇ§x§D§D§D§D§D§Dɴ§x§E§0§E§0§E§0ᴜ"));
      if (end < codes.size()) {
         inv.setItem(53, HeadManager.getHead("NEXT_PAGE", ChatColor.GRAY + "Next Page"));
      }

      ItemStack pageDisplay = new ItemStack(Material.PAPER);
      ItemMeta pageMeta = pageDisplay.getItemMeta();
      if (pageMeta != null) {
         int maxPage = codes.isEmpty() ? 1 : (codes.size() - 1) / 28 + 1;
         pageMeta.setDisplayName("Page: §x§2§D§9§D§F§F" + (page + 1) + "/" + maxPage);
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
            MainGUI.open(player);
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
         } else if (itemName.equals("Expired Codes")) {
            SoundUtil.playClick(plugin, player);
            ExpiredCodesListGUI expiredGUI = new ExpiredCodesListGUI(plugin, this);
            plugin.openExpiredCodeGUIs.put(player, expiredGUI);
            expiredGUI.open(player);
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
               CodeEditorGUI editor = new CodeEditorGUI(plugin, matchedCode, this);
               plugin.openEditorGUIs.put(player, editor);
               editor.open(player);
            }
         }
      }
   }
}