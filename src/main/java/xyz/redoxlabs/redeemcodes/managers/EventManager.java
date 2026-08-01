package xyz.redoxlabs.redeemcodes.managers;

import xyz.redoxlabs.redeemcodes.Main;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class EventManager {
   private final Main plugin;
   private final File eventsFolder;
   private final Map<String, FileConfiguration> eventConfigs = new HashMap<>();
   private final Random random = new Random();

   public EventManager(Main plugin) {
      this.plugin = plugin;
      this.eventsFolder = new File(plugin.getDataFolder(), "events");
      if (!eventsFolder.exists()) {
         eventsFolder.mkdirs();
      }

      reloadEvents();
   }

   public void reloadEvents() {
      eventConfigs.clear();
      Set<String> loadedNames = new HashSet();
      File[] files = eventsFolder.listFiles((dir, namex) -> namex.endsWith(".yml"));
      if (files != null) {
         for(File file : files) {
            String name = file.getName().replace(".yml", "");
            String lowerName = name.toLowerCase();
            if (loadedNames.contains(lowerName)) {
               plugin.getLogger().warning("Duplicate event name detected (ignoring case): '" + name + "'. Skipping file: " + file.getName());
            } else {
               eventConfigs.put(name, YamlConfiguration.loadConfiguration(file));
               loadedNames.add(lowerName);
            }
         }
      }

      plugin.getLogger().info("Loaded " + eventConfigs.size() + " events.");
   }

   public boolean createEvent(String name) {
      if (eventConfigs.containsKey(name)) {
         return false;
      } else {
         File file = new File(eventsFolder, name + ".yml");

         try {
            file.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.createSection("fireworks");
            config.createSection("commands");
            config.createSection("sounds");
            config.save(file);
            eventConfigs.put(name, config);
            return true;
         } catch (IOException e) {
            e.printStackTrace();
            return false;
         }
      }
   }

   public boolean deleteEvent(String name) {
      if (!eventConfigs.containsKey(name)) {
         return false;
      } else {
         File file = new File(eventsFolder, name + ".yml");
         if (file.exists()) {
            file.delete();
         }

         eventConfigs.remove(name);
         return true;
      }
   }

   public Set<String> getEventNames() {
      return eventConfigs.keySet();
   }

   public boolean eventExists(String name) {
      return eventConfigs.containsKey(name);
   }

   public FileConfiguration getEventConfig(String name) {
      return (FileConfiguration)eventConfigs.get(name);
   }

   public void saveEvent(String name) {
      FileConfiguration config = (FileConfiguration)eventConfigs.get(name);
      if (config != null) {
         try {
            config.save(new File(eventsFolder, name + ".yml"));
         } catch (IOException e) {
            e.printStackTrace();
         }

      }
   }

   public void executeEvent(final Player player, String eventName) {
      if (!eventConfigs.containsKey(eventName)) {
         plugin.getLogger().warning("Tried to execute non-existent event: " + eventName);
      } else {
         FileConfiguration config = (FileConfiguration)eventConfigs.get(eventName);
         if (config.contains("fireworks")) {
            List<?> list = config.getList("fireworks");
            if (list != null) {
               long currentDelay = 0L;

               for(Object obj : list) {
                  if (obj instanceof ItemStack) {
                     final ItemStack item = (ItemStack)obj;
                     currentDelay += 5L;
                     (new BukkitRunnable() {
                        public void run() {
                           if (player.isOnline()) {
                              spawnFirework(player.getLocation(), item);
                           }
                        }
                     }).runTaskLater(plugin, currentDelay);
                  }
               }
            }
         }

         if (config.contains("sounds")) {
            ConfigurationSection sounds = config.getConfigurationSection("sounds");
            if (sounds != null) {
               for(String key : sounds.getKeys(false)) {
                  String soundName = sounds.getString(key + ".sound");
                  int delay = sounds.getInt(key + ".delay", 1);
                  final float pitch = (float)sounds.getDouble(key + ".pitch", (double)1.0F);

                  try {
                     final Sound sound = Sound.valueOf(soundName);
                     (new BukkitRunnable() {
                        public void run() {
                           if (player.isOnline()) {
                              player.playSound(player.getLocation(), sound, 1.0F, pitch);
                           }
                        }
                     }).runTaskLater(plugin, (long)delay);
                  } catch (IllegalArgumentException e) {
                  }
               }
            }
         }

         if (config.contains("commands")) {
            for(String cmd : config.getStringList("commands")) {
               String parsed = parsePlaceholders(cmd, player);
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
         }

      }
   }

   private void spawnFirework(Location loc, ItemStack item) {
      if (item.getType() == Material.FIREWORK_ROCKET) {
         Firework fw = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
         FireworkMeta meta = (FireworkMeta)item.getItemMeta();
         if (meta != null) {
            fw.setFireworkMeta(meta);
         }

      }
   }

   private String parsePlaceholders(String input, Player player) {
      input = input.replace("%player%", player.getName());
      input = input.replace("%uuid%", player.getUniqueId().toString());
      input = input.replace("%displayname%", player.getDisplayName());
      input = input.replace("%world%", player.getWorld().getKey().toString());
      input = input.replace("%x%", String.valueOf(player.getLocation().getX()));
      input = input.replace("%y%", String.valueOf(player.getLocation().getY()));
      input = input.replace("%z%", String.valueOf(player.getLocation().getZ()));
      Pattern randomPattern = Pattern.compile("%random-(\\d+)-(\\d+)%");
      Matcher matcher = randomPattern.matcher(input);
      StringBuffer sb = new StringBuffer();

      while(matcher.find()) {
         int min = Integer.parseInt(matcher.group(1));
         int max = Integer.parseInt(matcher.group(2));
         int result = random.nextInt(max - min + 1) + min;
         matcher.appendReplacement(sb, String.valueOf(result));
      }

      matcher.appendTail(sb);
      return sb.toString();
   }
}





