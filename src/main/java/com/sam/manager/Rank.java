package com.sam.manager;

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

    // Setters
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setNameColor(ChatColor nameColor) { this.nameColor = nameColor; }
    public void setChatColor(ChatColor chatColor) { this.chatColor = chatColor; }

    // Permission management
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }
}