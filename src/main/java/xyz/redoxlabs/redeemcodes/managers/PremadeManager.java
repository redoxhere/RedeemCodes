package xyz.redoxlabs.redeemcodes.managers;

import xyz.redoxlabs.redeemcodes.Main;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PremadeManager {
   private final Main plugin;
   private FileConfiguration premadesConfig;
   private File premadesFile;

   public PremadeManager(Main plugin) {
      this.plugin = plugin;
      reloadPremades();
   }

   public void reloadPremades() {
      this.premadesFile = new File(plugin.getDataFolder(), "premades.yml");
      if (!premadesFile.exists()) {
         plugin.saveResource("premades.yml", false);
      }

      this.premadesConfig = YamlConfiguration.loadConfiguration(premadesFile);
   }

   public List<String> getPremadeCommands(String name) {
      return premadesConfig.contains("premades." + name) ? premadesConfig.getStringList("premades." + name) : Collections.emptyList();
   }

   public boolean premadeExists(String name) {
      return premadesConfig.contains("premades." + name);
   }

   public List<String> getPremadeNames() {
      if (premadesConfig.isConfigurationSection("premades")) {
         Set<String> keys = premadesConfig.getConfigurationSection("premades").getKeys(false);
         return new ArrayList(keys);
      } else {
         return new ArrayList<>();
      }
   }

   public void addCommand(String premadeName, String command) {
      List<String> cmds = getPremadeCommands(premadeName);
      if (!(cmds instanceof ArrayList)) {
         cmds = new ArrayList(cmds);
      }

      cmds.add(command);
      premadesConfig.set("premades." + premadeName, cmds);
      save();
   }

   public boolean removeCommand(String premadeName, int index) {
      List<String> cmds = getPremadeCommands(premadeName);
      if (index >= 0 && index < cmds.size()) {
         if (!(cmds instanceof ArrayList)) {
            cmds = new ArrayList(cmds);
         }

         cmds.remove(index);
         premadesConfig.set("premades." + premadeName, cmds);
         save();
         return true;
      } else {
         return false;
      }
   }

   private void save() {
      try {
         premadesConfig.save(premadesFile);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }
}



