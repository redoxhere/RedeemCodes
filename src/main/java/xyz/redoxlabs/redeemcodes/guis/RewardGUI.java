package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;

public class RewardGUI {
   private final Main plugin;
   private final String codeName;
   private final CodeEditorGUI parentGUI;
   private final String rewardPath;
   private View currentView;
   private int page;
   public final Set<UUID> awaitingCommandPackName;
   public final Set<UUID> awaitingCommandForPack;
   public final Map<UUID, String> activePackForCommand;
   public final Set<UUID> awaitingWeightInput;
   public final Map<UUID, String> activeItemForWeight;
   private static final String pro = "§x§2§B§8§6§D§7§l| §x§F§F§F§F§F§F";
   private static final String pre = "§x§2§B§8§6§D§7";
   private static final int ITEMS_PER_PAGE = 28;

   public RewardGUI(Main plugin, String codeName, CodeEditorGUI parentGUI) {
      this.currentView = RewardGUI.View.MAIN;
      this.page = 0;
      this.awaitingCommandPackName = new HashSet<>();
      this.awaitingCommandForPack = new HashSet<>();
      this.activePackForCommand = new HashMap<>();
      this.awaitingWeightInput = new HashSet<>();
      this.activeItemForWeight = new HashMap<>();
      this.plugin = plugin;
      this.codeName = codeName;
      this.parentGUI = parentGUI;
      this.rewardPath = "Codes." + codeName + ".rewards";
   }

