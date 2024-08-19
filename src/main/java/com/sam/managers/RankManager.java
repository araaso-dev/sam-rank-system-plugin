package com.sam.managers;

import com.sam.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class RankManager {
    private Main plugin;
    private String defaultRankName;
    private Map<UUID, PermissionAttachment> playerPermissions;

    public RankManager(Main plugin) {
        this.plugin = plugin;
        this.playerPermissions = new HashMap<>();
        this.defaultRankName = "rookie"; // Set a default rank name
    }

    public void setPlayerRank(UUID playerUUID, String rankName) {
        plugin.getDatabaseManager().setPlayerRank(playerUUID, rankName);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            updatePlayerPermissions(player);
            plugin.getNametagManager().updatePlayerNameTag(player);
        }
    }

    public Rank getPlayerRank(UUID playerUUID) {
        return plugin.getDatabaseManager().getPlayerRank(playerUUID);
    }

    public void createRank(String name, String prefix, ChatColor nameColor, ChatColor chatColor) {
        plugin.getDatabaseManager().createRank(name, prefix, nameColor, chatColor);
    }

    public void deleteRank(String name) {
        plugin.getDatabaseManager().deleteRank(name);
    }

    public void addPermission(String rankName, String permission) {
        plugin.getDatabaseManager().addPermission(rankName, permission);
        updateAllPlayersPermissions();
    }

    public void removePermission(String rankName, String permission) {
        plugin.getDatabaseManager().removePermission(rankName, permission);
        updateAllPlayersPermissions();
    }

    public List<String> getRankPermissions(String rankName) {
        return plugin.getDatabaseManager().getRankPermissions(rankName);
    }

    public Rank getRank(String rankName) {
        return plugin.getDatabaseManager().getRank(rankName);
    }

    public List<Rank> getAllRanks() {
        return plugin.getDatabaseManager().getAllRanks();
    }

    public String getDefaultRank() {
        return defaultRankName;
    }

    public void setDefaultRank(String rankName) {
        this.defaultRankName = rankName;
        // You might want to save this to a configuration file
    }

    public void updatePlayerPermissions(Player player) {
        Rank rank = getPlayerRank(player.getUniqueId());
        if (rank == null) {
            return;
        }

        PermissionAttachment attachment = playerPermissions.get(player.getUniqueId());
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            playerPermissions.put(player.getUniqueId(), attachment);
        }

        for (String permission : attachment.getPermissions().keySet()) {
            attachment.unsetPermission(permission);
        }

        for (String permission : rank.getPermissions()) {
            attachment.setPermission(permission, true);
        }

        player.recalculatePermissions();
    }

    public void updateRankColors(String rankName, ChatColor nameColor, ChatColor chatColor) {
        plugin.getDatabaseManager().updateRankColors(rankName, nameColor, chatColor);
        // You might want to update any cached rank information here
        // and update players with this rank
        updatePlayersWithRank(rankName);
    }

    public void updateRankPrefix(String rankName, String newPrefix) {
        plugin.getDatabaseManager().updateRankPrefix(rankName, newPrefix);
        // Update any cached rank information and affected players
        updatePlayersWithRank(rankName);
    }

    private void updatePlayersWithRank(String rankName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Rank playerRank = getPlayerRank(player.getUniqueId());
            if (playerRank != null && playerRank.getName().equalsIgnoreCase(rankName)) {
                plugin.getNametagManager().updatePlayerNameTag(player);
            }
        }
    }

    private void updateAllPlayersPermissions() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerPermissions(player);
        }
    }
}