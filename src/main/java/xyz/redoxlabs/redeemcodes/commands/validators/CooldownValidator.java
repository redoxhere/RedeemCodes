package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

public class CooldownValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        int cooldownMinutes = codesConfig.getInt("Codes." + code + ".redeem-limit.cooldown", 0);
        
        if (cooldownMinutes > 0) {
            RedeemDataManager dataManager = plugin.getRedeemDataManager();
            long lastRedeemTime = dataManager.getLastRedeemTime(code, player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long cooldownMillis = (long) cooldownMinutes * 60L * 1000L;

            if (currentTime - lastRedeemTime < cooldownMillis) {
                long remaining = (cooldownMillis - (currentTime - lastRedeemTime)) / 1000L;
                long minutes = remaining / 60L;
                long seconds = remaining % 60L;
                String formatted = minutes + "m " + seconds + "s";
                String msg = codesConfig.getString("Codes." + code + ".redeem-limit.cooldown-message", "&cWait %Cooldown%");
                
                MessageUtil.sendInteractiveMessage(player, MessageUtil.color(plugin.getPrefix()) + msg.replace("%Cooldown%", formatted));
                MessageUtil.playSound(plugin, player, "sounds.failure");
                return false;
            }
        }
        
        return true;
    }
}
