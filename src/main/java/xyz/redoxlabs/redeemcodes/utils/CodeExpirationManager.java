package xyz.redoxlabs.redeemcodes.utils;

import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import java.util.concurrent.TimeUnit;

public class CodeExpirationManager {
   private final Main plugin;
   private final RedeemDataManager dataManager;
   private final Map<String, Long> activeTimers = new ConcurrentHashMap<>();
   private final Set<String> expiredCodes = new HashSet<>();
   private WrappedTask timerTask;

   public CodeExpirationManager(Main plugin) {
      this.plugin = plugin;
      this.dataManager = plugin.getRedeemDataManager();
      loadExpirations();
      startTimer();
   }

   private void loadExpirations() {
      FileConfiguration data = dataManager.getData();
      if (data.isConfigurationSection("codes")) {
         long now = System.currentTimeMillis();

         for(String codeName : data.getConfigurationSection("codes").getKeys(false)) {
            long timestamp = data.getLong("codes." + codeName + ".expiration-timestamp", -1L);
            if (timestamp != -1L) {
               if (timestamp <= now) {
                  expiredCodes.add(codeName);
               } else {
                  activeTimers.put(codeName, timestamp);
               }
            }
         }

      }
   }

   private void startTimer() {
      this.timerTask = plugin.getFoliaLib().getImpl().runTimerAsync(() -> {
         if (!activeTimers.isEmpty()) {
            long now = System.currentTimeMillis();
            activeTimers.entrySet().removeIf(entry -> {
               if (now >= entry.getValue()) {
                  String codeName = entry.getKey();
                  expiredCodes.add(codeName);
                  plugin.getLogger().info("Code '" + codeName + "' has expired.");
                  return true;
               }
               return false;
            });
         }
      }, 20L * 50L, 20L * 50L, TimeUnit.MILLISECONDS);
   }

   public void stopTimer() {
      if (timerTask != null && !timerTask.isCancelled()) {
         timerTask.cancel();
      }

   }

   public boolean isExpired(String codeName) {
      return expiredCodes.contains(codeName);
   }

   public long getRemainingTime(String codeName) {
      Long timestamp = (Long)activeTimers.get(codeName);
      return timestamp == null ? -1L : Math.max(0L, timestamp - System.currentTimeMillis());
   }

   public void setExpiration(String codeName, long durationSeconds) {
      FileConfiguration codesConfig = plugin.getCodesConfig();
      if (durationSeconds == -1L) {
         activeTimers.remove(codeName);
         expiredCodes.remove(codeName);
         dataManager.setExpirationTimestamp(codeName, -1L);
         codesConfig.set("Codes." + codeName + ".expire-time", -1);
      } else {
         long timestamp = System.currentTimeMillis() + durationSeconds * 1000L;
         activeTimers.put(codeName, timestamp);
         expiredCodes.remove(codeName);
         dataManager.setExpirationTimestamp(codeName, timestamp);
         codesConfig.set("Codes." + codeName + ".expire-time", durationSeconds);
      }

      plugin.saveCodesConfig();
   }

   public void reactivate(String codeName) {
      setExpiration(codeName, -1L);
   }

   public Set<String> getExpiredCodes() {
      return new HashSet(expiredCodes);
   }
}




