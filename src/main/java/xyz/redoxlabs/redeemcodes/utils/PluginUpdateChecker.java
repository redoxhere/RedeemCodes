package xyz.redoxlabs.redeemcodes.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import xyz.redoxlabs.redeemcodes.Main;
import org.bukkit.ChatColor;

public class PluginUpdateChecker {
   private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/redeemcodes/version";
   private static final String MODRINTH_PROJECT_URL = "https://modrinth.com/plugin/redeemcodes";
   private final Main plugin;
   private final String currentVersion;

   public PluginUpdateChecker(Main plugin) {
      this.plugin = plugin;
      this.currentVersion = plugin.getDescription().getVersion();
   }

   public void checkForUpdates() {
      plugin.getFoliaLib().getImpl().runAsync((task) -> {
         try {
            String latestVersion = getLatestVersionFromModrinth();
            if (latestVersion != null) {
               compareVersions(latestVersion);
            } else {
               plugin.getLogger().warning("Could not check for plugin updates. Modrinth API unavailable.");
            }
         } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for plugin updates: " + e.getMessage());
         }
      });
   }

   private String getLatestVersionFromModrinth() {
      try {
         URL url = new URL(MODRINTH_API_URL);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("User-Agent", "RedeemCodes-Plugin/" + currentVersion);
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         int responseCode = connection.getResponseCode();
         
         if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
               StringBuilder response = new StringBuilder();
               String line;
               while ((line = reader.readLine()) != null) {
                  response.append(line);
               }
               
               String jsonResponse = response.toString();
               Pattern pattern = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
               Matcher matcher = pattern.matcher(jsonResponse);
               if (matcher.find()) {
                  return matcher.group(1);
               }
            }
         } else {
            plugin.getLogger().warning("Modrinth API returned HTTP " + responseCode);
         }
         connection.disconnect();
      } catch (IOException e) {
         plugin.getLogger().warning("Failed to connect to Modrinth API: " + e.getMessage());
      }
      return null;
   }

   private void compareVersions(String latestVersion) {
      if (isNewerVersion(latestVersion, currentVersion)) {
         plugin.getLogger().warning("========================================");
         plugin.getLogger().warning("PLUGIN UPDATE AVAILABLE!");
         plugin.getLogger().warning("Current version: " + currentVersion);
         plugin.getLogger().warning("Latest version: " + latestVersion);
         plugin.getLogger().warning("Please download the latest version:");
         plugin.getLogger().warning(MODRINTH_PROJECT_URL);
         plugin.getLogger().warning("========================================");
         notifyOperators(latestVersion);
      } else {
         plugin.getLogger().info("Plugin is up to date (v" + currentVersion + ") ✓");
      }
   }

    private boolean isNewerVersion(String versionA, String versionB) {
       return VersionComparator.isNewerVersion(versionA, versionB);
    }

   private void notifyOperators(final String latestVersion) {
      plugin.getFoliaLib().getImpl().runNextTick((task) -> {
         Bukkit.getOnlinePlayers().stream().filter((player) -> player.isOp() || player.hasPermission("redeemcodes.admin")).forEach((player) -> {
            String prefix = plugin.getConfig().getString("prefix", "&7[RedeemCodes] ");
            player.sendMessage(MessageUtil.color(prefix + "&eA new plugin update is available! (v" + latestVersion + ")"));
            player.sendMessage(MessageUtil.color(prefix + "&eDownload: " + MODRINTH_PROJECT_URL));
         });
      });
   }

   public String getCurrentVersion() {
      return currentVersion;
   }
}
