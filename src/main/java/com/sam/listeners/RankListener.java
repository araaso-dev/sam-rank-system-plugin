package com.sam.listeners;

import com.sam.Main;
import com.sam.managers.Rank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RankListener implements Listener {

    private Main plugin;

    public RankListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseManager().addPlayer(player);
        Rank playerRank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (playerRank == null) {
            String defaultRankName = plugin.getRankManager().getDefaultRank();
            plugin.getRankManager().setPlayerRank(player.getUniqueId(), defaultRankName);
        }
        plugin.getNametagManager().setNameTags(player);
        plugin.getRankManager().updatePlayerPermissions(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getNametagManager().removePlayerNameTag(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Rank playerRank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (playerRank == null) {
            String defaultRankName = plugin.getRankManager().getDefaultRank();
            playerRank = plugin.getRankManager().getRank(defaultRankName);
        }

        String message = event.getMessage();
        if (player.hasPermission("samrank.chatcolor")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        } else {
            message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
        }

        String formattedMessage = playerRank.getPrefix() + " " +
                playerRank.getNameColor() + player.getName() + ": " +
                playerRank.getChatColor() + message;

        for (Player recipient : event.getRecipients()) {
            recipient.sendMessage(formattedMessage);
        }
    }
}