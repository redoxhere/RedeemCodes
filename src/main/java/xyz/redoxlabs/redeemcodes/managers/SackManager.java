package xyz.redoxlabs.redeemcodes.managers;

import xyz.redoxlabs.redeemcodes.Main;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SackManager implements Listener {
   private final Main plugin;
   private final File sacksFolder;
   private final Map<UUID, String> editors = new HashMap<>();

   public SackManager(Main plugin) {
      this.plugin = plugin;
      this.sacksFolder = new File(plugin.getDataFolder(), "sacks");
      if (!sacksFolder.exists()) {
         sacksFolder.mkdirs();
      }

      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public boolean createSack(String name) {
      File file = new File(sacksFolder, name + ".yml");
      if (file.exists()) {
         return false;
      } else {
         try {
            file.createNewFile();
            return true;
         } catch (IOException e) {
            plugin.getLogger().severe("Could not create sack file: " + e.getMessage());
            return false;
         }
      }
   }

   public boolean deleteSack(String name) {
      File file = new File(sacksFolder, name + ".yml");
      return file.exists() ? file.delete() : false;
   }

   public boolean sackExists(String name) {
      return (new File(sacksFolder, name + ".yml")).exists();
   }

   public void openEditGUI(Player player, String name) {
      if (!sackExists(name)) {
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.not-exist", "&#FF6347Sack '&#E0E0E0%name%&#FF6347' does not exist.").replace("%name%", name));
      } else {
         File file = new File(sacksFolder, name + ".yml");
         FileConfiguration config = YamlConfiguration.loadConfiguration(file);
         xyz.redoxlabs.redeemcodes.utils.GUIHolder holder = new xyz.redoxlabs.redeemcodes.utils.GUIHolder("SACK_EDITOR");
         Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(ChatColor.translateAlternateColorCodes('&', "&8🎒 ꜱᴀᴄᴋ: &e" + name)));
         holder.setInventory(inv);
         if (config.contains("contents")) {
            List<?> list = config.getList("contents");
            if (list != null) {
               ItemStack[] contents = (ItemStack[])list.toArray(new ItemStack[0]);
               inv.setContents(contents);
            }
         }

         editors.put(player.getUniqueId(), name);
         player.openInventory(inv);
         xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.editing", "&#32CD32Editing sack '&#00BFFF%name%&#32CD32'. Close inventory to save.").replace("%name%", name));
      }
   }

   public void giveSack(Player player, String name) {
      if (!sackExists(name)) {
         plugin.getLogger().warning("Tried to give non-existent sack: " + name);
      } else {
         File file = new File(sacksFolder, name + ".yml");
         FileConfiguration config = YamlConfiguration.loadConfiguration(file);
         if (config.contains("contents")) {
            List<?> list = config.getList("contents");
            if (list != null) {
               for(Object obj : list) {
                  if (obj instanceof ItemStack) {
                     ItemStack item = (ItemStack)obj;
                     HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{item});

                     for(ItemStack drop : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                     }
                  }
               }
            }
         }

      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      if (editors.containsKey(player.getUniqueId())) {
         String sackName = (String)editors.remove(player.getUniqueId());
         if (event.getInventory().getHolder() instanceof xyz.redoxlabs.redeemcodes.utils.GUIHolder) {
            xyz.redoxlabs.redeemcodes.utils.GUIHolder holder = (xyz.redoxlabs.redeemcodes.utils.GUIHolder) event.getInventory().getHolder();
            if ("SACK_EDITOR".equals(holder.getGuiType())) {
               saveSack(sackName, event.getInventory());
               xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.sack.saved", "&#32CD32Sack '&#00BFFF%name%&#32CD32' updated and saved!").replace("%name%", sackName));
               xyz.redoxlabs.redeemcodes.utils.SoundUtil.playClick(plugin, player);
            }
         }
      }
   }

   private void saveSack(String name, Inventory inv) {
      File file = new File(sacksFolder, name + ".yml");
      FileConfiguration config = YamlConfiguration.loadConfiguration(file);
      config.set("contents", inv.getContents());
      final String dump = config.saveToString();

      ((xyz.redoxlabs.redeemcodes.Main) plugin).getFoliaLib().getImpl().runAsync((task) -> {
         try {
            java.nio.file.Files.write(file.toPath(), dump.getBytes(java.nio.charset.StandardCharsets.UTF_8));
         } catch (IOException e) {
            plugin.getLogger().severe("Could not save sack: " + name);
            e.printStackTrace();
         }
      });
   }

   public String[] getSackNames() {
      String[] list = sacksFolder.list((dir, name) -> name.endsWith(".yml"));
      if (list == null) {
         return new String[0];
      } else {
         for(int i = 0; i < list.length; ++i) {
            list[i] = list[i].replace(".yml", "");
         }

         return list;
      }
   }
}



