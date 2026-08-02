package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.utils.MessageUtil;

import java.util.Arrays;

public class ReviewCommand implements Subcommand {
    private final Main plugin;

    public ReviewCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rc review <message>");
        } else {
            if (plugin.getConfig().getBoolean("send-review.review-message", true)) {
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String playerName = player.getName();
                String webhookUrl = "https://discord.com/api/webhooks/1415900269459542048/FNd7DLkVp2x2zdTs2b7f0UwbHA96MgeM0MpLne4Wst1k818HaarInJaSz2oNO_pcg6VJ";
                String jsonPayload = "{\"embeds\": [{\"title\": \"New Plugin Review\",\"color\": 15844367,\"fields\": [  {\"name\": \"Player\",\"value\": \"" + playerName + "\",\"inline\": true},  {\"name\": \"Message\",\"value\": \"" + message.replace("\"", "\\\"") + "\",\"inline\": false}],\"footer\": {\"text\": \"RedeemCodes Review System\"}}]}";
                plugin.sendToWebhook(webhookUrl, jsonPayload);
                player.sendMessage(plugin.color("&aThank you! Your review has been sent."));
                MessageUtil.playSound(plugin, player, "sounds.success");
            } else {
                player.sendMessage(plugin.color("&cThe server owner has disabled this feature."));
            }
        }
        return true;
    }
}
