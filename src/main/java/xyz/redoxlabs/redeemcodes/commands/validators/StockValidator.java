package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

public class StockValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        int globalLimit = codesConfig.getInt("Codes." + code + ".redeem-limit.global", -1);
        
        if (globalLimit != -1) {
            RedeemDataManager dataManager = plugin.getRedeemDataManager();
            int globalUses = dataManager.getData().getInt("codes." + code + ".global-uses", 0);
            
            if (globalUses >= globalLimit) {
                MessageUtil.sendMessage(plugin, player, "out-of-stock");
                MessageUtil.playSound(plugin, player, "sounds.failure");
                return false;
            }
        }
        
        return true;
    }
}
