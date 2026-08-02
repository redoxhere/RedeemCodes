package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
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
        if (!(sender instanceof Player player)) return true;

        List<String> helpCommands = Arrays.asList(
                "&b/rc create <code> &7- Create a new code",
                "&b/rc remove <code> &7- Remove a code",
                "&b/rc reward <code> <action> &7- Manage rewards",
                "&b/rc sack create <name> &7- Create sack",
                "&b/rc sack edit <name> &7- Edit sack",
                "&b/rc sack give <name> &7- Give sack",
                "&b/rc event create <name> &7- Create event",
                "&b/rc event add <name> <type> &7- Add actions to event",
                "&b/rc event play <name> &7- Play/Test event",
                "&b/rc premade add <name> <cmd> &7- Manage premades",
                "&b/rc show <code> &7- Show details",
                "&b/rc list &7- List codes",
                "&b/rc redeemed <code> &7- List usage",
                "&b/rc reload &7- Reload config",
                "&b/rc gui &7- Open GUI"
        );
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

        player.sendMessage(plugin.color("&e&m                                                                       "));
        player.sendMessage(plugin.color(" &d&lRedeemCodes Help &r&d- Page " + page + "/" + totalPages));
        int startIndex = (page - 1) * commandsPerPage;

        for (int i = 0; i < commandsPerPage; ++i) {
            int commandIndex = startIndex + i;
            if (commandIndex >= helpCommands.size()) {
                break;
            }

            player.sendMessage(plugin.color(helpCommands.get(commandIndex)));
        }

        player.sendMessage(plugin.color("&e&m                                                                       "));
        return true;
    }
}
