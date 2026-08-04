package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

import java.util.List;
import java.util.UUID;

public class RedeemedCommand implements Subcommand {
    private final Main plugin;

    public RedeemedCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rc redeemed <code> [page]");
        } else {
            String codeName = args[1];
            List<String> redeemedPlayersUuids = plugin.getRedeemDataManager().getRedeemedPlayers(codeName);
            if (redeemedPlayersUuids.isEmpty()) {
                player.sendMessage(plugin.color("&cNo one has redeemed the code '&e" + codeName + "&c' yet."));
            } else {
                player.sendMessage(plugin.color("&dRedeemed count: " + redeemedPlayersUuids.size()));
                int limit = Math.min(redeemedPlayersUuids.size(), 10);

                for (int i = 0; i < limit; ++i) {
                    String uuid = redeemedPlayersUuids.get(i);

                    try {
                        String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                        player.sendMessage(plugin.color("&7- " + (name != null ? name : uuid)));
                    } catch (Exception e) {
                        player.sendMessage(plugin.color("&7- " + uuid));
                    }
                }

                if (redeemedPlayersUuids.size() > 10) {
                    player.sendMessage(plugin.color("&7... and " + (redeemedPlayersUuids.size() - 10) + " more."));
                }
            }
        }
        return true;
    }
}
