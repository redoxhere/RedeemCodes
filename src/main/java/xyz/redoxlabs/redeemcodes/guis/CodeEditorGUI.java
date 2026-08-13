package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.CodeExpirationManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.TimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import java.util.concurrent.TimeUnit;

public class CodeEditorGUI implements Listener {
   private final Main plugin;
   private final String codeName;
   private final CodesListGUI parentGUI;
   private Inventory inv;
   private final Set<UUID> awaitingCooldownInput = new HashSet<>();
   private final Set<UUID> awaitingLimitMessageInput = new HashSet<>();
   private final Set<UUID> awaitingRemoveConfirm = new HashSet<>();
   private final Set<UUID> awaitingBlacklistInput = new HashSet<>();
   private final Set<UUID> awaitingExpireTimeInput = new HashSet<>();
   private final Set<UUID> awaitingPermissionInput = new HashSet<>();
   private static final int TOTAL_SIZE = 54;
   private static final String pro = "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§F";
   private static final String pre = "§x§2§B§8§6§D§7";
   private WrappedTask updateTask;

   public CodeEditorGUI(Main plugin, String codeName, CodesListGUI parentGUI) {
      this.plugin = plugin;
      this.codeName = codeName;
      this.parentGUI = parentGUI;
   }

   public void open(final Player player) {
      cancelUpdateTask();
      GUIHolder holder = new GUIHolder("CODE_EDITOR");
      this.inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8✏ ᴇᴅɪᴛɪɴɢ ᴄᴏᴅᴇ: &9" + codeName.toUpperCase())));
      holder.setInventory(inv);

      ItemStack border = XMaterial.BLUE_STAINED_GLASS_PANE.parseItem();
      if (border == null) border = new ItemStack(org.bukkit.Material.DIRT);
      ItemMeta borderMeta = border.getItemMeta();
      if (borderMeta != null) {
         borderMeta.setDisplayName(" ");
         borderMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
      }

      border.setItemMeta(borderMeta);

      for(int i = 0; i < 54; ++i) {
         int row = i / 9;
         int col = i % 9;
         if (row == 0 || row == 5 || col == 0 || col == 8) {
            inv.setItem(i, border);
         }
      }

      Inventory inv_blacklist = inv;
      XMaterial mat_blacklist = XMaterial.WRITABLE_BOOK;
      String name_blacklist = "§x§8§3§4§F§1§5A§x§8§5§5§3§1§Cd§x§8§7§5§8§2§2d §x§8§9§5§C§2§9t§x§8§A§6§1§2§Fo §x§8§C§6§5§3§6B§x§8§E§6§A§3§Cl§x§9§0§6§E§4§3a§x§9§2§7§3§4§9c§x§9§4§7§7§5§0k§x§9§5§7§C§5§6l§x§9§7§8§0§5§Di§x§9§9§8§5§6§3s§x§9§B§8§9§6§At";
      List<String> lore_blacklist = List.of("§7ᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴅ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʙʟᴀᴄᴋʟɪꜱᴛ");
      inv_blacklist.setItem(41, createItem(mat_blacklist, name_blacklist, lore_blacklist));
      Inventory inv_remove = inv;
      XMaterial mat_remove = XMaterial.TNT;
      String name_remove = "§x§F§F§5§B§1§9R§x§F§F§6§1§1§De§x§F§F§6§6§2§1m§x§F§F§6§C§2§5o§x§F§F§7§1§2§9v§x§F§F§7§7§2§Ee §x§F§F§7§C§3§2C§x§F§F§8§2§3§6o§x§F§F§8§7§3§Ad§x§F§F§8§D§3§Ee";
      List<String> lore_remove = List.of("§7ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ ᴛʜɪꜱ ᴄᴏᴅᴇ.");
      inv_remove.setItem(45, createItem(mat_remove, name_remove, lore_remove));
      FileConfiguration config = plugin.getCodesConfig();
      boolean enabled = config.getBoolean("Codes." + codeName + ".enabled", true);
      XMaterial enabledMat = enabled ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE;
      String enabledName = enabled ? "§x§2§2§D§E§7§0E§x§2§2§D§E§7§0n§x§3§3§E§4§7§7a§x§4§4§E§B§7§Eb§x§5§4§F§2§8§5l§x§6§5§F§8§8§Ce§x§7§6§F§F§9§3d" : "§x§D§7§3§0§0§FD§x§D§D§3§4§1§5i§x§E§2§3§9§1§Bs§x§E§8§3§D§2§1a§x§E§E§4§1§2§7b§x§F§4§4§5§2§Dl§x§F§9§4§A§3§3e§x§F§F§4§E§3§9d";
      String clickToToggle = ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴇɴᴀʙʟᴇ/ᴅɪꜱᴀʙʟᴇ ᴛʜɪꜱ ᴄᴏᴅᴇ";
      inv.setItem(12, createItem(enabledMat, enabledName, List.of(clickToToggle)));

      boolean permRequired = config.getBoolean("Codes." + codeName + ".permisson.required", false);
      List<String> permList = config.getStringList("Codes." + codeName + ".permisson.list");
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.GRAY + "ʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ");
      lore.add(ChatColor.GRAY + "ʟᴇꜰᴛ ᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴅ ᴘᴇʀᴍɪꜱꜱɪᴏɴ");
      lore.add(ChatColor.GRAY + "ꜱʜɪꜰᴛ + ʟᴇꜰᴛ ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ ᴀʟʟ");
      lore.add(" ");
      lore.add("§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§FCurrently: §x§2§B§8§6§D§7" + (permRequired ? "Required" : "Not Required"));
      if (!permList.isEmpty()) {
         lore.add("§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§FPermissions:");
         for(String perm : permList) {
            lore.add("§x§2§B§8§6§D§7- " + perm);
         }
      }
      inv.setItem(14, createItem(XMaterial.NAME_TAG, "§x§F§F§E§5§6§8Permission Required", lore));

