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
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.test.usage");
            return true;
        }

        String code = args[1];
        if (!plugin.getCodesConfig().contains("Codes." + code)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "general.not-exist");
            return true;
        }

        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.test.header", "&#1E90FF&m        &r &#00BFFFDry Run &r&#1E90FF&m        "));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.test.start", "&#E0E0E0Simulating code: &#00BFFF%code%").replace("%code%", code));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.test.checking", "&#E0E0E0Checking rewards..."));

        RewardProcessor rewardProcessor = new RewardProcessor(plugin);
        rewardProcessor.processRewards(player, code, plugin.getCodesConfig(), true);

        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.test.complete", "&#32CD32[DRY RUN COMPLETE]"));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.list.footer", "&#1E90FF&m                                  "));

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
