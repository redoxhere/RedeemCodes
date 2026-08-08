package xyz.redoxlabs.redeemcodes;

import org.bstats.bukkit.Metrics;
import xyz.redoxlabs.redeemcodes.commands.RedeemCodesCommand;
import xyz.redoxlabs.redeemcodes.commands.RedeemCommand;
import xyz.redoxlabs.redeemcodes.guis.CodeEditorGUI;
import xyz.redoxlabs.redeemcodes.guis.CodesListGUI;
import xyz.redoxlabs.redeemcodes.guis.EventGUI;
import xyz.redoxlabs.redeemcodes.guis.ExpiredCodesListGUI;
import xyz.redoxlabs.redeemcodes.guis.RewardGUI;
import xyz.redoxlabs.redeemcodes.guis.RedeemLimitGUI;
import xyz.redoxlabs.redeemcodes.guis.SelectCodeListGUI;
import xyz.redoxlabs.redeemcodes.managers.CreateCodeHandler;
import xyz.redoxlabs.redeemcodes.managers.EventManager;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.managers.PremadeManager;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import xyz.redoxlabs.redeemcodes.managers.SackManager;
import xyz.redoxlabs.redeemcodes.utils.CodeExpirationManager;
import xyz.redoxlabs.redeemcodes.utils.DefaultExampleGenerator;
import xyz.redoxlabs.redeemcodes.listeners.GUIListener;
import xyz.redoxlabs.redeemcodes.utils.PluginUpdateChecker;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.tcoded.folialib.FoliaLib;

public final class Main extends JavaPlugin {
   private FileConfiguration codesConfig;
   private File codesFile;
   private RedeemDataManager redeemDataManager;
   private CodeExpirationManager expirationManager;
   private CreateCodeHandler createHandler;
   private SackManager sackManager;
   private PremadeManager premadeManager;
   private EventManager eventManager;
   private EventGUI eventGUI;
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
   public final Map<Player, CodesListGUI> openCodeGUIs = new HashMap<>();
   public final Map<Player, CodeEditorGUI> openEditorGUIs = new HashMap<>();
   public final Map<Player, ExpiredCodesListGUI> openExpiredCodeGUIs = new HashMap<>();
   public final Map<Player, SelectCodeListGUI> openSelectCodeGUIs = new HashMap<>();
   public final Map<Player, RewardGUI> openRewardGUIs = new HashMap<>();
   public final Map<Player, xyz.redoxlabs.redeemcodes.guis.RedeemLimitGUI> openLimitGUIs = new HashMap<>();
   private FoliaLib foliaLib;

   public void onEnable() {
      this.foliaLib = new FoliaLib(this);
      this.saveDefaultConfig();
      this.reloadConfig();
      createCodesConfig();
      xyz.redoxlabs.redeemcodes.migrations.MigrationManager.migrate(this);
      (new DefaultExampleGenerator(this)).generate();

      HeadManager.preloadHeads();
      this.redeemDataManager = new RedeemDataManager(this);
      this.expirationManager = new CodeExpirationManager(this);
      this.sackManager = new SackManager(this);
      this.premadeManager = new PremadeManager(this);
      this.eventManager = new EventManager(this);
      this.eventGUI = new EventGUI(this);
      PluginUpdateChecker updateChecker = new PluginUpdateChecker(this);
      updateChecker.checkForUpdates();
      PluginCommand redeemCommand = getCommand("redeem");
      if (redeemCommand != null) {
         redeemCommand.setExecutor(new RedeemCommand(this));
      }

      PluginCommand redeemcodesCommand = getCommand("redeemcodes");
      if (redeemcodesCommand != null) {
         RedeemCodesCommand rcc = new RedeemCodesCommand(this);
         redeemcodesCommand.setExecutor(rcc);
         redeemcodesCommand.setTabCompleter(rcc);
      }

      CreateCodeHandler createHandler = new CreateCodeHandler(this);
      getServer().getPluginManager().registerEvents(createHandler, this);
      this.createHandler = createHandler;
      getServer().getPluginManager().registerEvents(new GUIListener(this, createHandler), this);
      getServer().getPluginManager().registerEvents(eventGUI, this);

      int pluginId = 27831;

      try {
         Class.forName("org.bstats.bukkit.Metrics");
         new Metrics(this, pluginId);
         getLogger().info("bStats metrics initialized successfully!");
      } catch (Exception e) {
      }

      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[RedeemCodes] Plugin Enabled!");
   }