      int playerLimit = config.getInt("Codes." + codeName + ".redeem-limit.player", 1);
      int ipLimit = config.getInt("Codes." + codeName + ".redeem-limit.ip", 1);
      int globalLimit = config.getInt("Codes." + codeName + ".redeem-limit.global", -1);
      inv.setItem(20, createItem(XMaterial.EXPERIENCE_BOTTLE, "§x§D§4§F§F§1§9Redeem Limit", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴄᴏᴅᴇ'ꜱ ʀᴇᴅᴇᴇᴍ ʟɪᴍɪᴛ", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴘʟᴀʏᴇʀ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + playerLimit,
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fɪᴘ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + ipLimit,
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fɢʟᴏʙᴀʟ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + globalLimit
      )));

      int cd = config.getInt("Codes." + codeName + ".redeem-limit.cooldown", 0);
      inv.setItem(24, createItem(XMaterial.CLOCK, "§x§F§F§E§7§2§8Cooldown", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ʀᴇᴅᴇᴇᴍ ᴄᴏᴏʟᴅᴏᴡɴ", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ꜱᴛᴀᴛᴜꜱ: §x§2§B§8§6§D§7" + cd + " min"
      )));

      inv.setItem(29, createItem(XMaterial.ENDER_CHEST, "§x§9§D§2§6§F§FManage Rewards", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ʀᴇᴡᴀʀᴅꜱ (ᴀᴅᴅ/ᴇᴅɪᴛ/ᴇᴠᴇɴᴛꜱ/ᴛʏᴘᴇ)"
      )));

      String blacklistType = config.getString("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
      inv.setItem(33, createItem(XMaterial.BARRIER, "§x§F§F§0§0§0§0Blacklist Toggle", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ʙʟᴀᴄᴋʟɪꜱᴛ", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ꜱᴛᴀᴛᴜꜱ: §x§2§B§8§6§D§7" + blacklistType
      )));
      CodeExpirationManager expManager = plugin.getExpirationManager();
      boolean isExpired = expManager.isExpired(codeName);
      long remaining = expManager.getRemainingTime(codeName);
      if (isExpired) {
         inv.setItem(22, HeadManager.getHead("REACTIVATE", "§x§6§1§F§B§4§2R§x§6§0§F§B§4§8e§x§5§F§F§C§4§Da§x§5§E§F§C§5§3c§x§5§E§F§C§5§9t§x§5§D§F§D§5§Ei§x§5§C§F§D§6§4v§x§5§B§F§D§6§Aa§x§5§A§F§D§7§0t§x§5§9§F§E§7§5e §x§5§9§F§E§7§BC§x§5§8§F§E§8§1o§x§5§7§F§F§8§6d§x§5§6§F§F§8§Ce", "§7ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴀᴄᴛɪᴠᴀᴛᴇ ᴛʜɪꜱ ᴄᴏᴅᴇ", "§7ᴛʜɪꜱ ᴡɪʟʟ ꜱᴇᴛ ɪᴛꜱ ᴇxᴘɪʀᴀᴛɪᴏɴ ᴛᴏ 'ɴᴇᴠᴇʀ'"));
      } else {
         String expireDisplay;
         if (remaining > 0L) {
            expireDisplay = TimeFormatter.formatDuration(remaining);
            this.updateTask = plugin.getFoliaLib().getImpl().runAtEntityTimer(player, () -> {
                  if (player != null && player.isOnline() && player.getOpenInventory().getTitle().contains(codeName.toUpperCase())) {
                     long newRemaining = plugin.getExpirationManager().getRemainingTime(codeName);
                     if (newRemaining <= 0L) {
                        this.cancelUpdateTask();
                        open(player);
                     } else {
                        ItemStack item = HeadManager.getHead("EXPIRE_TIME", "§x§F§B§D§7§6§5E§x§F§B§D§B§6§Dx§x§F§C§D§F§7§5p§x§F§C§E§3§7§Di§x§F§D§E§7§8§5r§x§F§D§E§A§8§De §x§F§E§E§E§9§5T§x§F§E§F§2§9§Di§x§F§F§F§6§A§5m§x§F§F§F§A§A§De", "§7ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ᴇxᴘɪʀᴇ ᴛɪᴍᴇ", " ", "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴇxᴘɪʀɪɴɢ ɪɴ: §x§2§B§8§6§D§7" + TimeFormatter.formatDuration(newRemaining));
                        inv.setItem(22, item);
                     }
                  } else {
                     this.cancelUpdateTask();
                  }
            }, 20L * 50L, 20L * 50L, TimeUnit.MILLISECONDS);
         } else {
            int duration = plugin.getCodesConfig().getInt("Codes." + codeName + ".expire-time", -1);
            expireDisplay = duration == -1 ? "Never" : TimeFormatter.formatDuration((long)duration * 1000L);
         }

         inv.setItem(22, HeadManager.getHead("EXPIRE_TIME", "§cExpire Time", "§7Click to set expire time.", " ", "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§FCurrent: §x§2§B§8§6§D§7" + expireDisplay));
      }

      List<String> usedPlayers = plugin.getRedeemDataManager().getRedeemedPlayers(codeName);
      inv.setItem(39, createItem(XMaterial.PLAYER_HEAD, "§x§1§9§F§4§F§FPlayers Redeemed", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ʟɪꜱᴛ", 
         "", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴏᴜɴᴛ: §x§2§B§8§6§D§7" + usedPlayers.size()
      )));
      ItemStack backButton = HeadManager.getHead("BACK", "§cGo Back", "§7ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ʙᴀᴄᴋ ᴛᴏ ᴄᴏᴅᴇ ʟɪꜱᴛ");
      inv.setItem(49, backButton);

      for (int i = 0; i < inv.getSize(); i++) {
         ItemStack item = inv.getItem(i);
         if (item != null && item.hasItemMeta()) {
             ItemMeta meta = item.getItemMeta();
             meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
             item.setItemMeta(meta);
         }
      }

      player.openInventory(inv);
   }

   public void cancelUpdateTask() {
      if (updateTask != null && !updateTask.isCancelled()) {
         updateTask.cancel();
         this.updateTask = null;
      }

   }

   private ItemStack createItem(XMaterial material, String name, List<String> lore) {
      ItemStack item = material.parseItem();
      if (item == null) item = new ItemStack(org.bukkit.Material.DIRT);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(name));
         if (lore != null) {
             List<String> formattedLore = new java.util.ArrayList<>();
             for (String l : lore) formattedLore.add(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(l));
             meta.setLore(formattedLore);
         }
         item.setItemMeta(meta);
      }

      return item;
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      event.setCancelled(true);
      if (event.getClickedInventory() != null && event.getClickedInventory().equals(inv)) {
         ItemStack clicked = event.getCurrentItem();
         if (clicked != null && clicked.hasItemMeta() && !clicked.getItemMeta().getDisplayName().isEmpty()) {
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (name.equals("Go Back")) {
               xyz.redoxlabs.redeemcodes.utils.SoundUtil.playClick(plugin, player);
               cancelUpdateTask();
               player.closeInventory();
               plugin.openEditorGUIs.remove(player);
               if (parentGUI != null) {
                  plugin.openCodeGUIs.put(player, parentGUI);
                  parentGUI.open(player);
               }
            } else if (!name.equals("Enabled") && !name.equals("Disabled")) {
               xyz.redoxlabs.redeemcodes.utils.SoundUtil.playClick(plugin, player);
               if (name.equals("Permission Required")) {
                  if (event.isRightClick()) {
                     FileConfiguration config = plugin.getCodesConfig();
                     boolean permRequired = config.getBoolean("Codes." + codeName + ".permisson.required", false);
                     config.set("Codes." + codeName + ".permisson.required", !permRequired);
                     plugin.saveCodesConfig();
                     open(player);
                  } else if (event.isLeftClick() && event.isShiftClick()) {
                     plugin.getCodesConfig().set("Codes." + codeName + ".permisson.list", new ArrayList<>());
                     plugin.saveCodesConfig();
                     xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.permission-removed", "&#32CD32All permissions removed from code \'&#00BFFF") + codeName + "&#32CD32\'.");
                     open(player);
                  } else if (event.isLeftClick()) {
                     awaitingPermissionInput.add(player.getUniqueId());
                     plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                     xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.add-permission", "&#00BFFFPlease type the permission in chat. (e.g., \'code.redeem.example\')\n&#E0E0E0Type \'cancel\' to abort."));
                  }
               } else if (name.equals("Manage Rewards")) {
                  RewardGUI rewardGUI = new RewardGUI(plugin, codeName, this);
                  plugin.openRewardGUIs.put(player, rewardGUI);
                  rewardGUI.openMain(player);
               } else if (name.equals("Redeem Limit")) {
                  xyz.redoxlabs.redeemcodes.guis.RedeemLimitGUI limitGUI = new xyz.redoxlabs.redeemcodes.guis.RedeemLimitGUI(plugin, codeName, this);
                  plugin.openLimitGUIs.put(player, limitGUI);
                  limitGUI.open(player);
               } else if (name.equals("Cooldown")) {
                  awaitingCooldownInput.add(player.getUniqueId());
                  plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.set-cooldown", "&#00BFFFType the cooldown in chat (e.g., 1s, 5m, 1h, 3d, 1w, 2mn, 1y).\n&#E0E0E0Type \'cancel\' to abort."));
               } else if (name.equals("Expire Time")) {
                  awaitingExpireTimeInput.add(player.getUniqueId());
                  plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.set-expiration", "&#00BFFFType the expiration time in chat (e.g., 1s, 5m, 1h, 3d, 1w, 2mn, 1y).\n&#E0E0E0Type \'cancel\' to abort. Type \'never\' to disable expiration."));
               } else if (name.equals("Reactivate Code")) {
                  plugin.getExpirationManager().reactivate(codeName);
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.code-reactivated", "&#32CD32Code \'&#00BFFF") + codeName + "&#32CD32\' has been reactivated.");
                  open(player);
               } else if (name.equals("Blacklist Toggle")) {
                  String bl = plugin.getCodesConfig().getString("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
                  bl = bl.equalsIgnoreCase("ENABLED") ? "DISABLED" : (bl.equalsIgnoreCase("DISABLED") ? "REVERSED" : "ENABLED");
                  plugin.getCodesConfig().set("Codes." + codeName + ".Playerlist.Blacklist.Type", bl);
                  plugin.saveCodesConfig();
                  open(player);
               } else if (name.equals("Players Redeemed")) {
                  plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                  player.performCommand("rc redeemed " + codeName);
               } else if (name.equals("Add to Blacklist")) {
                  awaitingBlacklistInput.add(player.getUniqueId());
                  plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.set-limit", "&#00BFFFType the maximum uses in chat.\n&#E0E0E0Type \'cancel\' to abort."));
               } else if (name.equals("Remove Code")) {
                  awaitingRemoveConfirm.add(player.getUniqueId());
                  plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.confirm-remove", "&#FF6347WARNING: You are about to remove the code \'&#00BFFF") + codeName + "&#FF6347\'.\n&#FFD700This action is permanent. Type \'confirm\' in chat to proceed.\n&#E0E0E0Type anything else to cancel.");
               }
            } else {
               xyz.redoxlabs.redeemcodes.utils.SoundUtil.playClick(plugin, player);
               FileConfiguration config = plugin.getCodesConfig();
               boolean enabled = config.getBoolean("Codes." + codeName + ".enabled", true);
               config.set("Codes." + codeName + ".enabled", !enabled);
               plugin.saveCodesConfig();
               open(player);
            }

         }
      }
   }

   public void handleChatInput(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerUUID = player.getUniqueId();
      if (awaitingCooldownInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String msg = event.getMessage();
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            awaitingCooldownInput.remove(playerUUID);
            if (msg.equalsIgnoreCase("cancel")) {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.cancel", "&#FF6347Action cancelled."));
               open(player);
            } else {
               long minutes = TimeFormatter.parseTimeToMinutes(msg);
               if (minutes < 0L) {
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.invalid-format", "&#FF6347Invalid format. Use 1s, 3m, 1h, 1d, 1w, 1mn, 1y."));
                  awaitingCooldownInput.add(playerUUID);
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.invalid-number", "&#FF6347Invalid number! Please try again or type 'cancel'."));
               } else {
                  plugin.getCodesConfig().set("Codes." + codeName + ".redeem-limit.Cooldown", (int)minutes);
                  plugin.saveCodesConfig();
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.cooldown-set", "&#32CD32Cooldown set to &#00BFFF") + msg + "&#32CD32.");
                  open(player);
               }
            }

         });
      } else if (awaitingBlacklistInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String playerName = event.getMessage().trim();
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            awaitingBlacklistInput.remove(playerUUID);
            if (playerName.equalsIgnoreCase("cancel")) {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.blacklist-add-cancelled", "&#FF6347Blacklist addition cancelled."));
            } else {
               List<String> blacklist = plugin.getCodesConfig().getStringList("Codes." + codeName + ".Playerlist.Blacklist.List");
               if (blacklist.contains(playerName)) {
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.blacklist-already-exists", "&#FFD700") + playerName + " is already on the blacklist.");
               } else {
                  blacklist.add(playerName);
                  plugin.getCodesConfig().set("Codes." + codeName + ".Playerlist.Blacklist.List", blacklist);
                  plugin.saveCodesConfig();
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.blacklist-added", "&#87CEFA") + playerName + " has been added to the blacklist.");
               }
            }

            open(player);
         });
      } else if (awaitingExpireTimeInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String input = event.getMessage().trim();
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            awaitingExpireTimeInput.remove(playerUUID);
            if (input.equalsIgnoreCase("cancel")) {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.expire-cancelled", "&#FF6347Expire time setup cancelled."));
            } else if (input.equalsIgnoreCase("never")) {
               plugin.getExpirationManager().setExpiration(codeName, -1L);
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.expire-disabled", "&#87CEFAExpire time disabled (never expires)."));
            } else {
               long seconds = TimeFormatter.parseTimeToSeconds(input);
               if (seconds < 0L) {
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.invalid-permission", "&#FF6347Invalid permission format! Only letters, numbers, dots, and underscores allowed."));
                  awaitingExpireTimeInput.add(playerUUID);
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.try-again", "&#32CD32Please try again, or type 'cancel' to exit."));
                  return;
               }

               plugin.getExpirationManager().setExpiration(codeName, seconds);
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.expire-set", "&#87CEFAExpire time set to &#00BFFF") + input + " &#87CEFA(" + seconds + " seconds).");
            }

            open(player);
         });
      } else if (awaitingPermissionInput.contains(playerUUID)) {
         event.setCancelled(true);
         event.getRecipients().clear();
         String input = event.getMessage().trim();
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            awaitingPermissionInput.remove(playerUUID);
            if (input.equalsIgnoreCase("cancel")) {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.permission-add-cancelled", "&#FF6347Permission addition cancelled."));
            } else {
               List<String> permList = plugin.getCodesConfig().getStringList("Codes." + codeName + ".permisson.list");
               if (permList.contains(input)) {
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.permission-already-exists", "&#FFD700Permission \'&#00BFFF") + input + "&#FFD700\' is already in the list.");
               } else {
                  permList.add(input);
                  plugin.getCodesConfig().set("Codes." + codeName + ".permisson.list", permList);
                  plugin.saveCodesConfig();
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.permission-added", "&#87CEFAPermission \'&#00BFFF") + input + "&#87CEFA\' added to the list.");
               }
            }

            open(player);
         });
      } else if (awaitingRemoveConfirm.contains(playerUUID)) {
         event.setCancelled(true);
         String confirmation = ChatColor.stripColor(event.getMessage()).trim();
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            awaitingRemoveConfirm.remove(playerUUID);
            if (confirmation.equalsIgnoreCase("confirm")) {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.removing-code", "&#32CD32Executing removal command..."));
               player.performCommand("rc remove " + codeName);
               plugin.openEditorGUIs.remove(player);
            } else {
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.code-editor.remove-cancelled", "&#FF6347Code removal cancelled."));
               open(player);
            }

         });
      }
   }
}





