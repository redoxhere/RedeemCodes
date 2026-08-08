package xyz.redoxlabs.redeemcodes.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RedeemDataManager {
   private final JavaPlugin plugin;
   private File file;
   private FileConfiguration data;

   public RedeemDataManager(JavaPlugin plugin) {
      this.plugin = plugin;
      createFile();
   }

   private void createFile() {
      this.file = new File(plugin.getDataFolder(), "redeemdata.yml");
      if (!file.exists()) {
         try {
            file.getParentFile().mkdirs();
            file.createNewFile();
         } catch (IOException e) {
            plugin.getLogger().severe("Could not create redeemdata.yml!");
            e.printStackTrace();
         }
      }

      this.data = YamlConfiguration.loadConfiguration(file);
   }

   public FileConfiguration getData() {
      return data;
   }

   private volatile boolean isSaving = false;
   private volatile boolean saveQueued = false;

   public void saveFile() {
      if (isSaving) {
         saveQueued = true;
         return;
      }
      isSaving = true;
      final String dump = data.saveToString();
      if (!plugin.isEnabled()) {
         try {
            java.nio.file.Files.write(file.toPath(), dump.getBytes(java.nio.charset.StandardCharsets.UTF_8));
         } catch (IOException e) {
            plugin.getLogger().severe("Could not save redeemdata.yml!");
            e.printStackTrace();
         }
         isSaving = false;
         return;
      }
      
      ((xyz.redoxlabs.redeemcodes.Main) plugin).getFoliaLib().getImpl().runAsync((task) -> {
         try {
            java.nio.file.Files.write(file.toPath(), dump.getBytes(java.nio.charset.StandardCharsets.UTF_8));
         } catch (IOException e) {
            plugin.getLogger().severe("Could not save redeemdata.yml!");
            e.printStackTrace();
         } finally {
            isSaving = false;
            if (saveQueued) {
               saveQueued = false;
               ((xyz.redoxlabs.redeemcodes.Main) plugin).getFoliaLib().getImpl().runNextTick((t) -> saveFile());
            }
         }
      });
   }

   public int getPlayerUses(String codeName, UUID uuid) {
      return data.getInt("codes." + codeName + ".players." + uuid + ".used-times", 0);
   }

   public void addPlayerUse(String codeName, UUID uuid) {
      int current = getPlayerUses(codeName, uuid);
      data.set("codes." + codeName + ".players." + uuid + ".used-times", current + 1);
      saveFile();
   }

   public List<String> getRedeemedPlayers(String codeName) {
      String path = "codes." + codeName + ".players";
      if (data.isConfigurationSection(path)) {
         Set<String> playerUUIDs = data.getConfigurationSection(path).getKeys(false);
         return new ArrayList(playerUUIDs);
      } else {
         return Collections.emptyList();
      }
   }

   public void resetPlayerUses(String codeName, UUID uuid) {
      data.set("codes." + codeName + ".players." + uuid + ".used-times", 0);
      saveFile();
   }

   public long getLastRedeemTime(String codeName, UUID uuid) {
      return data.getLong("codes." + codeName + ".players." + uuid + ".last-redeem", 0L);
   }

   public void setLastRedeemTime(String codeName, UUID uuid, long time) {
      data.set("codes." + codeName + ".players." + uuid + ".last-redeem", time);
      saveFile();
   }

   public int getGlobalUses(String codeName) {
      return data.getInt("codes." + codeName + ".global-uses", 0);
   }

   public void addGlobalUse(String codeName) {
      int current = getGlobalUses(codeName);
      data.set("codes." + codeName + ".global-uses", current + 1);
      saveFile();
   }

   public void resetGlobalUses(String codeName) {
      data.set("codes." + codeName + ".global-uses", 0);
      saveFile();
   }

   public long getExpirationTimestamp(String codeName) {
      return data.getLong("codes." + codeName + ".expiration-timestamp", -1L);
   }

   public void setExpirationTimestamp(String codeName, long timestamp) {
      data.set("codes." + codeName + ".expiration-timestamp", timestamp);
      saveFile();
   }

   public boolean hasCooldown(String codeName, UUID uuid, long cooldownMillis) {
      long last = getLastRedeemTime(codeName, uuid);
      return System.currentTimeMillis() - last < cooldownMillis;
   }

   public int getIpUses(String codeName, String ip) {

      String safeIp = ip.replace(".", "_");
      return data.getInt("codes." + codeName + ".ip_uses." + safeIp, 0);
   }

   public void addIpUse(String codeName, String ip) {
      String safeIp = ip.replace(".", "_");
      int current = getIpUses(codeName, ip);
      data.set("codes." + codeName + ".ip_uses." + safeIp, current + 1);
      saveFile();
   }

   public List<String> getPlayerIps(String codeName, UUID uuid) {
      String path = "codes." + codeName + ".players." + uuid + ".ips";
      if (data.contains(path)) {
         return data.getStringList(path);
      }
      return new ArrayList<>();
   }

   public void addPlayerIp(String codeName, UUID uuid, String ip) {
      List<String> ips = getPlayerIps(codeName, uuid);
      if (!ips.contains(ip)) {
         ips.add(ip);
         data.set("codes." + codeName + ".players." + uuid + ".ips", ips);
         saveFile();
      }
   }
}



