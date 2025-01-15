package org.imradigamer.tNTTagBorregos;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TNTTagBorregos extends JavaPlugin {


    private TntTagManager tntTagManager;

    @Override
    public void onEnable() {
        // Initialize the TNT Tag Manager
        tntTagManager = new TntTagManager(this);

        // Register the TNT Tag command
        this.getCommand("tnttag").setExecutor(new TNTTagCommand(tntTagManager));

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new TNTTagListener(tntTagManager), this);

        getLogger().info("TNT Tag Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TNT Tag Plugin Disabled");
    }
}
