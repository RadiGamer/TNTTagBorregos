package org.imradigamer.tNTTagBorregos;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TNTTagCommand implements CommandExecutor {

    private final TntTagManager tntTagManager;

    public TNTTagCommand(TntTagManager tntTagManager) {
        this.tntTagManager = tntTagManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("¡Solo los jugadores pueden usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tnttag")) {
            tntTagManager.joinLobby(player);
            return true;
        }

        return false;
    }
}
