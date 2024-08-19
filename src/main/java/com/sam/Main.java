package com.sam;

import com.sam.commands.RankCommand;
import com.sam.commands.RankTabCompleter;
import com.sam.commands.MessageCommand;
import com.sam.listeners.RankListener;
import com.sam.manager.NametagManager;
import com.sam.manager.RankManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private RankManager rankManager;
    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize managers
        rankManager = new RankManager(this);
        nametagManager = new NametagManager(this);

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
        // Save ranks when the plugin is disabled
        rankManager.saveRanks();
        getLogger().info("Sam Rank System has been disabled!");
    }

    public RankManager getRankManager() { return rankManager; }
    public NametagManager getNametagManager() { return nametagManager; }
}