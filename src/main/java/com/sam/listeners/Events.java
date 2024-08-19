package com.sam.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.GREEN + "Welcome to the server, " + event.getPlayer().getName() + "!");
    }
}
