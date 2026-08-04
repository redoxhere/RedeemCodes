package xyz.redoxlabs.redeemcodes.guis;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.cryptomorin.xseries.XSound;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;

public class EventGUI implements Listener {
   private final Main plugin;
   private final Map<UUID, String> editingFirework = new HashMap<>();
   private final Map<UUID, String> editingSounds = new HashMap<>();
   private final Map<UUID, SoundEditSession> awaitingDelayInput = new HashMap<>();
   private final Map<UUID, SoundEditSession> awaitingPitchInput = new HashMap<>();
   private final Map<UUID, Integer> soundPickerPage = new HashMap<>();
   private final Map<UUID, Integer> soundListPage = new HashMap<>();
   private static final int SOUNDS_PER_PAGE = 45;
   private static final int SOUND_LIST_PER_PAGE = 28;
   private final List<XSound> allSounds;

   public EventGUI(Main plugin) {
      this.plugin = plugin;
      this.allSounds = getAllSounds();
      allSounds.sort((s1, s2) -> s1.name().compareTo(s2.name()));
   }

   private List<XSound> getAllSounds() {
      return java.util.Arrays.asList(XSound.values());
   }

   public void openFireworkEditor(Player player, String eventName) {
      FileConfiguration config = plugin.getEventManager().getEventConfig(eventName);
      List<?> existing = config.getList("fireworks");
      int size = 9;
      if (existing != null && !existing.isEmpty()) {
         size = (int)(Math.ceil((double)existing.size() / (double)9.0F) * (double)9.0F);
         if (size < 9) {
            size = 9;
         }

         if (size > 54) {
            size = 54;
         }
      } else {
         size = 9;
      }

      GUIHolder holder = new GUIHolder("EVENT_FIREWORKS");
      Inventory inv = Bukkit.createInventory(holder, size, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8📅 ᴇᴠᴇɴᴛ ꜰɪʀᴇᴡᴏʀᴋꜱ: &b" + eventName)));
      holder.setInventory(inv);
      if (existing != null) {
         ItemStack[] contents = (ItemStack[])existing.toArray(new ItemStack[0]);

         for(int i = 0; i < Math.min(contents.length, size); ++i) {
            inv.setItem(i, contents[i]);
         }
      }

      editingFirework.put(player.getUniqueId(), eventName);
      player.openInventory(inv);
      player.sendMessage(ChatColor.GREEN + "Add Firework Rockets here. Close to save.");
   }

   public void openSoundList(Player player, String eventName) {
      int page = (Integer)soundListPage.getOrDefault(player.getUniqueId(), 0);
      GUIHolder holder = new GUIHolder("EVENT_SOUNDS");
      Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8📅 ᴇᴠᴇɴᴛ ꜱᴏᴜɴᴅꜱ: &b" + eventName)));
      holder.setInventory(inv);
      GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);

      FileConfiguration config = plugin.getEventManager().getEventConfig(eventName);
      ConfigurationSection section = config.getConfigurationSection("sounds");
      List<String> keys = section != null ? new ArrayList<>(section.getKeys(false)) : new ArrayList<>();
      int start = page * 28;
      int end = Math.min(start + 28, keys.size());
      int index = 0;

      for(int row = 1; row <= 4 && start + index < end; ++row) {
         for(int col = 1; col <= 7 && start + index < end; ++col) {
            int slot = row * 9 + col;
            String key = (String)keys.get(start + index);
            String soundName = section.getString(key + ".sound");
            int delay = section.getInt(key + ".delay");
            double pitch = section.getDouble(key + ".pitch", (double)1.0F);
            ItemStack item = HeadManager.getHead("SOUND_ITEM", ChatColor.AQUA + soundName);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
               List<String> lore = new ArrayList<>();
               lore.add(ChatColor.GRAY + "Wait time: " + ChatColor.YELLOW + delay + " ticks");
               lore.add(ChatColor.GRAY + "Pitch: " + ChatColor.YELLOW + pitch);
               lore.add("");
               lore.add(ChatColor.YELLOW + "Left-Click " + ChatColor.GRAY + "to set wait time");
               lore.add(ChatColor.YELLOW + "Right-Click " + ChatColor.GRAY + "to set pitch");
               lore.add(ChatColor.RED + "Shift+Left-Click " + ChatColor.GRAY + "to delete");
               lore.add(ChatColor.BLACK + "id:" + key);
               meta.setLore(lore);
               item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            ++index;
         }
      }

      if (page > 0) {
         inv.setItem(45, HeadManager.getHead("PREV_PAGE", ChatColor.GRAY + "Previous Page"));
      }

      ItemStack addBtn = HeadManager.getHead("ADD_SOUND", ChatColor.GREEN + "Add New Sound", ChatColor.GRAY + "Click to browse sounds");
      inv.setItem(49, addBtn);
      if (end < keys.size()) {
         inv.setItem(53, HeadManager.getHead("NEXT_PAGE", ChatColor.GRAY + "Next Page"));
      }

      GUIUtils.applyFlags(inv);

      editingSounds.put(player.getUniqueId(), eventName);
      player.openInventory(inv);
   }

