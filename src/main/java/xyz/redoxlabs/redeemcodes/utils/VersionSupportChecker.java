package xyz.redoxlabs.redeemcodes.utils;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VersionSupportChecker {
   private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("1.21.x", "1.20.x", "1.19.x", "1.18.x", "1.17.x", "1.16.5+");
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
         String baseVersion = pattern.substring(0, pattern.length() - 2);
         return version.startsWith(baseVersion);
      } else if (pattern.endsWith("+")) {
         String basePattern = pattern.substring(0, pattern.length() - 1);
         String[] baseParts = basePattern.split("\\.");
         if (baseParts.length < 2) {
            return false;
         } else {
            String[] versionParts = version.split("\\.");
            if (versionParts.length < 2) {
               return false;
            } else if (versionParts[0].equals(baseParts[0]) && versionParts[1].equals(baseParts[1])) {
               if (baseParts.length >= 3) {
                  try {
                     int minSubVersion = Integer.parseInt(baseParts[2]);
                     if (versionParts.length >= 3) {
                        int actualSubVersion = Integer.parseInt(versionParts[2]);
                        return actualSubVersion >= minSubVersion;
                     } else {
                        return false;
                     }
                  } catch (NumberFormatException e) {
                     return false;
                  }
               } else {
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return version.equals(pattern);
      }
   }
}



