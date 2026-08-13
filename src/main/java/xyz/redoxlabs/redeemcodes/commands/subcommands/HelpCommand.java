package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

import java.util.Arrays;
import java.util.List;

public class HelpCommand implements Subcommand {
    private final Main plugin;

    public HelpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        List<String> helpCommands = Arrays.asList(
                "/rc create <code> - Create a new code",
                "/rc remove <code> - Remove a code",
                "/rc reward <code> <action> - Manage rewards",
                "/rc sack create <name> - Create sack",
                "/rc sack edit <name> - Edit sack",
                "/rc sack give <name> - Give sack",
                "/rc event create <name> - Create event",
                "/rc event add <name> <type> - Add actions to event",
                "/rc event play <name> - Play/Test event",
                "/rc premade add <name> <cmd> - Manage premades",
                "/rc show <code> - Show details",
                "/rc list - List codes",
                "/rc redeemed <code> - List usage",
                "/rc reload - Reload config",
                "/rc gui - Open GUI",
                "/rc test <code> - Dry run a code",
                "/rc info - Plugin information"
        );
        
        FileConfiguration config = plugin.getMessagesConfig();
        int commandsPerPage = 6;
        int totalPages = (int) Math.ceil((double) helpCommands.size() / (double) commandsPerPage);
        int page = 1;
        
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
            }
        }

        if (page < 1) {
            page = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, config.getString("commands.help.header", "&#1E90FF&m                                                                                &r &#00BFFFRedeemCodes Help &r&#1E90FF&m                                                                                "));
        
        int startIndex = (page - 1) * commandsPerPage;

        String itemTemplate = config.getString("commands.help.item", "<hover:&eClick to auto-fill><click:suggest_command:%command%>&#00BFFF%command% &#E0E0E0%description%</click></hover>");

        for (int i = 0; i < commandsPerPage; ++i) {
            int commandIndex = startIndex + i;
            if (commandIndex >= helpCommands.size()) {
                break;
            }

            String[] split = helpCommands.get(commandIndex).split(" - ", 2);
            String cmd = split[0];
            String desc = split.length > 1 ? split[1] : "";

            String line = itemTemplate.replace("%command%", cmd).replace("%description%", desc);
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, line);
        }

        String pagination = config.getString("commands.help.pagination", "<hover:&ePrevious Page><click:run_command:/rc help %prev_page%>&#1E90FF[«]</click></hover> &#E0E0E0Page %page%/%max_page% <hover:&eNext Page><click:run_command:/rc help %next_page%>&#1E90FF[»]</click></hover>");
        pagination = pagination.replace("%page%", String.valueOf(page))
                               .replace("%max_page%", String.valueOf(totalPages))
                               .replace("%prev_page%", String.valueOf(Math.max(1, page - 1)))
                               .replace("%next_page%", String.valueOf(Math.min(totalPages, page + 1)));
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, pagination);
        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, config.getString("commands.help.footer", "&#1E90FF&m                                                                                "));
        return true;
    }
    
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return new java.util.ArrayList<>();
    }
}