   public void openSoundPicker(Player player, int page) {
      String eventName = (String)editingSounds.get(player.getUniqueId());
      if (eventName != null) {
         soundPickerPage.put(player.getUniqueId(), page);
         GUIHolder holder = new GUIHolder("PICK_SOUND");
         Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8📅 ᴘɪᴄᴋ ꜱᴏᴜɴᴅ: &b" + eventName)));
         holder.setInventory(inv);
         int start = page * 45;
         int end = Math.min(start + 45, allSounds.size());

         for(int i = start; i < end; ++i) {
            XSound sound = allSounds.get(i);
            ItemStack item = XMaterial.PAPER.parseItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
               meta.setDisplayName(ChatColor.GOLD + sound.name());
               meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to add"));
               item.setItemMeta(meta);
            }

            inv.setItem(i - start, item);
         }

         if (page > 0) {
            inv.setItem(45, HeadManager.getHead("PREV_PAGE", ChatColor.YELLOW + "Previous Page"));
         }

         if (end < allSounds.size()) {
            inv.setItem(53, HeadManager.getHead("NEXT_PAGE", ChatColor.YELLOW + "Next Page"));
         }

         inv.setItem(49, HeadManager.getHead("BACK", ChatColor.RED + "Back to List"));

         GUIUtils.applyFlags(inv);

         player.openInventory(inv);
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         Player player = (Player)event.getPlayer();
         UUID uuid = player.getUniqueId();
         if (editingFirework.containsKey(uuid)) {
            String eventName = (String)editingFirework.remove(uuid);
            if (event.getInventory().getHolder() instanceof GUIHolder) {
               GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
               if ("EVENT_FIREWORKS".equals(holder.getGuiType())) {
                  List<ItemStack> content = new ArrayList<>();
   
                  for(ItemStack item : event.getInventory().getContents()) {
                     if (item != null && XMaterial.matchXMaterial(item) == XMaterial.FIREWORK_ROCKET) {
                        content.add(item);
                     }
                  }
   
                  FileConfiguration config = plugin.getEventManager().getEventConfig(eventName);
                  config.set("fireworks", content);
                  plugin.getEventManager().saveEvent(eventName);
                  player.sendMessage(ChatColor.GREEN + "Fireworks saved for event '" + eventName + "'.");
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         if (event.getInventory().getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
            String id = holder.getGuiType();
            
            if ("EVENT_SOUNDS".equals(id)) {
               event.setCancelled(true);
               String eventName = (String)editingSounds.get(player.getUniqueId());
               if (eventName == null) return;
   
               ItemStack clicked = event.getCurrentItem();
               if (clicked == null || !clicked.hasItemMeta()) return;
   
               String strippedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
               if (strippedName.equals("Add New Sound")) {
                  SoundUtil.playClick(plugin, player);
                  openSoundPicker(player, 0);
               } else if (strippedName.equals("Next Page")) {
                  SoundUtil.playPageTurn(plugin, player);
                  int page = (Integer)soundListPage.getOrDefault(player.getUniqueId(), 0);
                  soundListPage.put(player.getUniqueId(), page + 1);
                  openSoundList(player, eventName);
               } else if (strippedName.equals("Previous Page")) {
                  int page = (Integer)soundListPage.getOrDefault(player.getUniqueId(), 0);
                  if (page > 0) {
                     SoundUtil.playPageTurn(plugin, player);
                     soundListPage.put(player.getUniqueId(), page - 1);
                     openSoundList(player, eventName);
                  } else {
                     SoundUtil.playError(plugin, player);
                  }
               } else {
                  List<String> lore = clicked.getItemMeta().getLore();
                  String soundId = null;
                  if (lore != null) {
                     for(String line : lore) {
                        if (line.contains("id:")) {
                           soundId = ChatColor.stripColor(line).replace("id:", "").trim();
                           break;
                        }
                     }
                  }
   
                  if (soundId != null) {
                     if (event.isLeftClick() && !event.isShiftClick()) {
                        SoundUtil.playClick(plugin, player);
                        plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                        awaitingDelayInput.put(player.getUniqueId(), new SoundEditSession(eventName, soundId));
                        player.sendMessage(ChatColor.GREEN + "Type the wait time in ticks (integer) in chat:");
                        player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                     } else if (event.isRightClick()) {
                        SoundUtil.playClick(plugin, player);
                        plugin.getFoliaLib().getImpl().runNextTick((task) -> player.closeInventory());
                        awaitingPitchInput.put(player.getUniqueId(), new SoundEditSession(eventName, soundId));
                        player.sendMessage(ChatColor.GREEN + "Type the pitch (0.0 - 2.0) in chat:");
                        player.sendMessage(ChatColor.GRAY + "Type 'cancel' to abort.");
                     } else if (event.isShiftClick() && event.isLeftClick()) {
                        FileConfiguration config = plugin.getEventManager().getEventConfig(eventName);
                        config.set("sounds." + soundId, (Object)null);
                        plugin.getEventManager().saveEvent(eventName);
                        openSoundList(player, eventName);
                        XSound.UI_BUTTON_CLICK.play(player);
                     }
                  }
               }
            } else if ("PICK_SOUND".equals(id)) {
               event.setCancelled(true);
               String eventName = (String)editingSounds.get(player.getUniqueId());
               if (eventName == null) return;
   
               ItemStack clicked = event.getCurrentItem();
               if (clicked == null || !clicked.hasItemMeta()) return;
   
               String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
               if (name.equals("Next Page")) {
                  SoundUtil.playPageTurn(plugin, player);
                  int page = (Integer)soundPickerPage.getOrDefault(player.getUniqueId(), 0);
                  openSoundPicker(player, page + 1);
               } else if (name.equals("Previous Page")) {
                  int page = (Integer)soundPickerPage.getOrDefault(player.getUniqueId(), 0);
                  if (page > 0) {
                     SoundUtil.playPageTurn(plugin, player);
                     openSoundPicker(player, page - 1);
                  } else {
                     SoundUtil.playError(plugin, player);
                  }
               } else if (name.equals("Back to List")) {
                  SoundUtil.playClick(plugin, player);
                  openSoundList(player, eventName);
               } else if (XMaterial.matchXMaterial(clicked) == XMaterial.PAPER) {
                  FileConfiguration config = plugin.getEventManager().getEventConfig(eventName);
                  String soundId = UUID.randomUUID().toString();
                  config.set("sounds." + soundId + ".sound", name);
                  config.set("sounds." + soundId + ".delay", 1);
                  config.set("sounds." + soundId + ".pitch", (double)1.0F);
                  plugin.getEventManager().saveEvent(eventName);
                  openSoundList(player, eventName);
                  XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
               }
            } else if ("EVENT_FIREWORKS".equals(id)) {

            }
         }
      }
   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (awaitingDelayInput.containsKey(uuid)) {
         event.setCancelled(true);
         String msg = event.getMessage().trim();
         SoundEditSession session = (SoundEditSession)awaitingDelayInput.remove(uuid);
         if (msg.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.RED + "Cancelled.");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
            return;
         }

         try {
            int delay = Integer.parseInt(msg);
            if (delay < 0) {
               delay = 0;
            }

            FileConfiguration config = plugin.getEventManager().getEventConfig(session.eventName);
            config.set("sounds." + session.soundId + ".delay", delay);
            plugin.getEventManager().saveEvent(session.eventName);
            player.sendMessage(ChatColor.GREEN + "Delay set to " + delay + " ticks.");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
         } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number. Operation cancelled.");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
         }
      } else if (awaitingPitchInput.containsKey(uuid)) {
         event.setCancelled(true);
         String msg = event.getMessage().trim();
         SoundEditSession session = (SoundEditSession)awaitingPitchInput.remove(uuid);
         if (msg.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.RED + "Cancelled.");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
            return;
         }

         try {
            double pitch = Double.parseDouble(msg);
            if (pitch < (double)0.0F) {
               pitch = (double)0.0F;
            }

            if (pitch > (double)2.0F) {
               pitch = (double)2.0F;
            }

            FileConfiguration config = plugin.getEventManager().getEventConfig(session.eventName);
            config.set("sounds." + session.soundId + ".pitch", pitch);
            plugin.getEventManager().saveEvent(session.eventName);
            player.sendMessage(ChatColor.GREEN + "Pitch set to " + pitch + ".");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
         } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number. Operation cancelled.");
            plugin.getFoliaLib().getImpl().runAtEntity(player, (task) -> openSoundList(player, session.eventName));
         }
      }

   }

   private static class SoundEditSession {
      String eventName;
      String soundId;

      public SoundEditSession(String eventName, String soundId) {
         this.eventName = eventName;
         this.soundId = soundId;
      }
   }
}




