package org.imradigamer.tNTTagBorregos;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.imradigamer.tNTTagBorregos.TntTagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TNTTagLobby {

    private final JavaPlugin plugin;
    private final TntTagManager tntTagManager;
    private final World world;
    private final List<Player> players = new ArrayList<>();
    private final List<Player> tntPlayers = new ArrayList<>();
    private boolean gameActive = false;

    private static final String PREFIX = ChatColor.DARK_AQUA + "[BORREGOS] " + ChatColor.RESET;

    public TNTTagLobby(JavaPlugin plugin, TntTagManager tntTagManager, World world) {
        this.plugin = plugin;
        this.tntTagManager = tntTagManager;
        this.world = world;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.teleport(world.getSpawnLocation());
        sendMessageToLobby(player.getName() + " se ha unido al lobby.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        checkPlayersForCountdown();
    }

    public boolean isFull() {
        return players.size() >= 20; // Example max players
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public World getWorld() {
        return world;
    }

    private void checkPlayersForCountdown() {
        if (players.size() >= 2 && !gameActive) {
            sendMessageToLobby("Hay suficientes jugadores. El juego comenzará pronto...");
            startCountdown();
        } else if (!gameActive) {
            sendMessageToLobby("Esperando a más jugadores para comenzar...");
        }
    }

    public void startCountdown() {
        if (gameActive || players.size() < 2) return;

        sendMessageToLobby("El lobby está listo. El juego comenzará en 30 segundos...");
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::startGame, 30 * 20L);
    }

    private void startGame() {
        if (gameActive) {
            return;
        }

        sendMessageToLobby("¡El juego está comenzando!");
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
        gameActive = true;

        assignTNTPlayers();

        Bukkit.getScheduler().runTaskLater(plugin, this::endRound, 60 * 20L); // 1-minute round
    }

    private void assignTNTPlayers() {
        int tntCount = Math.max(1, players.size() / 5); // 1 TNT per 5 players
        Random random = new Random();

        for (int i = 0; i < tntCount; i++) {
            Player tntPlayer = players.get(random.nextInt(players.size()));
            tntPlayers.add(tntPlayer);
            tntPlayer.getInventory().addItem(new ItemStack(Material.TNT)); // Give TNT item
            tntPlayer.setGlowing(true); // Glow red
            tntPlayer.playSound(tntPlayer.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        }

        sendMessageToLobby("¡" + tntPlayers.size() + " jugadores ahora tienen TNT! ¡Corre!");
    }

    private void endRound() {
        // Play explosion sound to all players in the lobby
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }

        for (Player tntPlayer : tntPlayers) {
            tntPlayer.setGlowing(false);
            tntPlayer.sendTitle("§cEliminado", "", 10, 70, 20);

            World spawnWorld = Bukkit.getWorld("World");
            Location spawnLocation = spawnWorld.getSpawnLocation();
            tntPlayer.getInventory().clear();

            tntPlayer.teleport(spawnLocation);
            tntPlayer.playSound(tntPlayer.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
            players.remove(tntPlayer);
            sendMessageToLobby(tntPlayer.getName() + " fue eliminado.");
        }
        tntPlayers.clear();

        if (players.size() <= 1) {
            endLobby();
        } else {
            // Wait 5 seconds before starting the next round
            sendMessageToLobby("Preparándose para la siguiente ronda en 5 segundos...");
            Bukkit.getScheduler().runTaskLater(plugin, this::startGame, 5 * 20L);
        }
    }


    public void endLobby() {
        sendMessageToLobby("El juego ha terminado. Regresando al mundo de spawn en 10 segundos...");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World spawnWorld = Bukkit.getWorld("World");
            if (spawnWorld == null) {
                plugin.getLogger().severe("¡El mundo de spawn 'World' no está cargado!");
                return;
            }

            Location spawnLocation = spawnWorld.getSpawnLocation();

            // Teleport all players to the spawn world, including active players and spectators
            List<Player> allPlayers = new ArrayList<>(players);
            allPlayers.addAll(tntPlayers); // Ensure TNT holders are also teleported

            for (Player player : allPlayers) {
                player.teleport(spawnLocation);
                player.getInventory().clear();
                player.sendMessage(PREFIX + ChatColor.GREEN + "Has sido enviado al mundo de spawn.");
            }
            // Clear the player lists
            players.clear();
            tntPlayers.clear();

            // Notify the manager to unload and delete the arena
            tntTagManager.removeLobby(this);
        }, 10 * 20L); // 10 seconds
    }

    public void sendMessageToLobby(String message) {
        for (Player player : players) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + message);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    public void passTNT(Player from, Player to) {
        // Remove TNT effects from the current TNT player
        tntPlayers.remove(from);
        from.setGlowing(false);
        from.getInventory().remove(Material.TNT);
        from.playSound(from.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

        tntPlayers.add(to);
        to.setGlowing(true);
        to.getInventory().addItem(new ItemStack(Material.TNT));
        to.playSound(to.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

        // Notify players in the lobby
        sendMessageToLobby(ChatColor.RED + from.getName() + " le pasó el TNT a " + to.getName() + "!");
    }

    public boolean isTNTPlayer(Player player) {
        return tntPlayers.contains(player);
    }
}
