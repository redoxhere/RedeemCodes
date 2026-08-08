package xyz.redoxlabs.redeemcodes.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.List;
import java.util.ArrayList;

public class RedeemLimitGUI {
   private final Main plugin;
   private final String codeName;
   private final CodeEditorGUI parentGUI;
   private final Inventory inv;

   public RedeemLimitGUI(Main plugin, String codeName, CodeEditorGUI parentGUI) {
      this.plugin = plugin;
      this.codeName = codeName;
      this.parentGUI = parentGUI;
      this.inv = Bukkit.createInventory(new GUIHolder("REDEEM_LIMIT_GUI"), 27, "Redeem Limit - " + codeName.toUpperCase());
      setupInventory();
   }

   private void setupInventory() {
      FileConfiguration config = plugin.getCodesConfig();
      int playerLimit = config.getInt("Codes." + codeName + ".redeem-limit.player", 1);
      int ipLimit = config.getInt("Codes." + codeName + ".redeem-limit.ip", 1);
      int globalLimit = config.getInt("Codes." + codeName + ".redeem-limit.global", -1);

      inv.setItem(11, HeadManager.getHead("LIMIT_PLAYER", net.md_5.bungee.api.ChatColor.of("#FFB472") + "Player Limit", 
         ChatColor.GRAY + "ʟᴇꜰᴛ ᴄʟɪᴄᴋ: -1 | ʀɪɢʜᴛ ᴄʟɪᴄᴋ: +1", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + playerLimit
      ));

      inv.setItem(13, HeadManager.getHead("LIMIT_IP", net.md_5.bungee.api.ChatColor.of("#B6B3B0") + "IP Limit", 
         ChatColor.GRAY + "ʟᴇꜰᴛ ᴄʟɪᴄᴋ: -1 | ʀɪɢʜᴛ ᴄʟɪᴄᴋ: +1", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + ipLimit
      ));

      inv.setItem(15, HeadManager.getHead("LIMIT_GLOBAL", net.md_5.bungee.api.ChatColor.of("#3AB8FB") + "Global Limit", 
         ChatColor.GRAY + "ʟᴇꜰᴛ ᴄʟɪᴄᴋ: -1 | ʀɪɢʜᴛ ᴄʟɪᴄᴋ: +1", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + globalLimit
      ));

      inv.setItem(22, HeadManager.getHead("BACK", "§cGo Back", "§7ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ʙᴀᴄᴋ ᴛᴏ ᴇᴅɪᴛᴏʀ"));

      for (int i = 0; i < inv.getSize(); i++) {
         ItemStack item = inv.getItem(i);
         if (item != null && item.hasItemMeta()) {
             ItemMeta meta = item.getItemMeta();
             meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
             item.setItemMeta(meta);
         }
      }
   }

   public void open(Player player) {
      player.openInventory(inv);
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      event.setCancelled(true);
      if (event.getClickedInventory() != null && event.getClickedInventory().equals(inv)) {
         ItemStack clicked = event.getCurrentItem();
         if (clicked != null && clicked.hasItemMeta() && !clicked.getItemMeta().getDisplayName().isEmpty()) {
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            SoundUtil.playClick(plugin, player);

            if (name.equals("Go Back")) {
               player.closeInventory();
               plugin.openLimitGUIs.remove(player);
               if (parentGUI != null) {
                  plugin.openEditorGUIs.put(player, parentGUI);
                  parentGUI.open(player);
               }
            } else {
               String configKey = null;
               int current = -1;
               
               if (name.equals("Player Limit")) {
                  configKey = "player";
               } else if (name.equals("IP Limit")) {
                  configKey = "ip";
               } else if (name.equals("Global Limit")) {
                  configKey = "global";
               }

               if (configKey != null) {
                  current = plugin.getCodesConfig().getInt("Codes." + codeName + ".redeem-limit." + configKey, 1);
                  
                  if (event.isRightClick()) {
                     current = (current == -1) ? 1 : current + 1;
                  } else if (event.isLeftClick()) {
                     current = current - 1;
                     if (current < -1) current = -1;
                     if (current == 0) current = -1;
                  }
                  
                  plugin.getCodesConfig().set("Codes." + codeName + ".redeem-limit." + configKey, current);
                  plugin.saveCodesConfig();
                  

                  setupInventory();
                  open(player);
               }
            }
         }
      }
   }
}
