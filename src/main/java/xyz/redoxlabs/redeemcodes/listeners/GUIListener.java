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
      if (event.getInventory().getHolder() instanceof xyz.redoxlabs.redeemcodes.utils.GUIHolder) {
         xyz.redoxlabs.redeemcodes.utils.GUIHolder holder = (xyz.redoxlabs.redeemcodes.utils.GUIHolder) event.getInventory().getHolder();
         String id = holder.getGuiType();

         switch (id) {
            case "MAIN_GUI":
               MainGUI.handleClick(event, plugin, createHandler);
               break;
            case "ADMIN_PANEL":
               AdminPanelGUI.handleClick(event, plugin);
               break;
            case "CODES_LIST":
               CodesListGUI codesGui = (CodesListGUI)plugin.openCodeGUIs.get(player);
               if (codesGui != null) {
                  codesGui.handleClick(event, player);
               }
               break;
            case "CODE_EDITOR":
               CodeEditorGUI editor = (CodeEditorGUI)plugin.openEditorGUIs.get(player);
               if (editor != null) {
                  editor.handleClick(event, player);
               }
               break;
            case "EXPIRED_CODES_LIST":
               ExpiredCodesListGUI expiredGui = (ExpiredCodesListGUI)plugin.openExpiredCodeGUIs.get(player);
               if (expiredGui != null) {
                  expiredGui.handleClick(event, player);
               }
               break;
            case "SELECT_CODE_LIST":
               SelectCodeListGUI selectGui = (SelectCodeListGUI)plugin.openSelectCodeGUIs.get(player);
               if (selectGui != null) {
                  selectGui.handleClick(event, player);
               }
               break;
            case "REWARD_GUI":
               RewardGUI rewardGUI = (RewardGUI)plugin.openRewardGUIs.get(player);
               if (rewardGUI != null) {
                  rewardGUI.handleClick(event, player);
               }
               break;
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      if (event.getInventory().getHolder() instanceof xyz.redoxlabs.redeemcodes.utils.GUIHolder) {
         xyz.redoxlabs.redeemcodes.utils.GUIHolder holder = (xyz.redoxlabs.redeemcodes.utils.GUIHolder) event.getInventory().getHolder();
         String id = holder.getGuiType();

         switch (id) {
            case "CODE_EDITOR":
               CodeEditorGUI editor = (CodeEditorGUI)plugin.openEditorGUIs.get(player);
               if (editor != null) {
                  editor.cancelUpdateTask();
               }
               break;
            case "CODES_LIST":
               plugin.openCodeGUIs.remove(player);
               break;
            case "EXPIRED_CODES_LIST":
               plugin.openExpiredCodeGUIs.remove(player);
               break;
            case "SELECT_CODE_LIST":
               plugin.openSelectCodeGUIs.remove(player);
               break;
         }
      }
   }
}




