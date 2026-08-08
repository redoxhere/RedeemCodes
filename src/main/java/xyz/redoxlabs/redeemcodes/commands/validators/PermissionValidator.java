package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.List;

public class PermissionValidator implements RedeemValidator {

    @Override
    public boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig) {
        if (codesConfig.getBoolean("Codes." + code + ".permisson.required", false)) {
            List<String> perms = codesConfig.getStringList("Codes." + code + ".permisson.list");
            boolean hasPermission = perms.stream().anyMatch(player::hasPermission);
            if (!hasPermission) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                MessageUtil.playSound(plugin, player, "sounds.failure");
                return false;
            }
        }
        return true;
    }
}
