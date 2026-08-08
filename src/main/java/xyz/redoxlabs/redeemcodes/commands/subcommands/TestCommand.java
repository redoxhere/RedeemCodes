package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.RewardProcessor;

import java.util.ArrayList;
import java.util.List;

public class TestCommand implements Subcommand {
    private final Main plugin;

    public TestCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.color("&cThis command is for players only."));
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(plugin.color("&cUsage: /rc test <code>"));
            return true;
        }

        String code = args[1];
        if (!plugin.getCodesConfig().contains("Codes." + code)) {
            player.sendMessage(plugin.color("&cThat code does not exist."));
            return true;
        }

        player.sendMessage(plugin.color("&e&m--------------------------------------------------"));
        player.sendMessage(plugin.color("&d&l[DRY RUN] &r&7Simulating code: &f" + code));
        player.sendMessage(plugin.color("&7Checking rewards..."));

        RewardProcessor rewardProcessor = new RewardProcessor(plugin);
        rewardProcessor.processRewards(player, code, plugin.getCodesConfig(), true);

        player.sendMessage(plugin.color("&a&l[DRY RUN COMPLETE]"));
        player.sendMessage(plugin.color("&e&m--------------------------------------------------"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (plugin.getCodesConfig().getConfigurationSection("Codes") != null) {
                return new ArrayList<>(plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false));
            }
        }
        return new ArrayList<>();
    }
}
