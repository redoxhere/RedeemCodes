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
   private final Set<UUID> awaitingInput = new HashSet<>();
   private final Set<UUID> awaitingDuplicationInput = new HashSet<>();
   private final Set<UUID> awaitingReviewInput = new HashSet<>();
   private final Map<UUID, String> selectedCodeForDuplication = new HashMap<>();

   public CreateCodeHandler(Main plugin) {
      this.plugin = plugin;
   }

   public void startCodeCreation(Player player) {
      awaitingInput.add(player.getUniqueId());
      plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
      xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.create", "&aPlease type the name for your new code in chat.\n&7Type 'cancel' to abort."));
   }

   public void startDuplication(Player player, String selectedCode) {
      awaitingDuplicationInput.add(player.getUniqueId());
      selectedCodeForDuplication.put(player.getUniqueId(), selectedCode);
      plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
      xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.duplicate", "&aPlease type the name for the duplicated code in chat.\n&7Type 'cancel' to abort."));
   }

   public void startReviewInput(Player player) {
      awaitingReviewInput.add(player.getUniqueId());
      plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
      xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendRawMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.review", "&aPlease type your review or feedback in chat.\n&7Type 'cancel' to abort."));
   }

   @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
   public void onChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerUUID = player.getUniqueId();
      if (awaitingInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String input = ChatColor.stripColor(event.getMessage()).trim().replace(" ", "");
         if (input.equalsIgnoreCase("cancel")) {
            awaitingInput.remove(playerUUID);
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "guis.prompts.cancel");
         } else if (!isValidCodeName(input)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "guis.prompts.invalid-name");
         } else {
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> createCode(player, input));
            awaitingInput.remove(playerUUID);
         }
      } else if (awaitingDuplicationInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String input = ChatColor.stripColor(event.getMessage()).trim().replace(" ", "");
         String sourceCode = (String)selectedCodeForDuplication.get(playerUUID);
         if (input.equalsIgnoreCase("cancel")) {
            awaitingDuplicationInput.remove(playerUUID);
            selectedCodeForDuplication.remove(playerUUID);
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.cancelled", "&#FF6347Action cancelled."));
         } else if (!isValidCodeName(input)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.invalid-name", "&#FF6347Invalid code name! Code names can only contain letters and numbers."));
         } else if (plugin.getCodesConfig().contains("Codes." + input)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.already-exists", "&#FF6347That code name already exists!"));
         } else {
            awaitingDuplicationInput.remove(playerUUID);
            selectedCodeForDuplication.remove(playerUUID);
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> duplicateCode(player, sourceCode, input));
         }
      } else if (awaitingReviewInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String input = ChatColor.stripColor(event.getMessage()).trim();
         if (input.equalsIgnoreCase("cancel")) {
            awaitingReviewInput.remove(playerUUID);
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.review-cancelled", "&#FF6347Review cancelled."));
         } else {
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> sendReview(player, input));
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
            event.getRecipients().clear();
            rewardGUI.handleChat(event);
         }

         xyz.redoxlabs.redeemcodes.guis.GlobalSackListGUI sackGUI = plugin.openGlobalSackGUIs.get(player);
         if (sackGUI != null && sackGUI.awaitingSackName.contains(playerUUID)) {
            event.setCancelled(true);
            event.getRecipients().clear();
            sackGUI.handleChat(event);
         }

         xyz.redoxlabs.redeemcodes.guis.GlobalPremadeListGUI premadeGUI = plugin.openGlobalPremadeGUIs.get(player);
         if (premadeGUI != null && premadeGUI.awaitingPremadeName.contains(playerUUID)) {
            event.setCancelled(true);
            event.getRecipients().clear();
            premadeGUI.handleChat(event);
         }
      }
   }

   private void createCode(Player player, String codeName) {
      FileConfiguration codes = plugin.getCodesConfig();
      if (plugin.getCodesConfig().contains("Codes." + codeName)) {
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.dupe-already-exists", "&#FF6347That code already exists!"));
      } else {
         codes.set("Codes." + codeName + ".enabled", true);
         codes.set("Codes." + codeName + ".permisson.required", false);
         codes.set("Codes." + codeName + ".permisson.list", new ArrayList<>());
         codes.set("Codes." + codeName + ".redeem-limit.player", 1);
         codes.set("Codes." + codeName + ".redeem-limit.ip", 1);
         codes.set("Codes." + codeName + ".redeem-limit.global", -1);
         codes.set("Codes." + codeName + ".redeem-limit.cooldown", 0);
         codes.set("Codes." + codeName + ".expire-time", -1);
         codes.set("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
         codes.set("Codes." + codeName + ".Playerlist.Blacklist.List", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.type", "ALL");
         codes.createSection("Codes." + codeName + ".rewards.commands");
         codes.set("Codes." + codeName + ".rewards.sacks", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.premades", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.events", new ArrayList<>());
         codes.set("Codes." + codeName + ".rewards.list", new ArrayList<>());
         plugin.saveCodesConfig();
         String msg = plugin.getMessagesConfig().getString("general.code-created", "&aCode %code% has been created!").replace("%code%", codeName);
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msg);
         xyz.redoxlabs.redeemcodes.guis.CodesListGUI gui = new xyz.redoxlabs.redeemcodes.guis.CodesListGUI(plugin);
         plugin.openCodeGUIs.put(player, gui);
         gui.open(player);
      }
   }

   public void duplicateCode(Player player, String sourceCode, String newCodeName) {
      plugin.reloadCodesConfig();
      FileConfiguration codes = plugin.getCodesConfig();
      if (sourceCode == null) {
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.source-lost", "&#FF6347Error: Source code name was lost. Please try again."));
      } else {
         String sourcePath = "Codes." + sourceCode;
         String newPath = "Codes." + newCodeName;
         ConfigurationSection sourceSection = codes.getConfigurationSection(sourcePath);
         if (sourceSection == null) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.source-not-found", "&#FF6347Error: Source code configuration not found for '&#E0E0E0") + sourceCode + "&#FF6347'.");
         } else {
            for(String key : sourceSection.getKeys(true)) {
               if (!sourceSection.isConfigurationSection(key)) {
                  Object val = sourceSection.get(key);
                  if (val instanceof java.util.Collection) {
                     codes.set(newPath + "." + key, new java.util.ArrayList<>((java.util.Collection<?>) val));
                  } else {
                     codes.set(newPath + "." + key, val);
                  }
               }
            }

            plugin.saveCodesConfig();
            
            long expireTime = codes.getLong(newPath + ".expire-time", -1);
            if (expireTime != -1) {
               plugin.getExpirationManager().setExpiration(newCodeName, expireTime);
            }

            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.create.dupe-created", "&#32CD32Code <hover:&#E0E0E0Click to view details!><click:run_command:/rc show ") + newCodeName + ">&#00BFFF" + newCodeName + "</click></hover> &#32CD32has been created as a duplicate of &#00BFFF" + sourceCode + "&#32CD32!");
            xyz.redoxlabs.redeemcodes.guis.CodesListGUI gui = new xyz.redoxlabs.redeemcodes.guis.CodesListGUI(plugin);
            plugin.openCodeGUIs.put(player, gui);
            gui.open(player);
         }
      }
   }

   private boolean isValidCodeName(String name) {
      return name.matches("^[a-zA-Z0-9]+$");
   }

   private void sendReview(Player player, String reviewMessage) {
      player.closeInventory();
      player.performCommand("rc review " + reviewMessage);
   }
}



