package com.sam;

import com.sam.commands.MessageCommand;
import com.sam.commands.RankCommand;
import com.sam.commands.RankTabCompleter;
import com.sam.listeners.RankListener;
import com.sam.managers.DatabaseManager;
import com.sam.managers.NametagManager;
import com.sam.managers.RankManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DatabaseManager databaseManager;
    private RankManager rankManager;
    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize managers
        this.rankManager = new RankManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.nametagManager = new NametagManager(this);

        // Initialize database
        this.databaseManager.initializeDatabase();

        // Register commands and tab completer
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("rank").setTabCompleter(new RankTabCompleter(this));
        getCommand("message").setExecutor(new MessageCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new RankListener(this), this);

        // Update all online players (in case of reload)
        getServer().getOnlinePlayers().forEach(player -> {
            rankManager.updatePlayerPermissions(player);
            nametagManager.setNameTags(player);
        });

        getLogger().info("Sam Rank System has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Sam Rank System has been disabled!");
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public RankManager getRankManager() { return rankManager; }
    public NametagManager getNametagManager() { return nametagManager; }
}