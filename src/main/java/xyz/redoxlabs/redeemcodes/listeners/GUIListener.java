package xyz.redoxlabs.redeemcodes.listeners;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.guis.AdminPanelGUI;
import xyz.redoxlabs.redeemcodes.guis.CodeEditorGUI;
import xyz.redoxlabs.redeemcodes.guis.CodesListGUI;
import xyz.redoxlabs.redeemcodes.guis.ExpiredCodesListGUI;
import xyz.redoxlabs.redeemcodes.guis.MainGUI;
import xyz.redoxlabs.redeemcodes.guis.RewardGUI;
import xyz.redoxlabs.redeemcodes.guis.SelectCodeListGUI;
import xyz.redoxlabs.redeemcodes.managers.CreateCodeHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {
   private final Main plugin;
   private final CreateCodeHandler createHandler;

   public GUIListener(Main plugin, CreateCodeHandler createHandler) {
      this.plugin = plugin;
      this.createHandler = createHandler;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      String title = event.getView().getTitle();
      if (title.contains("Redeem Codes") && !title.contains("Expired")) {
         CodesListGUI gui = (CodesListGUI)plugin.openCodeGUIs.get(player);
         if (gui != null) {
            gui.handleClick(event, player);
         }
      } else if (title.contains("Edit Code:")) {
         CodeEditorGUI editor = (CodeEditorGUI)plugin.openEditorGUIs.get(player);
         if (editor != null) {
            editor.handleClick(event, player);
         }
      } else if (title.contains("Expired Redeem Codes")) {
         ExpiredCodesListGUI gui = (ExpiredCodesListGUI)plugin.openExpiredCodeGUIs.get(player);
         if (gui != null) {
            gui.handleClick(event, player);
         }
      } else if (title.contains("Select: Code List")) {
         SelectCodeListGUI gui = (SelectCodeListGUI)plugin.openSelectCodeGUIs.get(player);
         if (gui != null) {
            gui.handleClick(event, player);
         }
      } else if (title.equals(ChatColor.DARK_PURPLE + "Admin Panel")) {
         AdminPanelGUI.handleClick(event, plugin);
      } else if (title.equals(ChatColor.DARK_PURPLE + "RedeemCodes Menu")) {
         MainGUI.handleClick(event, plugin, createHandler);
      }

      RewardGUI rewardGUI = (RewardGUI)plugin.openRewardGUIs.get(player);
      if (rewardGUI != null && (title.startsWith("Reward Editor:") || title.startsWith("Add Reward:") || title.startsWith("Command Packs:") || title.startsWith("Sack Rewards:") || title.startsWith("Premade Rewards:") || title.startsWith("Select Sack:") || title.startsWith("Select Premade:") || title.startsWith("Select Event:"))) {
         rewardGUI.handleClick(event, player);
      }

   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      String title = event.getView().getTitle();
      if (title.contains("Edit Code:")) {
         CodeEditorGUI editor = (CodeEditorGUI)plugin.openEditorGUIs.get(player);
         if (editor != null) {
            editor.cancelUpdateTask();
         }
      } else if (title.contains("Redeem Codes") && !title.contains("Expired")) {
         plugin.openCodeGUIs.remove(player);
      } else if (title.contains("Expired Redeem Codes")) {
         plugin.openExpiredCodeGUIs.remove(player);
      } else if (title.contains("Select: Code List")) {
         plugin.openSelectCodeGUIs.remove(player);
      }

   }
}




