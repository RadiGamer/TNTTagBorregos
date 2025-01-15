package org.imradigamer.tNTTagBorregos;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TNTTagListener implements Listener {

    private final TntTagManager tntTagManager;

    public TNTTagListener(TntTagManager tntTagManager) {
        this.tntTagManager = tntTagManager;
    }

    // Prevent fall damage for all players
    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    // Prevent hunger damage for all players
    @EventHandler
    public void onPlayerHungerLoss(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.STARVATION) {
            event.setCancelled(true);
        }
    }

    // Launch players upwards when they step on a warped pressure plate
    @EventHandler
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null &&
                event.getClickedBlock().getType() == Material.WARPED_PRESSURE_PLATE) {
            Player player = event.getPlayer();
            player.setVelocity(player.getVelocity().setY(1.8)); // Launch upward
            player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 1);
        }
    }

    // Pass TNT when a player hits another player
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        // Ensure both entities are players
        if (!(damager instanceof Player) || !(damaged instanceof Player)) return;

        Player attacker = (Player) damager;
        Player target = (Player) damaged;

        // Find the lobby the players are in
        TNTTagLobby attackerLobby = findPlayerLobby(attacker);
        TNTTagLobby targetLobby = findPlayerLobby(target);

        // Ensure both players are in the same lobby
        if (attackerLobby == null || attackerLobby != targetLobby) return;

        // Ensure the attacker has TNT
        if (!attackerLobby.isTNTPlayer(attacker)) return;

        // Transfer TNT to the target
        attackerLobby.passTNT(attacker, target);
        target.getWorld().playEffect(target.getLocation(), Effect.SMOKE, 5);
        event.setCancelled(true); // Prevent any damage
    }

    private TNTTagLobby findPlayerLobby(Player player) {
        for (TNTTagLobby lobby : tntTagManager.getLobbies()) {
            if (lobby.getPlayers().contains(player)) {
                return lobby;
            }
        }
        return null;
    }
}
