package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.List;

public class BlacklistValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        String type = codesConfig.getString("Codes." + code + ".Playerlist.Blacklist.Type", "ENABLED");
        List<String> blacklisted = codesConfig.getStringList("Codes." + code + ".Playerlist.Blacklist.List");

        if (type.equalsIgnoreCase("DISABLED")) {
            return true;
        }

        boolean isBlacklisted = false;
        if (type.equalsIgnoreCase("ENABLED") && blacklisted.contains(player.getName())) {
            isBlacklisted = true;
        } else if (type.equalsIgnoreCase("REVERSE") && !blacklisted.contains(player.getName())) {
            isBlacklisted = true;
        }

        if (isBlacklisted) {
            MessageUtil.sendMessage(plugin, player, "general.blacklisted");
            MessageUtil.playSound(plugin, player, "sounds.failure");
            return false;
        }
        
        return true;
    }
}
