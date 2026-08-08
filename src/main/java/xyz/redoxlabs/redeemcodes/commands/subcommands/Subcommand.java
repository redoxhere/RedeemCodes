package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand {
    boolean execute(CommandSender sender, String[] args);
    List<String> onTabComplete(CommandSender sender, String[] args);
}
