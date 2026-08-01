package xyz.redoxlabs.redeemcodes.managers;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.guis.AdminPanelGUI;
import xyz.redoxlabs.redeemcodes.guis.CodeEditorGUI;
import xyz.redoxlabs.redeemcodes.guis.MainGUI;
import xyz.redoxlabs.redeemcodes.guis.RewardGUI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CreateCodeHandler implements Listener {
   private final Main plugin;
   private final Set<UUID> awaitingInput = new HashSet();
   private final Set<UUID> awaitingDuplicationInput = new HashSet();
   private final Set<UUID> awaitingReviewInput = new HashSet();
   private final Map<UUID, String> selectedCodeForDuplication = new HashMap<>();

   public CreateCodeHandler(Main plugin) {
      this.plugin = plugin;
   }

   public void startCodeCreation(Player player) {
      awaitingInput.add(player.getUniqueId());
      player.closeInventory();
      player.sendMessage("§aPlease type the name for your new code in chat.");
      player.sendMessage("§7Type 'cancel' to abort.");
   }

   public void startDuplication(Player player, String selectedCode) {
      awaitingDuplicationInput.add(player.getUniqueId());
      selectedCodeForDuplication.put(player.getUniqueId(), selectedCode);
      player.closeInventory();
      player.sendMessage("§aPlease type the name for the duplicated code in chat.");
      player.sendMessage("§7Type 'cancel' to abort.");
   }

   public void startReviewInput(Player player) {
      awaitingReviewInput.add(player.getUniqueId());
      player.closeInventory();
      player.sendMessage("§aPlease type your review or feedback in chat.");
      player.sendMessage("§7Type 'cancel' to abort.");
   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerUUID = player.getUniqueId();
      if (awaitingInput.contains(playerUUID)) {
         event.setCancelled(true);
         String input = ChatColor.stripColor(event.getMessage()).trim().replace(" ", "");
         if (input.equalsIgnoreCase("cancel")) {
            awaitingInput.remove(playerUUID);
            player.sendMessage("§cCode creation cancelled.");
         } else if (!isValidCodeName(input)) {
            player.sendMessage("§cInvalid code name! Code names can only contain letters, numbers, and underscores.");
         } else {
            Bukkit.getScheduler().runTask(plugin, () -> createCode(player, input));
            awaitingInput.remove(playerUUID);
         }
      } else if (awaitingDuplicationInput.contains(playerUUID)) {
         event.setCancelled(true);
         String input = ChatColor.stripColor(event.getMessage()).trim().replace(" ", "");
         String sourceCode = (String)selectedCodeForDuplication.get(playerUUID);
         if (input.equalsIgnoreCase("cancel")) {
            awaitingDuplicationInput.remove(playerUUID);
            selectedCodeForDuplication.remove(playerUUID);
            player.sendMessage("§cCode duplication cancelled.");
         } else if (!isValidCodeName(input)) {
            player.sendMessage("§cInvalid code name! Code names can only contain letters, numbers, and underscores.");
         } else if (plugin.getCodesConfig().contains("Codes." + input)) {
            player.sendMessage("§cThat code name already exists!");
         } else {
            awaitingDuplicationInput.remove(playerUUID);
            selectedCodeForDuplication.remove(playerUUID);
            Bukkit.getScheduler().runTask(plugin, () -> duplicateCode(player, sourceCode, input));
         }
      } else if (awaitingReviewInput.contains(playerUUID)) {
         event.setCancelled(true);
         String input = ChatColor.stripColor(event.getMessage()).trim();
         if (input.equalsIgnoreCase("cancel")) {
            awaitingReviewInput.remove(playerUUID);
            player.sendMessage("§cReview cancelled.");
         } else {
            Bukkit.getScheduler().runTask(plugin, () -> sendReview(player, input));
            awaitingReviewInput.remove(playerUUID);
         }
      } else {
         CodeEditorGUI editor = (CodeEditorGUI)plugin.openEditorGUIs.get(player);
         if (editor != null) {
            editor.handleChatInput(event);
         }

         RewardGUI rewardGUI = (RewardGUI)plugin.openRewardGUIs.get(player);
         if (rewardGUI != null && (rewardGUI.awaitingCommandPackName.contains(playerUUID) || rewardGUI.awaitingCommandForPack.contains(playerUUID) || rewardGUI.awaitingWeightInput.contains(playerUUID))) {
            event.setCancelled(true);
            rewardGUI.handleChat(event);
         }

      }
   }

   private void createCode(Player player, String codeName) {
      FileConfiguration codes = plugin.getCodesConfig();
      if (codes.contains("Codes." + codeName)) {
         player.sendMessage("§cThat code already exists!");
      } else {
         codes.set("Codes." + codeName + ".enabled", true);
         codes.set("Codes." + codeName + ".permisson.required", false);
         codes.set("Codes." + codeName + ".permisson.list", new ArrayList<>());
         codes.set("Codes." + codeName + ".redeem-limit.Type", "PLAYER");
         codes.set("Codes." + codeName + ".redeem-limit.Count", 1);
         codes.set("Codes." + codeName + ".redeem-limit.Cooldown", 0);
         codes.set("Codes." + codeName + ".expire-time", -1);
         codes.set("Codes." + codeName + ".Playerlist.Used", new ArrayList<>());
         codes.set("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
         codes.set("Codes." + codeName + ".Playerlist.Blacklist.List", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.type", "RANDOM");
         codes.createSection("Codes." + codeName + ".rewards.commands");
         codes.set("Codes." + codeName + ".rewards.sacks", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.premades", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.events", new ArrayList<>());
         plugin.saveCodesConfig();
         player.sendMessage("§aCode §e" + codeName + " §ahas been created!");
         MainGUI.open(player);
      }
   }

   private void duplicateCode(Player player, String sourceCode, String newCodeName) {
      plugin.reloadCodesConfig();
      FileConfiguration codes = plugin.getCodesConfig();
      if (sourceCode == null) {
         player.sendMessage("§cError: Source code name was lost. Please try again.");
      } else {
         String sourcePath = "Codes." + sourceCode;
         String newPath = "Codes." + newCodeName;
         ConfigurationSection sourceSection = codes.getConfigurationSection(sourcePath);
         if (sourceSection == null) {
            player.sendMessage("§cError: Source code configuration not found for '" + sourceCode + "'.");
         } else {
            for(String key : sourceSection.getKeys(true)) {
               if (!sourceSection.isConfigurationSection(key)) {
                  codes.set(newPath + "." + key, sourceSection.get(key));
               }
            }

            codes.set(newPath + ".Playerlist.Used", new ArrayList<>());
            plugin.saveCodesConfig();
            player.sendMessage("§aCode §e" + newCodeName + " §ahas been created as a duplicate of §e" + sourceCode + "§a!");
            AdminPanelGUI.open(player, plugin);
         }
      }
   }

   private boolean isValidCodeName(String name) {
      return name.matches("^[a-zA-Z0-9_]+$");
   }

   private void sendReview(Player player, String reviewMessage) {
      player.closeInventory();
      player.performCommand("rc review " + reviewMessage);
   }
}



