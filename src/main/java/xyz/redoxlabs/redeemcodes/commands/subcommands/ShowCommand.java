package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

import java.util.List;

public class ShowCommand implements Subcommand {
    private final Main plugin;

    public ShowCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rc show <code>");
        } else {
            String code = args[1];
            FileConfiguration codes = plugin.getCodesConfig();
            if (!codes.contains("Codes." + code)) {
                player.sendMessage("§cThis code doesn't exist.");
            } else {
                player.sendMessage(plugin.color("&d---- &bCode Details: &f" + code + " &d----"));
                player.sendMessage(plugin.color("&7Enabled: " + (codes.getBoolean("Codes." + code + ".enabled", true) ? "&aTrue" : "&cFalse")));
                sendRewardsView(player, code, codes, "Codes." + code + ".rewards");
            }
        }
        return true;
    }

    private void sendRewardsView(Player player, String codeName, FileConfiguration codes, String rewardPath) {
        player.sendMessage(plugin.color("&d--- Rewards for &b" + codeName + " &d---"));
        String type = codes.getString(rewardPath + ".type", "RANDOM");
        player.sendMessage(plugin.color("&7Type: &e" + type));
        ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
        if (cmdSection != null) {
            player.sendMessage(plugin.color("&7Command Packs:"));

            for (String pack : cmdSection.getKeys(false)) {
                player.sendMessage(plugin.color("  &e" + pack + " &7(" + cmdSection.getStringList(pack).size() + ")"));
            }
        }

        List<String> sacks = codes.getStringList(rewardPath + ".sacks");
        if (!sacks.isEmpty()) {
            player.sendMessage(plugin.color("&7Sacks:"));

            for (String s : sacks) {
                player.sendMessage(plugin.color("  &b- " + s));
            }
        }

        List<String> premades = codes.getStringList(rewardPath + ".premades");
        if (!premades.isEmpty()) {
            player.sendMessage(plugin.color("&7Premades:"));

            for (String p : premades) {
                player.sendMessage(plugin.color("  &b- " + p));
            }
        }

        List<String> events = codes.getStringList(rewardPath + ".events");
        if (!events.isEmpty()) {
            player.sendMessage(plugin.color("&7Events:"));

            for (String e : events) {
                player.sendMessage(plugin.color("  &b- " + e));
            }
        }

        if (codes.isList(rewardPath)) {
            List<String> oldRewards = codes.getStringList(rewardPath);
            if (!oldRewards.isEmpty()) {
                player.sendMessage(plugin.color("&7Legacy Rewards: &7(" + oldRewards.size() + ")"));
            }
        }
    }
}
