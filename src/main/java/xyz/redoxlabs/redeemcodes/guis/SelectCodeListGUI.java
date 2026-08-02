package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.CodeExpirationManager;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
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
import org.bukkit.inventory.ItemFlag;

public class SelectCodeListGUI {
   private final Main plugin;
   private List<String> codes;
   private int page = 0;
   private static final int TOTAL_SIZE = 54;
   private static final int COLUMNS = 7;
   private static final int ROW_START = 1;
   private static final int ROW_END = 4;
   private static final int CODES_PER_PAGE = 28;
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8📜 ꜱᴇʟᴇᴄᴛ ᴄᴏᴅᴇ");
   private static final String pro = "§x§2§D§9§D§F§F§l| ";
   private static final String titleColor = "§x§2§D§9§D§F§F";
   private static final String pro_expired = "§x§F§F§7§0§7§0§l| ";
   private static final String titleColor_expired = "§c";

   public SelectCodeListGUI(Main plugin) {
      this.plugin = plugin;
   }

   public void open(Player player) {
      plugin.reloadCodesConfig();
      Set<String> codeKeys = plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false);
      this.codes = new ArrayList<>();

      for(String code : codeKeys) {
         codes.add(code);
      }

      GUIHolder holder = new GUIHolder("SELECT_CODE_LIST");
      Inventory inv = Bukkit.createInventory(holder, 54, GUI_TITLE);
      holder.setInventory(inv);

      ItemStack border = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
      ItemMeta borderMeta = border.getItemMeta();
      if (borderMeta != null) {
         borderMeta.setDisplayName(" ");
         borderMeta.addItemFlags(ItemFlag.values());
      }
      border.setItemMeta(borderMeta);

      for(int start = 0; start < 54; ++start) {
         int end = start / 9;
         int index = start % 9;
         if (end == 0 || end == 5 || index == 0 || index == 8) {
            inv.setItem(start, border);
         }
      }

      int startIndex = page * 28;
      int end = Math.min(startIndex + 28, codes.size());
      int index = 0;

      for(int row = 1; row <= 4 && startIndex + index < end; ++row) {
         for(int col = 1; col <= 7 && startIndex + index < end; ++col) {
            int slot = row * 9 + col;
            inv.setItem(slot, createCodeHead(codes.get(startIndex + index)));
            ++index;
         }
      }

      if (page > 0) {
         inv.setItem(45, HeadManager.getHead("PREV_PAGE", ChatColor.GRAY + "Previous Page"));
      }

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

   private ItemStack createCodeHead(String code) {
      boolean isExpired = plugin.getExpirationManager().isExpired(code);
      String currentPro = isExpired ? "§x§F§F§7§0§7§0§l| " : "§x§2§D§9§D§F§F§l| ";
      String currentTitleColor = isExpired ? "§c" : "§x§2§D§9§D§F§F";
      String currentValueColor = isExpired ? "§e" : "§b";
      String headKey = isExpired ? "EXPIRED_CODE_ITEM" : "CODE_ITEM";
      ItemStack head = HeadManager.getHead(headKey, currentTitleColor + code.toUpperCase());
      ItemMeta meta = head.getItemMeta();
      if (meta != null) {
         List<String> lore = new ArrayList<>();
         String gray = ChatColor.GRAY.toString();
         lore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇʟᴇᴄᴛ ᴛʜɪꜱ ᴄᴏᴅᴇ ꜰᴏʀ ᴅᴜᴘʟɪᴄᴀᴛɪᴏɴ");
         lore.add("");
         lore.add(currentPro + gray + "ᴇɴᴀʙʟᴇᴅ: " + (plugin.getCodesConfig().getBoolean("Codes." + code + ".enabled", true) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
         lore.add(currentPro + gray + "ᴘᴇʀᴍɪꜱꜱɪᴏɴ ʀᴇQᴜɪʀᴇᴅ: " + (plugin.getCodesConfig().getBoolean("Codes." + code + ".permisson.required", false) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
         lore.add(currentPro + gray + "ᴄᴏᴏʟᴅᴏᴡɴ: " + currentValueColor + plugin.getCodesConfig().getInt("Codes." + code + ".redeem-limit.Cooldown", 0) + " min");
         lore.add(currentPro + gray + "ʀᴇᴅᴇᴇᴍ ᴛʏᴘᴇ: " + currentValueColor + plugin.getCodesConfig().getString("Codes." + code + ".redeem-limit.Type", "PLAYER"));
         lore.add(currentPro + gray + "ʀᴇᴅᴇᴇᴍ ʟɪᴍɪᴛ: " + currentValueColor + plugin.getCodesConfig().getInt("Codes." + code + ".redeem-limit.Count", 1));
         meta.setLore(lore);
         meta.addItemFlags(ItemFlag.values());
         head.setItemMeta(meta);
      }

      return head;
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      event.setCancelled(true);
      ItemStack clickedItem = event.getCurrentItem();
      if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
         String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
         if (itemName.equals("Go Back")) {
            SoundUtil.playClick(plugin, player);
            player.closeInventory();
            AdminPanelGUI.open(player, plugin);
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
               plugin.openSelectCodeGUIs.put(player, this);
               plugin.getDuplicationHandler().startDuplication(player, matchedCode);
            }
         }
      }
   }
}




