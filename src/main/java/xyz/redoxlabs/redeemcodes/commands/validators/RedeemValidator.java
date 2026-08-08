package xyz.redoxlabs.redeemcodes.commands.validators;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

public interface RedeemValidator {
    /**
     * Checks if the player passes this specific validation step.
     * @param plugin The main plugin instance
     * @param player The player redeeming the code
     * @param code The code being redeemed
     * @param codesConfig The active codes.yml configuration
     * @return true if validation passes, false if it fails (the validator is responsible for sending the error message)
     */
    boolean validate(Main plugin, Player player, String code, FileConfiguration codesConfig);
}
