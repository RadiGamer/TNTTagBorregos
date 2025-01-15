package org.imradigamer.tNTTagBorregos;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TntTagManager {

    private final JavaPlugin plugin;
    private final List<TNTTagLobby> lobbies = new ArrayList<>();
    private int lobbyCount = 0; // Counter for unique lobby world names

    public TntTagManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void joinLobby(Player player) {
        for (TNTTagLobby lobby : lobbies) {
            if (!lobby.isFull() && !lobby.isGameActive()) {
                lobby.addPlayer(player);
                if (lobby.getPlayers().size() >= 2) {
                    lobby.startCountdown();
                }
                return;
            }
        }

        createNewLobby(player);
    }

    private void createNewLobby(Player player) {
        Bukkit.getLogger().info("Creando un nuevo lobby de TNT Tag...");

        String baseWorldName = "TNTTagArena";
        String newWorldName = "TNTTagLobby_" + (++lobbyCount);

        if (!cloneWorld(baseWorldName, newWorldName)) {
            Bukkit.getLogger().severe("No se pudo crear un nuevo lobby. Por favor, contacta al administrador.");
            return;
        }

        World newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            Bukkit.getLogger().severe("No se pudo cargar el mundo del nuevo lobby: " + newWorldName);
            return;
        }

        TNTTagLobby newLobby = new TNTTagLobby(plugin, this, newWorld);
        lobbies.add(newLobby);

        newLobby.addPlayer(player);
        newLobby.sendMessageToLobby("Esperando más jugadores...");
    }

    private boolean cloneWorld(String baseWorldName, String newWorldName) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv clone " + baseWorldName + " " + newWorldName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("No se pudo clonar el mundo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeLobby(TNTTagLobby lobby) {
        lobbies.remove(lobby);

        String worldName = lobby.getWorld().getName();

        if (Bukkit.unloadWorld(worldName, false)) {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            Bukkit.unloadWorld(Bukkit.getWorld(worldName), false);
            if (worldFolder.exists()) {
                deleteDirectory(worldFolder);
            }
        } else {
            plugin.getLogger().warning("No se pudo descargar el mundo: " + worldName);
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
    public List<TNTTagLobby> getLobbies() {
        return lobbies;
    }

}
