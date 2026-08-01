package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.CodeExpirationManager;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.TimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CodeEditorGUI implements Listener {
   private final Main plugin;
   private final String codeName;
   private final CodesListGUI parentGUI;
   private Inventory inv;
   private final Set<UUID> awaitingCooldownInput = new HashSet();
   private final Set<UUID> awaitingLimitMessageInput = new HashSet();
   private final Set<UUID> awaitingRemoveConfirm = new HashSet();
   private final Set<UUID> awaitingBlacklistInput = new HashSet();
   private final Set<UUID> awaitingExpireTimeInput = new HashSet();
   private final Set<UUID> awaitingPermissionInput = new HashSet();
   private static final int TOTAL_SIZE = 54;
   private static final String pro = "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§F";
   private static final String pre = "§x§2§B§8§6§D§7";
   private BukkitTask updateTask;

   public CodeEditorGUI(Main plugin, String codeName, CodesListGUI parentGUI) {
      this.plugin = plugin;
      this.codeName = codeName;
      this.parentGUI = parentGUI;
   }

   public void open(final Player player) {
      cancelUpdateTask();
      this.inv = Bukkit.createInventory((InventoryHolder)null, 54, "§x§0§0§0§0§0§0Edit Code: §x§2§B§8§6§D§7" + codeName);
      ItemStack border = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
      ItemMeta borderMeta = border.getItemMeta();
      if (borderMeta != null) {
         borderMeta.setDisplayName(" ");
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
      Material mat_blacklist = Material.WRITABLE_BOOK;
      String name_blacklist = "§x§8§3§4§F§1§5A§x§8§5§5§3§1§Cd§x§8§7§5§8§2§2d §x§8§9§5§C§2§9t§x§8§A§6§1§2§Fo §x§8§C§6§5§3§6B§x§8§E§6§A§3§Cl§x§9§0§6§E§4§3a§x§9§2§7§3§4§9c§x§9§4§7§7§5§0k§x§9§5§7§C§5§6l§x§9§7§8§0§5§Di§x§9§9§8§5§6§3s§x§9§B§8§9§6§At";
      List<String> lore_blacklist = List.of("§7ᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴅ ᴀ ᴘʟᴀʏᴇʀ ᴛᴏ ʙʟᴀᴄᴋʟɪꜱᴛ");
      inv_blacklist.setItem(41, createItem(mat_blacklist, name_blacklist, lore_blacklist));
      Inventory inv_remove = inv;
      Material mat_remove = Material.TNT;
      String name_remove = "§x§F§F§5§B§1§9R§x§F§F§6§1§1§De§x§F§F§6§6§2§1m§x§F§F§6§C§2§5o§x§F§F§7§1§2§9v§x§F§F§7§7§2§Ee §x§F§F§7§C§3§2C§x§F§F§8§2§3§6o§x§F§F§8§7§3§Ad§x§F§F§8§D§3§Ee";
      List<String> lore_remove = List.of("§7ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ ᴛʜɪꜱ ᴄᴏᴅᴇ.");
      inv_remove.setItem(45, createItem(mat_remove, name_remove, lore_remove));
      FileConfiguration config = plugin.getCodesConfig();
      boolean enabled = config.getBoolean("Codes." + codeName + ".enabled", true);
      Material enabledMat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
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
      inv.setItem(14, createItem(Material.NAME_TAG, "§x§F§F§E§5§6§8Permission Required", lore));

      int limitCount = config.getInt("Codes." + codeName + ".redeem-limit.Count", 1);
      String limitType = config.getString("Codes." + codeName + ".redeem-limit.Type", "PLAYER");
      inv.setItem(20, createItem(Material.EXPERIENCE_BOTTLE, "§x§D§4§F§F§1§9Redeem Limit", List.of(
         ChatColor.GRAY + "ʟᴇꜰᴛ ᴄʟɪᴄᴋ: -1 | ʀɪɢʜᴛ ᴄʟɪᴄᴋ: +1", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ʟɪᴍɪᴛ: §x§2§B§8§6§D§7" + limitCount
      )));

      inv.setItem(22, createItem(Material.COMPARATOR, "§x§F§F§5§1§2§DRedeem Type", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ʟɪᴍɪᴛ ᴛʏᴘᴇ", 
         "", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§FType: §x§2§B§8§6§D§7" + limitType
      )));

      int cd = config.getInt("Codes." + codeName + ".redeem-limit.Cooldown", 0);
      inv.setItem(24, createItem(Material.CLOCK, "§x§F§F§E§7§2§8Cooldown", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ʀᴇᴅᴇᴇᴍ ᴄᴏᴏʟᴅᴏᴡɴ", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ꜱᴛᴀᴛᴜꜱ: §x§2§B§8§6§D§7" + cd + " min"
      )));

      inv.setItem(29, createItem(Material.ENDER_CHEST, "§x§9§D§2§6§F§FManage Rewards", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ʀᴇᴡᴀʀᴅꜱ (ᴀᴅᴅ/ᴇᴅɪᴛ/ᴇᴠᴇɴᴛꜱ/ᴛʏᴘᴇ)"
      )));

      String blacklistType = config.getString("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
      inv.setItem(33, createItem(Material.BARRIER, "§x§F§F§0§0§0§0Blacklist Toggle", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ʙʟᴀᴄᴋʟɪꜱᴛ", 
         " ", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴜʀʀᴇɴᴛ ꜱᴛᴀᴛᴜꜱ: §x§2§B§8§6§D§7" + blacklistType
      )));
      CodeExpirationManager expManager = plugin.getExpirationManager();
      boolean isExpired = expManager.isExpired(codeName);
      long remaining = expManager.getRemainingTime(codeName);
      if (isExpired) {
         inv.setItem(31, HeadManager.getHead("REACTIVATE", "§x§6§1§F§B§4§2R§x§6§0§F§B§4§8e§x§5§F§F§C§4§Da§x§5§E§F§C§5§3c§x§5§E§F§C§5§9t§x§5§D§F§D§5§Ei§x§5§C§F§D§6§4v§x§5§B§F§D§6§Aa§x§5§A§F§D§7§0t§x§5§9§F§E§7§5e §x§5§9§F§E§7§BC§x§5§8§F§E§8§1o§x§5§7§F§F§8§6d§x§5§6§F§F§8§Ce", "§7ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴀᴄᴛɪᴠᴀᴛᴇ ᴛʜɪꜱ ᴄᴏᴅᴇ", "§7ᴛʜɪꜱ ᴡɪʟʟ ꜱᴇᴛ ɪᴛꜱ ᴇxᴘɪʀᴀᴛɪᴏɴ ᴛᴏ 'ɴᴇᴠᴇʀ'"));
      } else {
         String expireDisplay;
         if (remaining > 0L) {
            expireDisplay = TimeFormatter.formatDuration(remaining);
            this.updateTask = (new BukkitRunnable() {
               public void run() {
                  if (player != null && player.isOnline() && player.getOpenInventory().getTitle().contains(codeName)) {
                     long newRemaining = plugin.getExpirationManager().getRemainingTime(codeName);
                     if (newRemaining <= 0L) {
                        cancel();
                        open(player);
                     } else {
                        ItemStack item = HeadManager.getHead("EXPIRE_TIME", "§x§F§B§D§7§6§5E§x§F§B§D§B§6§Dx§x§F§C§D§F§7§5p§x§F§C§E§3§7§Di§x§F§D§E§7§8§5r§x§F§D§E§A§8§De §x§F§E§E§E§9§5T§x§F§E§F§2§9§Di§x§F§F§F§6§A§5m§x§F§F§F§A§A§De", "§7ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ᴇxᴘɪʀᴇ ᴛɪᴍᴇ", " ", "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴇxᴘɪʀɪɴɢ ɪɴ: §x§2§B§8§6§D§7" + TimeFormatter.formatDuration(newRemaining));
                        inv.setItem(31, item);
                     }
                  } else {
                     cancel();
                  }
               }
            }).runTaskTimer(plugin, 20L, 20L);
         } else {
            int duration = plugin.getCodesConfig().getInt("Codes." + codeName + ".expire-time", -1);
            expireDisplay = duration == -1 ? "Never" : TimeFormatter.formatDuration((long)duration * 1000L);
         }

         inv.setItem(31, HeadManager.getHead("EXPIRE_TIME", "§cExpire Time", "§7Click to set expire time.", " ", "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§FCurrent: §x§2§B§8§6§D§7" + expireDisplay));
      }

      List<String> usedPlayers = plugin.getRedeemDataManager().getRedeemedPlayers(codeName);
      inv.setItem(39, createItem(Material.PLAYER_HEAD, "§x§1§9§F§4§F§FPlayers Redeemed", List.of(
         ChatColor.GRAY + "ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ʟɪꜱᴛ", 
         "", 
         "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§Fᴄᴏᴜɴᴛ: §x§2§B§8§6§D§7" + usedPlayers.size()
      )));
      ItemStack backButton = HeadManager.getHead("BACK", "§cGo Back", "§7ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ʙᴀᴄᴋ ᴛᴏ ᴄᴏᴅᴇ ʟɪꜱᴛ");
      inv.setItem(49, backButton);
      player.openInventory(inv);
   }

   public void cancelUpdateTask() {
      if (updateTask != null && !updateTask.isCancelled()) {
         updateTask.cancel();
         this.updateTask = null;
      }

   }

   private ItemStack createItem(Material material, String name, List<String> lore) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   public void handleClick(InventoryClickEvent event, Player player) {
      if (event.getView().getTitle().contains("Edit Code:")) {
         event.setCancelled(true);
         if (event.getClickedInventory() != null && event.getClickedInventory().equals(inv)) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && !clicked.getItemMeta().getDisplayName().isEmpty()) {
               String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
               if (name.equals("Go Back")) {
                  cancelUpdateTask();
                  player.closeInventory();
                  plugin.openEditorGUIs.remove(player);
                  if (parentGUI != null) {
                     plugin.openCodeGUIs.put(player, parentGUI);
                     parentGUI.open(player);
                  }
               } else if (!name.equals("Enabled") && !name.equals("Disabled")) {
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
                        player.sendMessage(ChatColor.GREEN + "All permissions removed from code '" + codeName + "'.");
                        open(player);
                     } else if (event.isLeftClick()) {
                        awaitingPermissionInput.add(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage(ChatColor.GREEN + "Please type the permission in chat. (e.g., 'code.redeem.example')");
                        player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                     }
                  } else if (name.equals("Manage Rewards")) {
                     RewardGUI rewardGUI = new RewardGUI(plugin, codeName, this);
                     plugin.openRewardGUIs.put(player, rewardGUI);
                     rewardGUI.openMain(player);
                  } else if (name.equals("Redeem Limit")) {
                     int current = plugin.getCodesConfig().getInt("Codes." + codeName + ".redeem-limit.Count", 1);
                     if (event.isRightClick()) {
                        ++current;
                     } else if (event.isLeftClick()) {
                        current = Math.max(1, current - 1);
                     }

                     plugin.getCodesConfig().set("Codes." + codeName + ".redeem-limit.Count", current);
                     plugin.saveCodesConfig();
                     open(player);
                  } else if (name.equals("Redeem Type")) {
                     FileConfiguration config = plugin.getCodesConfig();
                     String type = config.getString("Codes." + codeName + ".redeem-limit.Type", "PLAYER");
                     type = type.equalsIgnoreCase("PLAYER") ? "CODE" : "PLAYER";
                     config.set("Codes." + codeName + ".redeem-limit.Type", type);
                     plugin.saveCodesConfig();
                     open(player);
                  } else if (name.equals("Cooldown")) {
                     awaitingCooldownInput.add(player.getUniqueId());
                     player.closeInventory();
                     player.sendMessage(ChatColor.GREEN + "Type the cooldown in chat (e.g., 1s, 5m, 1h, 3d, 1w, 2mn, 1y).");
                     player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                  } else if (name.equals("Expire Time")) {
                     awaitingExpireTimeInput.add(player.getUniqueId());
                     player.closeInventory();
                     player.sendMessage(ChatColor.GREEN + "Type the expire time in chat (e.g., 1s, 5m, 1h, 3d, 1w, 2mn, 1y).");
                     player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort. Type 'never' to disable expiration.");
                  } else if (name.equals("Reactivate Code")) {
                     plugin.getExpirationManager().reactivate(codeName);
                     player.sendMessage(ChatColor.GREEN + "Code '" + codeName + "' has been reactivated.");
                     open(player);
                  } else if (name.equals("Blacklist Toggle")) {
                     String bl = plugin.getCodesConfig().getString("Codes." + codeName + ".Playerlist.Blacklist.Type", "ENABLED");
                     bl = bl.equalsIgnoreCase("ENABLED") ? "DISABLED" : (bl.equalsIgnoreCase("DISABLED") ? "REVERSED" : "ENABLED");
                     plugin.getCodesConfig().set("Codes." + codeName + ".Playerlist.Blacklist.Type", bl);
                     plugin.saveCodesConfig();
                     open(player);
                  } else if (name.equals("Players Redeemed")) {
                     player.closeInventory();
                     player.performCommand("rc redeemed " + codeName);
                  } else if (name.equals("Add to Blacklist")) {
                     awaitingBlacklistInput.add(player.getUniqueId());
                     player.closeInventory();
                     player.sendMessage(ChatColor.GREEN + "Please type the username to add to the blacklist.");
                     player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                  } else if (name.equals("Remove Code")) {
                     awaitingRemoveConfirm.add(player.getUniqueId());
                     player.closeInventory();
                     player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WARNING: You are about to remove the code '" + codeName + "'.");
                     player.sendMessage(ChatColor.YELLOW + "This action is permanent. Type 'confirm' in chat to proceed.");
                     player.sendMessage(ChatColor.GRAY + "Type anything else to cancel.");
                  }
               } else {
                  FileConfiguration config = plugin.getCodesConfig();
                  boolean enabled = config.getBoolean("Codes." + codeName + ".enabled", true);
                  config.set("Codes." + codeName + ".enabled", !enabled);
                  plugin.saveCodesConfig();
                  open(player);
               }

            }
         }
      }
   }

   public void handleChatInput(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerUUID = player.getUniqueId();
      if (awaitingCooldownInput.contains(playerUUID)) {
         event.setCancelled(true);
         String msg = event.getMessage();
         Bukkit.getScheduler().runTask(plugin, () -> {
            awaitingCooldownInput.remove(playerUUID);
            if (msg.equalsIgnoreCase("cancel")) {
               player.sendMessage(String.valueOf(ChatColor.RED) + "Cooldown setup cancelled.");
               open(player);
            } else {
               long minutes = parseTimeToMinutes(msg);
               if (minutes < 0L) {
                  player.sendMessage(String.valueOf(ChatColor.RED) + "Invalid format. Use 1s, 3m, 1h, 1d, 1w, 1mn, 1y.");
                  awaitingCooldownInput.add(playerUUID);
                  player.sendMessage(String.valueOf(ChatColor.GREEN) + "Please try again, or type 'cancel' to exit.");
               } else {
                  plugin.getCodesConfig().set("Codes." + codeName + ".redeem-limit.Cooldown", (int)minutes);
                  plugin.saveCodesConfig();
                  player.sendMessage(String.valueOf(ChatColor.AQUA) + "Cooldown set to " + msg + " (" + minutes + " minutes).");
                  open(player);
               }
            }

         });
      } else if (awaitingBlacklistInput.contains(playerUUID)) {
         event.setCancelled(true);
         String playerName = event.getMessage().trim();
         Bukkit.getScheduler().runTask(plugin, () -> {
            awaitingBlacklistInput.remove(playerUUID);
            if (playerName.equalsIgnoreCase("cancel")) {
               player.sendMessage(ChatColor.RED + "Blacklist addition cancelled.");
            } else {
               List<String> blacklist = plugin.getCodesConfig().getStringList("Codes." + codeName + ".Playerlist.Blacklist.List");
               if (blacklist.contains(playerName)) {
                  player.sendMessage(ChatColor.YELLOW + playerName + " is already on the blacklist.");
               } else {
                  blacklist.add(playerName);
                  plugin.getCodesConfig().set("Codes." + codeName + ".Playerlist.Blacklist.List", blacklist);
                  plugin.saveCodesConfig();
                  player.sendMessage(ChatColor.AQUA + playerName + " has been added to the blacklist.");
               }
            }

            open(player);
         });
      } else if (awaitingExpireTimeInput.contains(playerUUID)) {
         event.setCancelled(true);
         String input = event.getMessage().trim();
         Bukkit.getScheduler().runTask(plugin, () -> {
            awaitingExpireTimeInput.remove(playerUUID);
            if (input.equalsIgnoreCase("cancel")) {
               player.sendMessage(ChatColor.RED + "Expire time setup cancelled.");
            } else if (input.equalsIgnoreCase("never")) {
               plugin.getExpirationManager().setExpiration(codeName, -1L);
               player.sendMessage(ChatColor.AQUA + "Expire time disabled (never expires).");
            } else {
               long seconds = parseTimeToSeconds(input);
               if (seconds < 0L) {
                  player.sendMessage(ChatColor.RED + "Invalid format. Use 1s, 3m, 1h, 1d, 1w, 1mn, 1y.");
                  awaitingExpireTimeInput.add(playerUUID);
                  player.sendMessage(ChatColor.GREEN + "Please try again, or type 'cancel' to exit.");
                  return;
               }

               plugin.getExpirationManager().setExpiration(codeName, seconds);
               player.sendMessage(ChatColor.AQUA + "Expire time set to " + input + " (" + seconds + " seconds).");
            }

            open(player);
         });
      } else if (awaitingPermissionInput.contains(playerUUID)) {
         event.setCancelled(true);
         String permission = event.getMessage().trim();
         Bukkit.getScheduler().runTask(plugin, () -> {
            awaitingPermissionInput.remove(playerUUID);
            if (permission.equalsIgnoreCase("cancel")) {
               player.sendMessage(ChatColor.RED + "Permission addition cancelled.");
            } else {
               List<String> permList = plugin.getCodesConfig().getStringList("Codes." + codeName + ".permisson.list");
               if (permList.contains(permission)) {
                  player.sendMessage(ChatColor.YELLOW + "Permission '" + permission + "' is already in the list.");
               } else {
                  permList.add(permission);
                  plugin.getCodesConfig().set("Codes." + codeName + ".permisson.list", permList);
                  plugin.saveCodesConfig();
                  player.sendMessage(ChatColor.AQUA + "Permission '" + permission + "' added to the list.");
               }
            }

            open(player);
         });
      } else if (awaitingRemoveConfirm.contains(playerUUID)) {
         event.setCancelled(true);
         String confirmation = ChatColor.stripColor(event.getMessage()).trim();
         Bukkit.getScheduler().runTask(plugin, () -> {
            awaitingRemoveConfirm.remove(playerUUID);
            if (confirmation.equalsIgnoreCase("confirm")) {
               player.sendMessage(ChatColor.GREEN + "Executing removal command...");
               player.performCommand("rc remove " + codeName);
               plugin.openEditorGUIs.remove(player);
            } else {
               player.sendMessage(ChatColor.RED + "Code removal cancelled.");
               open(player);
            }

         });
      }
   }

   private long parseTimeToMinutes(String input) {
      Pattern p = Pattern.compile("(\\d+)\\s*(s|m|h|d|w|mn|y)", 2);
      Matcher m = p.matcher(input.toLowerCase().trim());
      if (!m.matches()) {
         return -1L;
      } else {
         long value = Long.parseLong(m.group(1));
         switch (m.group(2)) {
            case "s" -> {
               return value / 60L;
            }
            case "m" -> {
               return value;
            }
            case "h" -> {
               return value * 60L;
            }
            case "d" -> {
               return value * 60L * 24L;
            }
            case "w" -> {
               return value * 60L * 24L * 7L;
            }
            case "mn" -> {
               return value * 60L * 24L * 30L;
            }
            case "y" -> {
               return value * 60L * 24L * 365L;
            }
            default -> {
               return -1L;
            }
         }
      }
   }

   private long parseTimeToSeconds(String input) {
      Pattern p = Pattern.compile("(\\d+)\\s*(s|m|h|d|w|mn|y)", 2);
      Matcher m = p.matcher(input.toLowerCase().trim());
      if (!m.matches()) {
         return -1L;
      } else {
         long value = Long.parseLong(m.group(1));
         switch (m.group(2)) {
            case "s" -> {
               return value;
            }
            case "m" -> {
               return value * 60L;
            }
            case "h" -> {
               return value * 60L * 60L;
            }
            case "d" -> {
               return value * 60L * 60L * 24L;
            }
            case "w" -> {
               return value * 60L * 60L * 24L * 7L;
            }
            case "mn" -> {
               return value * 60L * 60L * 24L * 30L;
            }
            case "y" -> {
               return value * 60L * 60L * 24L * 365L;
            }
            default -> {
               return -1L;
            }
         }
      }
   }
}





