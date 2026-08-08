package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.redoxlabs.redeemcodes.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.ArrayList;

public class FileTracker {

    private static final Pattern LINE_PATTERN = Pattern.compile("line (\\d+), column");

    public static YamlConfiguration validateAndLoad(File file, Main plugin) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
            return config;
        } catch (InvalidConfigurationException e) {
            handleConfigError(file, e, plugin);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not read file " + file.getName() + " due to IOException!");
            e.printStackTrace();
        }
        return null;
    }

    private static void handleConfigError(File file, InvalidConfigurationException exception, Main plugin) {
        String msg = exception.getMessage();
        int errorLine = -1;
        
        Matcher matcher = LINE_PATTERN.matcher(msg);
        if (matcher.find()) {
            try {
                errorLine = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "====================================================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "[RedeemCodes] CONFIGURATION SYNTAX ERROR!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "File: " + ChatColor.YELLOW + file.getName());
        
        if (errorLine != -1) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Line: " + ChatColor.YELLOW + errorLine);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error details: " + ChatColor.GRAY + msg.split("\n")[0]);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--- Context ---");
            
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                int start = Math.max(0, errorLine - 3);
                int end = Math.min(lines.size(), errorLine + 2);
                
                for (int i = start; i < end; i++) {
                    int currentLineNum = i + 1;
                    String prefix = (currentLineNum == errorLine) ? ChatColor.RED + ">> " : ChatColor.GRAY + "   ";
                    String lineContent = lines.get(i).replace("\t", "    ");
                    Bukkit.getConsoleSender().sendMessage(prefix + currentLineNum + " | " + ChatColor.WHITE + lineContent);
                }
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not read file contents for context.");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error details:");
            for (String line : msg.split("\n")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + line);
            }
        }
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "====================================================");
        Bukkit.getConsoleSender().sendMessage("");
    }

    public static boolean updateConfig(File file, String resourceName, Main plugin, FileConfiguration currentConfig) {
        if (currentConfig == null) return false;
        
        InputStream defStream = plugin.getResource(resourceName);
        if (defStream == null) return false;

        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
        
        boolean modified = false;
        Set<String> defaultKeys = defConfig.getKeys(true);
        List<String> missingKeys = new ArrayList<>();
        
        for (String key : defaultKeys) {
            if (!currentConfig.contains(key)) {
                currentConfig.set(key, defConfig.get(key));
                missingKeys.add(key);
                modified = true;
            }
        }

        if (modified) {
            try {
                currentConfig.save(file);
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[RedeemCodes] Auto-updated " + file.getName() + " with missing elements:");
                for (String key : missingKeys) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + " - Added missing key: " + ChatColor.WHITE + key);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save auto-updated config: " + file.getName());
                e.printStackTrace();
            }
        }
        return modified;
    }
}
