package xyz.redoxlabs.redeemcodes.migrations.v26;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.migrations.Migration;

public class Migration_26_1_Beta_3 implements Migration {

    @Override
    public String getTargetVersion() {
        return "26.1+Beta.3";
    }

    @Override
    public boolean execute(Main plugin) throws Exception {
        FileConfiguration config = plugin.getCodesConfig();
        ConfigurationSection codesSection = config.getConfigurationSection("Codes");
        
        if (codesSection == null) return true;
        
        boolean modified = false;
        
        for (String code : codesSection.getKeys(false)) {
            ConfigurationSection codeSection = codesSection.getConfigurationSection(code);
            if (codeSection == null) continue;
            

            if (codeSection.contains("Playerlist.Used")) {
                config.set("Codes." + code + ".Playerlist.Used", null);
                modified = true;
            }
            

            if (codeSection.contains("redeem-limit.Type") || codeSection.contains("redeem-limit.Count")) {
                String legacyType = codeSection.getString("redeem-limit.Type", "PLAYER");
                int legacyCount = codeSection.getInt("redeem-limit.Count", 1);
                int legacyCooldown = codeSection.getInt("redeem-limit.Cooldown", 0);
                

                int playerLimit = 1;
                int ipLimit = 1;
                int globalLimit = -1;
                
                if (legacyType.equalsIgnoreCase("CODE")) {
                    globalLimit = legacyCount;
                    playerLimit = -1;
                    ipLimit = -1;
                } else {
                    playerLimit = legacyCount;
                    ipLimit = legacyCount;
                    globalLimit = -1;
                }
                
                config.set("Codes." + code + ".redeem-limit.player", playerLimit);
                config.set("Codes." + code + ".redeem-limit.ip", ipLimit);
                config.set("Codes." + code + ".redeem-limit.global", globalLimit);
                config.set("Codes." + code + ".redeem-limit.cooldown", legacyCooldown);
                

                config.set("Codes." + code + ".redeem-limit.Type", null);
                config.set("Codes." + code + ".redeem-limit.Count", null);
                config.set("Codes." + code + ".redeem-limit.Cooldown", null);
                
                modified = true;
            }
        }
        
        if (modified) {
            plugin.saveCodesConfig();
        }
        return true;
    }
}
