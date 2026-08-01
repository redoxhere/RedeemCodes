package xyz.redoxlabs.redeemcodes.utils;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class DefaultExampleGenerator {
   private final JavaPlugin plugin;

   public DefaultExampleGenerator(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void generate() {
      generateResource("sacks/miner.yml");
      generateResource("sacks/starter.yml");
      generateResource("sacks/warrior.yml");
      generateResource("events/alert.yml");
      generateResource("events/celebration.yml");
      generateResource("events/dungeon.yml");
   }

   private void generateResource(String path) {
      File file = new File(plugin.getDataFolder(), path);
      if (!file.exists()) {
         if (plugin.getResource(path) != null) {
            plugin.saveResource(path, false);
            plugin.getLogger().info("Generated default file: " + path);
         } else {
            plugin.getLogger().warning("Could not find default resource to generate: " + path);
         }
      }

   }
}



