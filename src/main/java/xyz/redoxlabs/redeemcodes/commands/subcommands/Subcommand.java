package xyz.redoxlabs.redeemcodes.commands.subcommands;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    /**
     * Executes the subcommand.
     *
     * @param sender The sender of the command.
     * @param args   The arguments passed to the command.
     * @return true if successful, false otherwise.
     */
    boolean execute(CommandSender sender, String[] args);
}
