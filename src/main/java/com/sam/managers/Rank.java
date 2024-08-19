package com.sam.managers;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Rank {
    private String name;
    private String prefix;
    private ChatColor nameColor;
    private ChatColor chatColor;
    private List<String> permissions;

    public Rank(String name, String prefix, ChatColor nameColor, ChatColor chatColor, List<String> permissions) {
        this.name = name;
        this.prefix = prefix;
        this.nameColor = nameColor;
        this.chatColor = chatColor;
        this.permissions = new ArrayList<>(permissions);
    }

    // Getters
    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public ChatColor getNameColor() { return nameColor; }
    public ChatColor getChatColor() { return chatColor; }
    public List<String> getPermissions() { return new ArrayList<>(permissions); }

    // Setters are removed as ranks are now managed through the database
}