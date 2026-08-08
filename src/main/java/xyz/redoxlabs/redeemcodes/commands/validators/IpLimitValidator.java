package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RedeemDataManager;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class IpLimitValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        int ipLimit = codesConfig.getInt("Codes." + code + ".redeem-limit.ip", 1);
        
        if (ipLimit != -1) {
            RedeemDataManager dataManager = plugin.getRedeemDataManager();
            String currentIp = player.getAddress().getAddress().getHostAddress();
            List<String> usedIps = dataManager.getPlayerIps(code, player.getUniqueId());

            List<String> ipsToCheck = new ArrayList<>(usedIps);
            if (!ipsToCheck.contains(currentIp)) {
                ipsToCheck.add(currentIp);
            }
            
            for (String ip : ipsToCheck) {
                if (dataManager.getIpUses(code, ip) >= ipLimit) {
                    MessageUtil.sendMessage(plugin, player, "already-used");
                    MessageUtil.playSound(plugin, player, "sounds.failure");
                    return false;
                }
            }
        }
        
        return true;
    }
}
