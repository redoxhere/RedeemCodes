package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.TimeFormatter;

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
        FileConfiguration msgConfig = plugin.getMessagesConfig();

        if (args.length < 2) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "general.usage");
            return true;
        }

        String code = args[1];
        FileConfiguration codes = plugin.getCodesConfig();
        
        if (!codes.contains("Codes." + code)) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "general.not-exist");
            return true;
        }

        // Header
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.header", "&#1E90FF&m                                                                                &r &#00BFFFCode Details: &f%code% &r&#1E90FF&m                                                                                ").replace("%code%", code));

        // Enabled Status
        boolean enabled = codes.getBoolean("Codes." + code + ".enabled", true);
        String status = enabled ? msgConfig.getString("commands.list.status-enabled", "&#00FF7F(Enabled)") : msgConfig.getString("commands.list.status-disabled", "&#FF4500(Disabled)");
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.status", "<hover:&#E0E0E0Click to toggle status><click:run_command:/rc edit %code% enabled toggle>&#1E90FF➤ &#E0E0E0Status: %status_formatted%</click></hover>").replace("%code%", code).replace("%status_formatted%", status));

        // Limits
        int playerLimit = codes.getInt("Codes." + code + ".redeem-limit.player", 1);
        int ipLimit = codes.getInt("Codes." + code + ".redeem-limit.ip", 1);
        int globalLimit = codes.getInt("Codes." + code + ".redeem-limit.global", -1);
        String globalStr = globalLimit == -1 ? "Infinite" : String.valueOf(globalLimit);

        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.limits", "<hover:&#E0E0E0Click to edit limits><click:suggest_command:/rc edit %code% limit-global >&#1E90FF➤ &#E0E0E0Limits: &#00BFFFPlayer: &#E0E0E0%player% &#87CEFA| &#00BFFFIP: &#E0E0E0%ip% &#87CEFA| &#00BFFFGlobal: &#E0E0E0%global%</click></hover>")
                .replace("%code%", code)
                .replace("%player%", String.valueOf(playerLimit))
                .replace("%ip%", String.valueOf(ipLimit))
                .replace("%global%", globalStr));

        // Cooldown
        int cooldownMins = codes.getInt("Codes." + code + ".redeem-limit.Cooldown", 0);
        String cooldownStr = cooldownMins == 0 ? "None" : cooldownMins + " minutes";
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.cooldown", "<hover:&#E0E0E0Click to edit cooldown><click:suggest_command:/rc edit %code% cooldown >&#1E90FF➤ &#E0E0E0Cooldown: &#00BFFF%cooldown%</click></hover>")
                .replace("%code%", code)
                .replace("%cooldown%", cooldownStr));

        // Expire Time
        long expireSeconds = codes.getLong("Codes." + code + ".expire-time", -1L);
        String expireStr = expireSeconds == -1L ? "Never" : TimeFormatter.formatDuration(expireSeconds * 1000L);
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.expire", "<hover:&#E0E0E0Click to edit expiration><click:suggest_command:/rc edit %code% expire >&#1E90FF➤ &#E0E0E0Expires: &#00BFFF%expire%</click></hover>")
                .replace("%code%", code)
                .replace("%expire%", expireStr));

        // Permission
        boolean permReq = codes.getBoolean("Codes." + code + ".permisson.required", false);
        List<String> perms = codes.getStringList("Codes." + code + ".permisson.list");
        String permStatus = permReq ? "&#00FF7FRequired" : "&#E0E0E0Not Required";
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.permission", "<hover:&#E0E0E0Click to toggle permission requirement><click:run_command:/rc edit %code% permission toggle>&#1E90FF➤ &#E0E0E0Permission: %perm_status% &#87CEFA(%count% perms)</click></hover>")
                .replace("%code%", code)
                .replace("%perm_status%", permStatus)
                .replace("%count%", String.valueOf(perms.size())));

        // Blacklist
        String blType = codes.getString("Codes." + code + ".Playerlist.Blacklist.Type", "ENABLED");
        List<String> blList = codes.getStringList("Codes." + code + ".Playerlist.Blacklist.List");
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.blacklist", "<hover:&#E0E0E0Click to toggle blacklist mode><click:run_command:/rc edit %code% blacklist toggle>&#1E90FF➤ &#E0E0E0Blacklist: &#00BFFF%type% &#87CEFA(%count% players)</click></hover>")
                .replace("%code%", code)
                .replace("%type%", blType)
                .replace("%count%", String.valueOf(blList.size())));

        // Rewards
        sendRewardsView(player, code, codes, msgConfig, "Codes." + code + ".rewards");

        // Footer
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.list.footer", "&#1E90FF&m                                                                                "));
        return true;
    }

    private void sendRewardsView(Player player, String codeName, FileConfiguration codes, FileConfiguration msgConfig, String rewardPath) {
        String type = codes.getString(rewardPath + ".type", "RANDOM");
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.type", "<hover:&#E0E0E0Click to change reward type><click:suggest_command:/rc edit %code% reward settype >&#1E90FF➤ &#E0E0E0Reward Type: &#00BFFF%type%</click></hover>")
                .replace("%code%", codeName)
                .replace("%type%", type));

        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, msgConfig.getString("commands.show.rewards-header", "&#1E90FF➤ &#E0E0E0Rewards:"));

        String catHeader = msgConfig.getString("commands.show.category-header", "  &#00BFFF%category% &#87CEFA(%count%)");
        String catItem = msgConfig.getString("commands.show.category-item", "    <hover:&#00BFFFClick to remove reward><click:run_command:/rc edit %code% reward remove %type% %item%>&#E0E0E0- %item%</click></hover>");

        ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
        if (cmdSection != null && !cmdSection.getKeys(false).isEmpty()) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catHeader.replace("%category%", "Command Packs").replace("%count%", String.valueOf(cmdSection.getKeys(false).size())));
            for (String pack : cmdSection.getKeys(false)) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catItem.replace("%code%", codeName).replace("%type%", "command").replace("%item%", pack));
            }
        }

        List<String> sacks = codes.getStringList(rewardPath + ".sacks");
        if (!sacks.isEmpty()) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catHeader.replace("%category%", "Sacks").replace("%count%", String.valueOf(sacks.size())));
            for (String s : sacks) {
                String sName = s.split(":")[0];
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catItem.replace("%code%", codeName).replace("%type%", "sack").replace("%item%", sName));
            }
        }

        List<String> premades = codes.getStringList(rewardPath + ".premades");
        if (!premades.isEmpty()) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catHeader.replace("%category%", "Premades").replace("%count%", String.valueOf(premades.size())));
            for (String p : premades) {
                String pName = p.split(":")[0];
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catItem.replace("%code%", codeName).replace("%type%", "premade").replace("%item%", pName));
            }
        }

        List<String> events = codes.getStringList(rewardPath + ".events");
        if (!events.isEmpty()) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, catHeader.replace("%category%", "Events").replace("%count%", String.valueOf(events.size())));
            for (String e : events) {
                String eItem = msgConfig.getString("commands.show.category-item-event", "    <hover:&#00BFFFClick to edit><click:suggest_command:/rc edit %code% reward setevent >&#E0E0E0- %item%</click></hover>");
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, eItem.replace("%code%", codeName).replace("%item%", e));
            }
        }
    }

    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (plugin.getCodesConfig().getConfigurationSection("Codes") != null) {
                return new java.util.ArrayList<>(plugin.getCodesConfig().getConfigurationSection("Codes").getKeys(false));
            }
        }
        return new java.util.ArrayList<>();
    }
}
