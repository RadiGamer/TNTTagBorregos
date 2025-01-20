package org.imradigamer.tNTTagBorregos;

import org.bukkit.Bukkit;
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
        if (command.getName().equalsIgnoreCase("tnttag")) {
            // Ensure the command is executed by a player
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c¡Este comando solo puede ser ejecutado por jugadores!");
                return true;
            }

            Player player = (Player) sender;

            // Join the lobby
            tntTagManager.joinLobby(player);
            player.sendMessage("§a¡Te has unido al lobby de TNT Tag!");

            return true;
        }

        return false;
    }
}