   public void openMain(Player player) {
      this.currentView = RewardGUI.View.MAIN;
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 27, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎁 ʀᴇᴡᴀʀᴅꜱ")));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      inv.setItem(11, HeadManager.getHead("REWARD_ADD", "§x§F§B§E§2§D§EA§x§F§C§E§0§D§9d§x§F§C§D§E§D§3d §x§F§D§D§C§C§ER§x§F§D§D§A§C§8e§x§F§E§D§7§C§3w§x§F§E§D§5§B§Da§x§F§F§D§3§B§8r§x§F§F§D§1§B§2d", " ", "§x§F§B§E§2§D§E§l| §fᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴅ ᴀ ɴᴇᴡ ʀᴇᴡᴀʀᴅ"));
      List<String> events = plugin.getCodesConfig().getStringList(rewardPath + ".events");
      String currentEvent = events.isEmpty() ? "None" : (String)events.get(0);
      inv.setItem(13, HeadManager.getHead("REWARD_EVENT", "§x§B§4§0§0§F§BS§x§B§4§1§1§F§Ce§x§B§5§2§1§F§Ct §x§B§5§3§2§F§DE§x§B§6§4§3§F§Dv§x§B§6§5§4§F§Ee§x§B§7§6§4§F§En§x§B§7§7§5§F§Ft", "", "§x§B§4§0§0§F§B§l| §fᴄᴜʀʀᴇɴᴛ: §x§B§4§0§0§F§B" + currentEvent, "§x§B§4§0§0§F§B§l|§f ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇʟᴇᴄᴛ ᴀɴ ᴇᴠᴇɴᴛ"));
      String type = plugin.getCodesConfig().getString(rewardPath + ".type", "RANDOM");
      inv.setItem(15, HeadManager.getHead("REWARD_TYPE", "§x§2§B§8§6§D§7Distribution Type", " ", "§x§2§B§8§6§D§7§l| §fᴄᴜʀʀᴇɴᴛ: §x§2§B§8§6§D§7" + type, "§x§2§B§8§6§D§7§l| §fᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ᴛʏᴘᴇ"));
      inv.setItem(22, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Code Editor"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }

   public void openAddSelection(Player player) {
      this.currentView = RewardGUI.View.ADD_SELECTION;
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 27, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎁 ᴀᴅᴅ ʀᴇᴡᴀʀᴅ")));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      inv.setItem(11, HeadManager.getHead("REWARD_COMMAND", "§x§F§B§9§D§4§ECommand Pack", " ", "§x§F§B§9§D§4§E§l| §fᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴄᴏᴍᴍᴀɴᴅ ᴘᴀᴄᴋꜱ"));
      inv.setItem(13, HeadManager.getHead("REWARD_SACK", "§x§4§5§D§1§5§8Sack", " ", "§x§4§5§D§1§5§8§l| §fᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ꜱᴀᴄᴋꜱ"));
      inv.setItem(15, HeadManager.getHead("REWARD_PREMADE", "§x§6§E§B§1§D§4Premade", " ", "§x§6§E§B§1§D§4§l| §fᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴘʀᴇᴍᴀᴅᴇꜱ"));
      inv.setItem(22, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Main Reward Menu"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }

   public void openCommandList(Player player, int page) {
      this.currentView = RewardGUI.View.COMMAND_LIST;
      this.page = page;
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎁 ᴄᴏᴍᴍᴀɴᴅ ᴘᴀᴄᴋꜱ")));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      ConfigurationSection section = plugin.getCodesConfig().getConfigurationSection(rewardPath + ".commands");
      List<String> packs = section != null ? new ArrayList<>(section.getKeys(false)) : new ArrayList<>();
      addPagination(inv, packs, page, "COMMAND_PACK", (item, packName) -> {
         List<String> cmds = section.getStringList(packName);
         ItemMeta meta = item.getItemMeta();
         List<String> lore = new ArrayList<>();
         lore.add("§7ᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴅ ᴄᴏᴍᴍᴀɴᴅ");
         lore.add("§7ꜱʜɪꜰᴛ+ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ ᴘᴀᴄᴋ");
         lore.add("");
         lore.add("§x§9§5§6§9§F§B§l| §7ᴄᴏᴍᴍᴀɴᴅꜱ: §x§9§5§6§9§F§B" + cmds.size());

         for(int i = 0; i < Math.min(5, cmds.size()); ++i) {
            lore.add("§x§9§5§6§9§F§B- " + cmds.get(i));
         }

         if (cmds.size() > 5) {
            lore.add("§x§9§5§6§9§F§B... and " + (cmds.size() - 5) + " more");
         }

         lore.add("");
         meta.setLore(lore);
         meta.setDisplayName("§x§9§5§6§9§F§B" + packName);
         item.setItemMeta(meta);
      });
      inv.setItem(49, HeadManager.getHead("GENERIC_ADD", "§aAdd New Pack", "§7Click to create a new command pack"));
      inv.setItem(45, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Add Selection"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }

   public void openSackList(Player player, int page) {
      this.currentView = RewardGUI.View.SACK_LIST;
      this.page = page;
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎁 ꜱᴀᴄᴋ ʀᴇᴡᴀʀᴅꜱ")));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      List<String> sacks = plugin.getCodesConfig().getStringList(rewardPath + ".sacks");
      addPagination(inv, sacks, page, "REWARD_SACK", (item, entry) -> {
         String[] parts = entry.split(":");
         String name = parts[0];
         String weight = parts.length > 1 ? parts[1] : "1";
         ItemMeta meta = item.getItemMeta();
         List<String> lore = new ArrayList<>();
         lore.add("§7ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ᴡᴇɪɢʜᴛ");
         lore.add("§7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ/ᴇᴅɪᴛ ɪᴛᴇᴍꜱ");
         lore.add("§7ꜱʜɪꜰᴛ+ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ");
         lore.add("");
         lore.add("§x§4§5§D§1§5§8§l| §7ᴡᴇɪɢʜᴛ: §x§4§5§D§1§5§8" + weight);
         lore.add("");
         meta.setLore(lore);
         meta.setDisplayName("§x§4§5§D§1§5§8" + name);
         item.setItemMeta(meta);
      });
      inv.setItem(49, HeadManager.getHead("GENERIC_ADD", "§aAdd Sack", "§7Click to add existing sack"));
      inv.setItem(45, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Add Selection"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }

   public void openPremadeList(Player player, int page) {
      this.currentView = RewardGUI.View.PREMADE_LIST;
      this.page = page;
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎁 ᴘʀᴇᴍᴀᴅᴇ ʀᴇᴡᴀʀᴅꜱ")));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      List<String> premades = plugin.getCodesConfig().getStringList(rewardPath + ".premades");
      addPagination(inv, premades, page, "REWARD_PREMADE", (item, entry) -> {
         String[] parts = entry.split(":");
         String name = parts[0];
         String weight = parts.length > 1 ? parts[1] : "1";
         ItemMeta meta = item.getItemMeta();
         List<String> lore = new ArrayList<>();
         lore.add("§7ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ꜱᴇᴛ ᴡᴇɪɢʜᴛ");
         lore.add("§7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄᴏᴍᴍᴀɴᴅꜱ");
         lore.add("§7ꜱʜɪꜰᴛ+ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ");
         lore.add("");
         lore.add("§x§6§E§B§1§D§4§l| §7ᴡᴇɪɢʜᴛ: §x§6§E§B§1§D§4" + weight);
         lore.add("");
         meta.setLore(lore);
         meta.setDisplayName("§x§6§E§B§1§D§4" + name);
         item.setItemMeta(meta);
      });
      inv.setItem(49, HeadManager.getHead("GENERIC_ADD", "§aAdd Premade", "§7Click to add existing premade"));
      inv.setItem(45, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Add Selection"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }

   public void openSelector(Player player, View view, int page) {
      this.currentView = view;
      this.page = page;
      String title = view == RewardGUI.View.EVENT_SELECTOR ? "&8🎁 ꜱᴇʟᴇᴄᴛ ᴇᴠᴇɴᴛ" : (view == RewardGUI.View.SACK_SELECTOR ? "&8🎁 ꜱᴇʟᴇᴄᴛ ꜱᴀᴄᴋ" : "&8🎁 ꜱᴇʟᴇᴄᴛ ᴘʀᴇᴍᴀᴅᴇ");
      GUIHolder holder = new GUIHolder("REWARD_GUI");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', title)));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);
      List<String> available;
      String headKey;
      if (view == RewardGUI.View.EVENT_SELECTOR) {
         available = new ArrayList(plugin.getEventManager().getEventNames());
         headKey = "REWARD_EVENT";
      } else if (view == RewardGUI.View.SACK_SELECTOR) {
         available = Arrays.asList(plugin.getSackManager().getSackNames());
         headKey = "REWARD_SACK";
      } else {
         available = new ArrayList<>(plugin.getPremadeManager().getPremadeNames());
         headKey = "REWARD_PREMADE";
      }

      addPagination(inv, available, page, headKey, (item, name) -> {
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName("§a" + name);
         meta.setLore(Collections.singletonList("§7Click to add"));
         item.setItemMeta(meta);
      });
      inv.setItem(45, HeadManager.getHead("BACK", "§cGo Back", "§7Return to List"));
      GUIUtils.applyFlags(inv);
      player.openInventory(inv);
   }


   private void addPagination(Inventory inv, List<String> items, int page, String headKey, ItemConfigurator configurator) {
      int start = page * 28;
      int end = Math.min(start + 28, items.size());
      int index = 0;

      for(int row = 1; row <= 4; ++row) {
         for(int col = 1; col <= 7 && start + index < end; ++col) {
            int slot = row * 9 + col;
            String data = (String)items.get(start + index);
            ItemStack item = HeadManager.getHead(headKey, data);
            configurator.configure(item, data);
            inv.setItem(slot, item);
            ++index;
         }
      }

      if (page > 0) {
         inv.setItem(48, HeadManager.getHead("PREV_PAGE", "§7Previous Page"));
      }

      if (end < items.size()) {
         inv.setItem(53, HeadManager.getHead("NEXT_PAGE", "§7Next Page"));
      }

   }

   public void handleClick(InventoryClickEvent event, Player player) {
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.hasItemMeta()) {
         String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
         event.setCancelled(true);
         if (name.equals("Go Back")) {
            SoundUtil.playClick(plugin, player);
            if (currentView == RewardGUI.View.MAIN) {
               plugin.openRewardGUIs.remove(player);
               parentGUI.open(player);
            } else if (currentView == RewardGUI.View.ADD_SELECTION) {
               openMain(player);
            } else if (currentView != RewardGUI.View.COMMAND_LIST && currentView != RewardGUI.View.SACK_LIST && currentView != RewardGUI.View.PREMADE_LIST) {
               if (currentView == RewardGUI.View.EVENT_SELECTOR) {
                  openMain(player);
               } else if (currentView == RewardGUI.View.SACK_SELECTOR) {
                  openSackList(player, 0);
               } else if (currentView == RewardGUI.View.PREMADE_SELECTOR) {
                  openPremadeList(player, 0);
               }
            } else {
               openAddSelection(player);
            }

         } else if (name.equals("Next Page")) {
            SoundUtil.playPageTurn(plugin, player);
            refreshView(player, page + 1);
         } else if (name.equals("Previous Page")) {
            SoundUtil.playPageTurn(plugin, player);
            refreshView(player, Math.max(0, page - 1));
         } else {
            SoundUtil.playClick(plugin, player);
            if (currentView == RewardGUI.View.MAIN) {
               if (name.equals("Add Reward")) {
                  openAddSelection(player);
               } else if (name.equals("Set Event")) {
                  openSelector(player, RewardGUI.View.EVENT_SELECTOR, 0);
               } else if (name.equals("Distribution Type")) {
                  String current = plugin.getCodesConfig().getString(rewardPath + ".type", "RANDOM");
                  String next = current.equals("RANDOM") ? "ALL" : (current.equals("ALL") ? "DRAW" : "RANDOM");
                  plugin.getCodesConfig().set(rewardPath + ".type", next);
                  plugin.saveCodesConfig();
                  openMain(player);
               }
            } else if (currentView == RewardGUI.View.ADD_SELECTION) {
               if (name.equals("Command Pack")) {
                  openCommandList(player, 0);
               } else if (name.equals("Sack")) {
                  openSackList(player, 0);
               } else if (name.equals("Premade")) {
                  openPremadeList(player, 0);
               }
            } else if (currentView == RewardGUI.View.COMMAND_LIST) {
               if (name.equals("Add New Pack")) {
                  player.closeInventory();
                  awaitingCommandPackName.add(player.getUniqueId());
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.prompt-pack-name", "&#00BFFFPlease type the name for the new command pack in chat.\n&#E0E0E0Type 'cancel' to abort."));
               } else if (XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                  String packName = ChatColor.stripColor(name);
                  if (event.isShiftClick()) {
                     plugin.getCodesConfig().set(rewardPath + ".commands." + packName, (Object)null);
                     plugin.saveCodesConfig();
                     openCommandList(player, page);
                  } else {
                     player.closeInventory();
                     activePackForCommand.put(player.getUniqueId(), packName);
                     awaitingCommandForPack.add(player.getUniqueId());
                     xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.prompt-pack-cmd", "&#00BFFFPlease type the command to add to pack '&#E0E0E0") + packName + "&#00BFFF' in chat.\n&#E0E0E0Type 'cancel' to abort.");
                  }
               }
            } else if (currentView != RewardGUI.View.SACK_LIST && currentView != RewardGUI.View.PREMADE_LIST) {
               if (currentView != RewardGUI.View.SACK_SELECTOR && currentView != RewardGUI.View.PREMADE_SELECTOR) {
                  if (currentView == RewardGUI.View.EVENT_SELECTOR && XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                     String eventName = ChatColor.stripColor(name);
                     List<String> list = new ArrayList<>();
                     list.add(eventName);
                     plugin.getCodesConfig().set(rewardPath + ".events", list);
                     plugin.saveCodesConfig();
                     openMain(player);
                  }
               } else if (XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                  String itemName = ChatColor.stripColor(name);
                  boolean isSack = currentView == RewardGUI.View.SACK_SELECTOR;
                  String listKey = isSack ? "sacks" : "premades";
                  List<String> list = plugin.getCodesConfig().getStringList(rewardPath + "." + listKey);
                  boolean exists = list.stream().anyMatch((s) -> s.split(":")[0].equals(itemName));
                  if (!exists) {
                     list.add(itemName + ":1");
                     plugin.getCodesConfig().set(rewardPath + "." + listKey, list);
                     plugin.saveCodesConfig();
                  }

                  if (isSack) {
                     openSackList(player, 0);
                  } else {
                     openPremadeList(player, 0);
                  }
               }
            } else {
               boolean isSack = currentView == RewardGUI.View.SACK_LIST;
               String listKey = isSack ? "sacks" : "premades";
               if (!name.equals("Add Sack") && !name.equals("Add Premade")) {
                  if (XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                     String itemName = ChatColor.stripColor(name);
                     if (event.isRightClick()) {
                        activeItemForWeight.put(player.getUniqueId(), (isSack ? "sack:" : "premade:") + itemName);
                        player.closeInventory();
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.prompt-weight", "&#00BFFFPlease type the new weight for &#E0E0E0") + itemName + "&#00BFFF in chat.\n&#E0E0E0Type 'cancel' to abort.");
                     } else if (event.isShiftClick()) {
                        List<String> list = plugin.getCodesConfig().getStringList(rewardPath + "." + listKey);
                        list.removeIf((s) -> s.split(":")[0].equals(itemName));
                        plugin.getCodesConfig().set(rewardPath + "." + listKey, list);
                        plugin.saveCodesConfig();
                        refreshView(player, page);
                     } else if (isSack) {
                        plugin.getSackManager().openEditGUI(player, itemName);
                     } else {
                        plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                        player.performCommand("rc premade view " + itemName);
                     }
                  }
               } else {
                  openSelector(player, isSack ? RewardGUI.View.SACK_SELECTOR : RewardGUI.View.PREMADE_SELECTOR, 0);
               }
            }

         }
      }
   }

   private void refreshView(Player player, int page) {
      switch (currentView) {
         case COMMAND_LIST:
            openCommandList(player, page);
            break;
         case SACK_LIST:
            openSackList(player, page);
            break;
         case PREMADE_LIST:
            openPremadeList(player, page);
            break;
         case SACK_SELECTOR:
            openSelector(player, RewardGUI.View.SACK_SELECTOR, page);
            break;
         case PREMADE_SELECTOR:
            openSelector(player, RewardGUI.View.PREMADE_SELECTOR, page);
            break;
         case EVENT_SELECTOR:
            openSelector(player, RewardGUI.View.EVENT_SELECTOR, page);
            break;
         default:
            openMain(player);
            break;
      }

   }

   public void handleChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      String rawMsg = event.getMessage();
      String msg = ChatColor.stripColor(rawMsg).trim();
      UUID uuid = player.getUniqueId();
      if (msg.equalsIgnoreCase("cancel")) {
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.cancel", "&#FF6347Action cancelled."));
         cleanupChat(uuid);
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            if (activeItemForWeight.containsKey(uuid)) {
               String type = ((String)activeItemForWeight.remove(uuid)).split(":")[0];
               if (type.equals("sack")) {
                  openSackList(player, 0);
               } else {
                  openPremadeList(player, 0);
               }
            } else if (activePackForCommand.containsKey(uuid)) {
               activePackForCommand.remove(uuid);
               openCommandList(player, 0);
            } else {
               openCommandList(player, 0);
            }

         });
      } else {
         plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> {
            if (awaitingCommandPackName.contains(uuid)) {
               String packName = msg.replace(" ", "");
               if (!packName.matches("^[a-zA-Z0-9_]+$")) {
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.invalid-name", "&#FF6347Invalid pack name! Use only letters, numbers, and underscores."));
                  return;
               }

               plugin.getCodesConfig().set(rewardPath + ".commands." + packName, new ArrayList<>());
               plugin.saveCodesConfig();
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.pack-created", "&#32CD32Created new command pack: &#00BFFF") + packName);
               awaitingCommandPackName.remove(uuid);
               openCommandList(player, 0);
            } else if (awaitingCommandForPack.contains(uuid)) {
               String pack = (String)activePackForCommand.get(uuid);
               if (pack != null) {
                  List<String> list = plugin.getCodesConfig().getStringList(rewardPath + ".commands." + pack);
                  list.add(rawMsg.trim());
                  plugin.getCodesConfig().set(rewardPath + ".commands." + pack, list);
                  plugin.saveCodesConfig();
                  xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.cmd-added", "&#32CD32Added command to pack &#00BFFF") + pack);
               }

               awaitingCommandForPack.remove(uuid);
               activePackForCommand.remove(uuid);
               openCommandList(player, 0);
            } else if (awaitingWeightInput.contains(uuid)) {
               String raw = (String)activeItemForWeight.get(uuid);
               if (raw != null) {
                  try {
                     int weight = Integer.parseInt(msg);
                     String[] parts = raw.split(":");
                     String type = parts[0];
                     String name = parts[1];
                     String listKey = type.equals("sack") ? "sacks" : "premades";
                     List<String> list = plugin.getCodesConfig().getStringList(rewardPath + "." + listKey);

                     for(int i = 0; i < list.size(); ++i) {
                        if (((String)list.get(i)).split(":")[0].equals(name)) {
                           list.set(i, name + ":" + weight);
                           break;
                        }
                     }

                     plugin.getCodesConfig().set(rewardPath + "." + listKey, list);
                     plugin.saveCodesConfig();
                     xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.weight-updated", "&#32CD32Weight updated to &#00BFFF") + weight);
                     awaitingWeightInput.remove(uuid);
                     activeItemForWeight.remove(uuid);
                     if (type.equals("sack")) {
                        openSackList(player, 0);
                     } else {
                        openPremadeList(player, 0);
                     }
                  } catch (NumberFormatException e) {
                     xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.rewards.invalid-weight", "&#FF6347Invalid number! Please try again or type 'cancel' to abort."));
                  }
               }
            }

         });
      }
   }

   private void cleanupChat(UUID uuid) {
      awaitingCommandPackName.remove(uuid);
      awaitingCommandForPack.remove(uuid);
      awaitingWeightInput.remove(uuid);
   }

   private static enum View {
      MAIN,
      ADD_SELECTION,
      COMMAND_LIST,
      SACK_LIST,
      PREMADE_LIST,
      EVENT_SELECTOR,
      SACK_SELECTOR,
      PREMADE_SELECTOR;


      private static View[] $values() {
         return new View[]{MAIN, ADD_SELECTION, COMMAND_LIST, SACK_LIST, PREMADE_LIST, EVENT_SELECTOR, SACK_SELECTOR, PREMADE_SELECTOR};
      }
   }

   private interface ItemConfigurator {
      void configure(ItemStack item, String packName);
   }
}




