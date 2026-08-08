package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

public class PlayerLimitValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        int playerLimit = codesConfig.getInt("Codes." + code + ".redeem-limit.player", 1);
        
        if (playerLimit != -1) {
            RedeemDataManager dataManager = plugin.getRedeemDataManager();
            int playerUses = dataManager.getPlayerUses(code, player.getUniqueId());
            
            if (playerUses >= playerLimit) {
                MessageUtil.sendMessage(plugin, player, "already-used");
                MessageUtil.playSound(plugin, player, "sounds.failure");
                return false;
            }
        }
        
        return true;
    }
}
