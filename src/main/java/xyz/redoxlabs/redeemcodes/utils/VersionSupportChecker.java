package xyz.redoxlabs.redeemcodes.utils;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VersionSupportChecker {
   private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("26.x", "1.21.x", "1.20.x", "1.19.x", "1.18.x", "1.17.x", "1.16.5+");
   private final JavaPlugin plugin;

   public VersionSupportChecker(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean isVersionSupported() {
      String version = getServerVersion();

      for(String pattern : SUPPORTED_VERSIONS) {
         if (matchesVersionPattern(version, pattern)) {
            return true;
         }
      }

      return false;
   }

   public String getServerVersion() {
      String bukkitVersion = Bukkit.getBukkitVersion();
      int dashIndex = bukkitVersion.indexOf(45);
      return dashIndex > 0 ? bukkitVersion.substring(0, dashIndex) : bukkitVersion;
   }

   public void checkVersion() {
      String currentVersion = getServerVersion();
      boolean supported = isVersionSupported();
      if (!supported) {
         plugin.getLogger().warning("========================================");
         plugin.getLogger().warning("UNSUPPORTED MINECRAFT VERSION DETECTED!");
         plugin.getLogger().warning("Current version: " + currentVersion);
         plugin.getLogger().warning("Supported versions: " + String.join(", ", SUPPORTED_VERSIONS));
         plugin.getLogger().warning("This version is not officially supported.");
         plugin.getLogger().warning("Some features may not work properly.");
         plugin.getLogger().warning("Please look for a supported version at:");
         plugin.getLogger().warning("https://modrinth.com/plugin/redeemcodes");
         plugin.getLogger().warning("or contact developer");
         plugin.getLogger().warning("========================================");
      } else {
         plugin.getLogger().info("Server version " + currentVersion + " is supported ✓");
      }

   }

   public List<String> getSupportedVersions() {
      return SUPPORTED_VERSIONS;
   }

   private boolean matchesVersionPattern(String version, String pattern) {
      if (pattern.endsWith(".x")) {
         String baseVersion = pattern.substring(0, pattern.length() - 1);
         return version.startsWith(baseVersion) || version.equals(baseVersion.substring(0, baseVersion.length() - 1));
      } else if (pattern.endsWith("+")) {
         String basePattern = pattern.substring(0, pattern.length() - 1);
         return compareVersions(version, basePattern) >= 0;
      } else {
         return compareVersions(version, pattern) == 0;
      }
   }

   private int compareVersions(String v1, String v2) {
      String[] parts1 = v1.split("\\.");
      String[] parts2 = v2.split("\\.");
      int maxLen = Math.max(parts1.length, parts2.length);
      for (int i = 0; i < maxLen; i++) {
         int p1 = (i < parts1.length) ? parseInt(parts1[i]) : 0;
         int p2 = (i < parts2.length) ? parseInt(parts2[i]) : 0;
         if (p1 < p2) return -1;
         if (p1 > p2) return 1;
      }
      return 0;
   }

   private int parseInt(String str) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException e) {
         return 0;
      }
   }
}



