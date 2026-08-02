package xyz.redoxlabs.redeemcodes.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.CreateCodeHandler;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class MainGUI {
   private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&8🛠 ᴍᴀɪɴ ᴍᴇɴᴜ");

   public static void open(Player player) {
      GUIHolder holder = new GUIHolder("MAIN_GUI");
      Inventory inv = Bukkit.createInventory(holder, 27, GUI_TITLE);
      holder.setInventory(inv);

      inv.setItem(11, HeadManager.getHead("CREATE", "§x§F§B§C§8§C§8C§x§F§B§C§E§C§Er§x§F§C§D§4§D§4e§x§F§C§D§A§D§Aa§x§F§D§E§0§E§0t§x§F§D§E§7§E§7e §x§F§E§E§D§E§DC§x§F§E§F§3§F§3o§x§F§F§F§9§F§9d§x§F§F§F§F§F§Fe", "", "§x§F§B§8§E§2§F§l| §x§F§B§D§A§3§Bᴄ§x§F§B§D§C§3§Fʀ§x§F§B§D§D§4§4ᴇ§x§F§C§D§F§4§8ᴀ§x§F§C§E§1§4§Cᴛ§x§F§C§E§2§5§1ᴇ §x§F§C§E§4§5§5ᴀ §x§F§C§E§5§5§9ɴ§x§F§D§E§7§5§Eᴇ§x§F§D§E§9§6§2ᴡ §x§F§D§E§A§6§6ʀ§x§F§D§E§C§6§Aᴇ§x§F§E§E§E§6§Fᴅ§x§F§E§E§F§7§3ᴇ§x§F§E§F§1§7§7ᴇ§x§F§E§F§2§7§Cᴍ §x§F§E§F§4§8§0ᴄ§x§F§F§F§6§8§4ᴏ§x§F§F§F§7§8§9ᴅ§x§F§F§F§9§8§Dᴇ", "§x§F§B§8§E§2§F§l| §x§F§B§D§A§3§Bᴄ§x§F§B§D§B§3§Eᴏ§x§F§B§D§C§4§1ɴ§x§F§B§D§E§4§4ꜰ§x§F§C§D§F§4§8ɪ§x§F§C§E§0§4§Bɢ§x§F§C§E§1§4§Eᴜ§x§F§C§E§2§5§1ʀ§x§F§C§E§4§5§4ᴇ §x§F§C§E§5§5§7ᴅ§x§F§D§E§6§5§Bᴇ§x§F§D§E§7§5§Eᴛ§x§F§D§E§8§6§1ᴀ§x§F§D§E§A§6§4ɪ§x§F§D§E§B§6§7ʟ§x§F§D§E§C§6§Aꜱ §x§F§D§E§D§6§Dɪ§x§F§E§E§E§7§1ɴ §x§F§E§E§F§7§4ᴄ§x§F§E§F§1§7§7ᴏ§x§F§E§F§2§7§Aᴅ§x§F§E§F§3§7§Dᴇ§x§F§E§F§4§8§0ꜱ§x§F§F§F§5§8§4.§x§F§F§F§7§8§7ʏ§x§F§F§F§8§8§Aᴍ§x§F§F§F§9§8§Dʟ"));
      inv.setItem(13, HeadManager.getHead("LIST", "§x§F§B§2§9§5§EC§x§F§C§3§1§6§9o§x§F§C§3§9§7§3d§x§F§D§4§1§7§Ee§x§F§D§4§A§8§9s §x§F§E§5§2§9§3L§x§F§E§5§A§9§Ei§x§F§F§6§2§A§8s§x§F§F§6§A§B§3t", "", "§x§F§B§8§E§2§F§l| §x§F§B§C§F§3§Eᴠ§x§F§B§D§1§4§0ɪ§x§F§B§D§3§4§1ᴇ§x§F§C§D§5§4§3ᴡ §x§F§C§D§7§4§5ᴀ§x§F§C§D§A§4§7ʟ§x§F§C§D§C§4§8ʟ §x§F§C§D§E§4§Aᴇ§x§F§D§E§0§4§Cx§x§F§D§E§2§4§Eɪ§x§F§D§E§4§4§Fꜱ§x§F§D§E§6§5§1ᴛ§x§F§E§E§8§5§3ɪ§x§F§E§E§A§5§5ɴ§x§F§E§E§C§5§6ɢ §x§F§E§E§F§5§8ᴄ§x§F§E§F§1§5§Aᴏ§x§F§F§F§3§5§Cᴅ§x§F§F§F§5§5§Dᴇ§x§F§F§F§7§5§Fꜱ", "§x§F§B§8§E§2§F§l| §x§F§B§C§F§3§Eᴄ§x§F§B§D§2§4§0ʟ§x§F§C§D§4§4§2ɪ§x§F§C§D§7§4§4ᴄ§x§F§C§D§9§4§6ᴋ §x§F§C§D§C§4§8ᴏ§x§F§D§D§E§4§Aɴ §x§F§D§E§1§4§Cᴛ§x§F§D§E§3§4§Fʜ§x§F§D§E§6§5§1ᴇ§x§F§E§E§8§5§3ᴍ §x§F§E§E§B§5§5ᴛ§x§F§E§E§D§5§7ᴏ §x§F§E§F§0§5§9ᴇ§x§F§F§F§2§5§Bᴅ§x§F§F§F§5§5§Dɪ§x§F§F§F§7§5§Fᴛ"));
      inv.setItem(15, HeadManager.getHead("ADMIN", ChatColor.GOLD + "§x§C§A§E§1§F§BA§x§C§5§D§D§F§Bd§x§C§0§D§A§F§Cm§x§B§B§D§6§F§Ci§x§B§6§D§2§F§Dn §x§B§2§C§F§F§DP§x§A§D§C§B§F§Ea§x§A§8§C§7§F§En§x§A§3§C§4§F§Fe§x§9§E§C§0§F§Fl", "", "§x§F§B§8§E§2§F§l| §x§F§B§C§F§3§Eᴀ§x§F§B§D§2§4§0ᴅ§x§F§C§D§5§4§3ᴍ§x§F§C§D§8§4§5ɪ§x§F§C§D§A§4§7ɴ§x§F§C§D§D§4§A-§x§F§D§E§0§4§Cᴏ§x§F§D§E§3§4§Fɴ§x§F§D§E§6§5§1ʟ§x§F§E§E§9§5§3ʏ §x§F§E§E§C§5§6ᴛ§x§F§E§E§E§5§8ᴏ§x§F§E§F§1§5§Aᴏ§x§F§F§F§4§5§Dʟ§x§F§F§F§7§5§Fꜱ", "§x§F§B§8§E§2§F§l| §x§F§F§F§3§1§Eᴄ§x§F§F§E§E§1§Fʟ§x§F§F§E§9§2§0ɪ§x§F§F§E§5§2§1ᴄ§x§F§F§E§0§2§2ᴋ §x§F§F§D§B§2§4ᴛ§x§F§F§D§6§2§5ᴏ §x§F§F§D§1§2§6ᴏ§x§F§F§C§D§2§7ᴘ§x§F§F§C§8§2§8ᴇ§x§F§F§C§3§2§9ɴ §x§F§F§B§E§2§Aᴀ§x§F§F§B§9§2§Bᴅ§x§F§F§B§5§2§Cᴍ§x§F§F§B§0§2§Dɪ§x§F§F§A§B§2§Fɴ §x§F§F§A§6§3§0ᴘ§x§F§F§A§1§3§1ᴀ§x§F§F§9§D§3§2ɴ§x§F§F§9§8§3§3ᴇ§x§F§F§9§3§3§4ʟ"));

      GUIUtils.applyFlags(inv);

      player.openInventory(inv);
   }

   public static void handleClick(InventoryClickEvent event, Main plugin, CreateCodeHandler createHandler) {
      event.setCancelled(true);
      Player player = (Player)event.getWhoClicked();
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().getDisplayName() != null) {
         String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
         if (itemName.equals("Create Code")) {
            SoundUtil.playClick(plugin, player);
            createHandler.startCodeCreation(player);
         } else if (itemName.equals("Codes List")) {
            SoundUtil.playClick(plugin, player);
            CodesListGUI gui = (CodesListGUI)plugin.openCodeGUIs.get(player);
            if (gui == null) {
               gui = new CodesListGUI(plugin);
               plugin.openCodeGUIs.put(player, gui);
            }
            gui.open(player);
         } else if (itemName.equals("Admin Panel")) {
            SoundUtil.playClick(plugin, player);
            AdminPanelGUI.open(player, plugin);
         }
      }
   }
}