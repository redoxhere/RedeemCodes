package xyz.redoxlabs.redeemcodes.migrations;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.migrations.v26.Migration_26_1_Beta_3;
import xyz.redoxlabs.redeemcodes.utils.VersionComparator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MigrationManager {

    private static final List<Migration> MIGRATION_PATH = new ArrayList<>();

    static {

        MIGRATION_PATH.add(new Migration_26_1_Beta_3());

    }

    public static void migrate(Main plugin) {
        FileConfiguration config = plugin.getConfig();
        String currentPluginVersion = plugin.getDescription().getVersion();
        String dataVersion = config.getString("data-version", "x");


        if (dataVersion.equals("x") || dataVersion.isEmpty()) {
            boolean hasLegacyData = plugin.getCodesConfig().contains("Codes") && 
                                    !plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false).isEmpty();
            
            if (hasLegacyData) {

                dataVersion = "0.0.0";
            } else {

                dataVersion = currentPluginVersion;
                config.set("data-version", dataVersion);
                plugin.saveConfig();
                plugin.getLogger().info("Fresh install detected. Set data-version to " + dataVersion);
                return;
            }
        }


        if (VersionComparator.isNewerVersion(dataVersion, currentPluginVersion)) {
            plugin.getLogger().severe("========================================");
            plugin.getLogger().severe("FATAL DATA DOWNGRADE DETECTED!");
            plugin.getLogger().severe("Your data is from version " + dataVersion);
            plugin.getLogger().severe("But the plugin is version " + currentPluginVersion);
            plugin.getLogger().severe("Downgrading plugin versions can severely corrupt data.");
            plugin.getLogger().severe("Disabling plugin to protect data integrity...");
            plugin.getLogger().severe("========================================");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }


        boolean needsMigration = false;
        for (Migration m : MIGRATION_PATH) {
            if (VersionComparator.isNewerVersion(m.getTargetVersion(), dataVersion) &&
                !VersionComparator.isNewerVersion(m.getTargetVersion(), currentPluginVersion)) {
                needsMigration = true;
                break;
            }
        }

        File backupDir = null;
        if (needsMigration) {
            backupDir = new File(plugin.getDataFolder(), "backups/migration_" + System.currentTimeMillis());
            if (!backupDir.exists()) backupDir.mkdirs();
            backupFiles(plugin, backupDir);
            plugin.getLogger().info("Created pre-migration backup at " + backupDir.getPath());
        }


        for (Migration migration : MIGRATION_PATH) {
            String targetVersion = migration.getTargetVersion();


            boolean targetNewerThanData = VersionComparator.isNewerVersion(targetVersion, dataVersion);
            boolean targetNotNewerThanPlugin = !VersionComparator.isNewerVersion(targetVersion, currentPluginVersion);

            if (targetNewerThanData && targetNotNewerThanPlugin) {
                plugin.getLogger().info("Migrating data to format version " + targetVersion + "...");
                
                try {
                    boolean success = migration.execute(plugin);
                    if (success) {
                        dataVersion = targetVersion;
                        config.set("data-version", dataVersion);
                        plugin.saveConfig();
                        plugin.getLogger().info("Successfully migrated data to " + targetVersion);
                    } else {
                        throw new Exception("Migration executed but returned false.");
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("========================================");
                    plugin.getLogger().severe("CRITICAL ERROR DURING DATA MIGRATION!");
                    plugin.getLogger().severe("Failed to migrate data to " + targetVersion);
                    plugin.getLogger().severe("Error: " + e.getMessage());
                    plugin.getLogger().severe("Reverting files from backup...");
                    if (backupDir != null) restoreFiles(plugin, backupDir);
                    plugin.getLogger().severe("Please DO NOT modify your data files.");
                    plugin.getLogger().severe("Contact the developer immediately at: https://discord.com/invite/G4dYdeaDWZ");
                    plugin.getLogger().severe("Disabling plugin...");
                    plugin.getLogger().severe("========================================");
                    e.printStackTrace();
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                    return;
                }
            }
        }
        

        if (VersionComparator.isNewerVersion(currentPluginVersion, dataVersion)) {
            dataVersion = currentPluginVersion;
            config.set("data-version", dataVersion);
            plugin.saveConfig();
            plugin.getLogger().info("Data version synced to plugin version " + dataVersion);
        }
    }

    private static void backupFiles(Main plugin, File backupDir) {
        String[] filesToBackup = {"config.yml", "codes.yml", "premades.yml", "redeemdata.yml"};
        for (String fileName : filesToBackup) {
            File file = new File(plugin.getDataFolder(), fileName);
            if (file.exists()) {
                try {
                    Files.copy(file.toPath(), new File(backupDir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to backup " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    private static void restoreFiles(Main plugin, File backupDir) {
        String[] filesToRestore = {"config.yml", "codes.yml", "premades.yml", "redeemdata.yml"};
        for (String fileName : filesToRestore) {
            File backupFile = new File(backupDir, fileName);
            File originalFile = new File(plugin.getDataFolder(), fileName);
            if (backupFile.exists()) {
                try {
                    Files.copy(backupFile.toPath(), originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to restore " + fileName + " from backup! Data may be corrupted.");
                }
            }
        }
    }
}
