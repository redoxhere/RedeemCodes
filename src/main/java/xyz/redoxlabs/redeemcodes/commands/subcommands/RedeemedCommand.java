package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

        FileConfiguration config = plugin.getConfig();

        if (args.length < 2) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.redeemed.usage", "&#FF6347Usage: /rc redeemed <code> [page]"));
        } else {
            String codeName = args[1];
            List<String> redeemedPlayersUuids = plugin.getRedeemDataManager().getRedeemedPlayers(codeName);
            if (redeemedPlayersUuids.isEmpty()) {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMessage(plugin, player, "commands.redeemed.empty");
            } else {
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.redeemed.header", "&d---- &bRedemptions: &f%code% &d----").replace("%code%", codeName));
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.redeemed.count", "&7Total Redemptions: &e%count%").replace("%count%", String.valueOf(redeemedPlayersUuids.size())));
                
                int limit = Math.min(redeemedPlayersUuids.size(), 10);
                String itemTemplate = plugin.getMessagesConfig().getString("commands.redeemed.item", "  &b• &f%player%");

                for (int i = 0; i < limit; ++i) {
                    String uuid = redeemedPlayersUuids.get(i);

                    try {
                        String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, itemTemplate.replace("%player%", (name != null ? name : uuid)));
                    } catch (Exception e) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, itemTemplate.replace("%player%", uuid));
                    }
                }

                if (redeemedPlayersUuids.size() > 10) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.redeemed.more", "  &7... and %more% more.").replace("%more%", String.valueOf(redeemedPlayersUuids.size() - 10)));
                }
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("commands.list.footer", "&d------------------------"));
            }
        }
        return true;
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