   public void onDisable() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (openCodeGUIs.containsKey(player) || openEditorGUIs.containsKey(player) ||
             openExpiredCodeGUIs.containsKey(player) || openSelectCodeGUIs.containsKey(player) ||
             openRewardGUIs.containsKey(player)) {
            player.closeInventory();
         }
      }
      openCodeGUIs.clear();
      openEditorGUIs.clear();
      openExpiredCodeGUIs.clear();
      openSelectCodeGUIs.clear();
      openRewardGUIs.clear();
      if (openLimitGUIs != null) openLimitGUIs.clear();

      if (redeemDataManager != null) {
         redeemDataManager.saveFile();
      }

      if (expirationManager != null) {
         expirationManager.stopTimer();
      }

      this.foliaLib.getImpl().cancelAllTasks();

      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[RedeemCodes] Plugin Disabled!");
   }

   public void sendToWebhook(String webhookUrl, String jsonPayload) {
      this.foliaLib.getImpl().runAsync((task) -> {
         try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Minecraft Plugin");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
               byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
               os.write(input, 0, input.length);
            }

            connection.getResponseCode();
            connection.disconnect();
         } catch (IOException e) {
            getLogger().severe("Could not send webhook message: " + e.getMessage());
         }

      });
   }

   private void createCodesConfig() {
      this.codesFile = new File(getDataFolder(), "codes.yml");
      if (!codesFile.exists()) {
         saveResource("codes.yml", false);
      }

      this.codesConfig = xyz.redoxlabs.redeemcodes.utils.FileTracker.validateAndLoad(codesFile, this);
      if (this.codesConfig == null) {
          this.codesConfig = new org.bukkit.configuration.file.YamlConfiguration();
      } else {
          xyz.redoxlabs.redeemcodes.utils.FileTracker.updateConfig(codesFile, "codes.yml", this, this.codesConfig);
      }
   }

   public void reloadCodesConfig() {
      this.codesFile = new File(getDataFolder(), "codes.yml");
      this.codesConfig = xyz.redoxlabs.redeemcodes.utils.FileTracker.validateAndLoad(codesFile, this);
      if (this.codesConfig == null) {
          this.codesConfig = new org.bukkit.configuration.file.YamlConfiguration();
      }
   }

   @Override
   public void reloadConfig() {
      super.reloadConfig();
      File configFile = new File(getDataFolder(), "config.yml");
      org.bukkit.configuration.file.YamlConfiguration validatedConfig = xyz.redoxlabs.redeemcodes.utils.FileTracker.validateAndLoad(configFile, this);
      if (validatedConfig != null) {
          xyz.redoxlabs.redeemcodes.utils.FileTracker.updateConfig(configFile, "config.yml", this, validatedConfig);
      }
   }

   public FileConfiguration getCodesConfig() {
      return codesConfig;
   }

   public void saveCodesConfig() {
      final String dump = codesConfig.saveToString();
      this.foliaLib.getImpl().runAsync((task) -> {
         try {
            java.nio.file.Files.write(codesFile.toPath(), dump.getBytes(java.nio.charset.StandardCharsets.UTF_8));
         } catch (IOException e) {
            e.printStackTrace();
         }
      });
   }

   public RedeemDataManager getRedeemDataManager() {
      return redeemDataManager;
   }

   public CodeExpirationManager getExpirationManager() {
      return expirationManager;
   }

   public SackManager getSackManager() {
      return sackManager;
   }

   public PremadeManager getPremadeManager() {
      return premadeManager;
   }

   public EventManager getEventManager() {
      return eventManager;
   }

   public EventGUI getEventGUI() {
      return eventGUI;
   }

   public String color(String msg) {
      if (msg == null) {
         return null;
      } else {
         Matcher matcher = HEX_PATTERN.matcher(msg);
         StringBuffer buffer = new StringBuffer();

         while(matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder spigotHex = new StringBuilder("&x");

            for(char c : hex.toCharArray()) {
               spigotHex.append('&').append(c);
            }

            matcher.appendReplacement(buffer, spigotHex.toString());
         }

         matcher.appendTail(buffer);
         return ChatColor.translateAlternateColorCodes('&', buffer.toString());
      }
   }

   public String getPrefix() {
      return getConfig().getString("prefix", "&7[RedeemCodes] ");
   }

   public CreateCodeHandler getDuplicationHandler() {
      return createHandler;
   }

   public FoliaLib getFoliaLib() {
      return foliaLib;
   }
}