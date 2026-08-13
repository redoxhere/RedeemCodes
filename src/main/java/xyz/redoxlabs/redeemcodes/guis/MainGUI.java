package xyz.redoxlabs.redeemcodes.guis;


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
import com.cryptomorin.xseries.XMaterial;
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
      Inventory inv = Bukkit.createInventory(holder, 27, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(GUI_TITLE));
      holder.setInventory(inv);

      inv.setItem(11, HeadManager.getHead("CREATE", "§x§F§B§C§8§C§8V§x§F§B§C§E§C§Eo§x§F§C§D§4§D§4u§x§F§C§D§A§D§Ac§x§F§D§E§0§E§0h§x§F§D§E§7§E§7e§x§F§E§E§D§E§Dr§x§F§E§F§3§F§3s", "§7ᴘʜʏꜱɪᴄᴀʟ ʀᴇᴡᴀʀᴅꜱ ᴄᴀʀᴅꜱ"));
      inv.setItem(13, HeadManager.getHead("LIST", "§x§F§B§2§9§5§ER§x§F§C§3§1§6§9e§x§F§C§3§9§7§3d§x§F§D§4§1§7§Ee§x§F§D§4§A§8§9e§x§F§E§5§2§9§3m §x§F§E§5§A§9§EC§x§F§F§6§2§A§8o§x§F§F§6§A§B§3d§x§F§F§6§A§B§3e§x§F§F§6§A§B§3s", "§7ᴠɪʀᴛᴜᴀʟ ʀᴇᴡᴀʀᴅ ᴄᴏᴅᴇꜱ"));
      inv.setItem(15, HeadManager.getHead("ADMIN", ChatColor.GOLD + "§x§C§A§E§1§F§BA§x§C§5§D§D§F§Bd§x§C§0§D§A§F§Cm§x§B§B§D§6§F§Ci§x§B§6§D§2§F§Dn §x§B§2§C§F§F§DP§x§A§D§C§B§F§Ea§x§A§8§C§7§F§En§x§A§3§C§4§F§Fe§x§9§E§C§0§F§Fl", "§7ᴍᴀɴᴀɢᴇᴍᴇɴᴛ ᴏᴘᴛɪᴏɴꜱ ᴀɴᴅ ᴛᴏᴏʟꜱ"));

      GUIUtils.applyFlags(inv);

      player.openInventory(inv);
   }

   public static void handleClick(InventoryClickEvent event, Main plugin, CreateCodeHandler createHandler) {
      event.setCancelled(true);
      Player player = (Player)event.getWhoClicked();
      ItemStack clicked = event.getCurrentItem();
      ItemStack info = XMaterial.PAPER.parseItem();
      if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().getDisplayName() != null) {
         String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
         if (itemName.equals("Vouchers")) {
            SoundUtil.playClick(plugin, player);
            player.closeInventory();
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("general.coming-soon", "&#2D9DFF[&#62D6E6Vouchers&#2D9DFF] &#62D6E6Coming Soon..."));
         } else if (itemName.equals("Redeem Codes")) {
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