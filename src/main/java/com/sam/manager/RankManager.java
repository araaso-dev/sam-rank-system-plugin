package com.sam.manager;

import com.sam.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {
    private Main plugin;
    private File ranksFile;
    private FileConfiguration ranksConfig;
    private Map<String, Rank> ranks;
    private String defaultRank;
    private Map<UUID, String> playerRanks;

    public RankManager(Main plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();
        this.playerRanks = new HashMap<>();
        loadRanks();
        loadPlayerRanks();
    }

    public void loadRanks() {
        ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);

        defaultRank = ranksConfig.getString("default_rank", "rookie");

        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("ranks");
        if (ranksSection != null) {
            for (String rankName : ranksSection.getKeys(false)) {
                ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankName);
                if (rankSection != null) {
                    String prefix = ChatColor.translateAlternateColorCodes('&', rankSection.getString("prefix", ""));
                    ChatColor nameColor = ChatColor.getByChar(rankSection.getString("name_color", "&f").replace("&", ""));
                    ChatColor chatColor = ChatColor.getByChar(rankSection.getString("chat_color", "&f").replace("&", ""));
                    List<String> permissions = rankSection.getStringList("permissions");

                    ranks.put(rankName.toLowerCase(), new Rank(rankName, prefix, nameColor, chatColor, permissions));
                }
            }
        }
    }

    public void saveRanks() {
        ranksConfig.set("default_rank", defaultRank);
        ConfigurationSection ranksSection = ranksConfig.createSection("ranks");

        for (Map.Entry<String, Rank> entry : ranks.entrySet()) {
            Rank rank = entry.getValue();
            ConfigurationSection rankSection = ranksSection.createSection(entry.getKey());
            rankSection.set("prefix", rank.getPrefix().replace(ChatColor.COLOR_CHAR, '&'));
            rankSection.set("name_color", "&" + rank.getNameColor().getChar());
            rankSection.set("chat_color", "&" + rank.getChatColor().getChar());
            rankSection.set("permissions", rank.getPermissions());
        }

        try {
            ranksConfig.save(ranksFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ranks.yml: " + e.getMessage());
        }
    }

    private void loadPlayerRanks() {
        File playerRanksFile = new File(plugin.getDataFolder(), "playerranks.yml");
        if (!playerRanksFile.exists()) {
            try {
                playerRanksFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerranks.yml: " + e.getMessage());
                return;
            }
        }
        FileConfiguration playerRanksConfig = YamlConfiguration.loadConfiguration(playerRanksFile);

        for (String uuidString : playerRanksConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            String rankName = playerRanksConfig.getString(uuidString);
            playerRanks.put(uuid, rankName);
        }
    }

    private void savePlayerRanks() {
        File playerRanksFile = new File(plugin.getDataFolder(), "playerranks.yml");
        FileConfiguration playerRanksConfig = new YamlConfiguration();

        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            playerRanksConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            playerRanksConfig.save(playerRanksFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerranks.yml: " + e.getMessage());
        }
    }

    public Rank getRank(String rankName) {
        return ranks.get(rankName.toLowerCase());
    }

    public void setPlayerRank(UUID playerUUID, String rankName) {
        if (ranks.containsKey(rankName.toLowerCase())) {
            playerRanks.put(playerUUID, rankName.toLowerCase());
            savePlayerRanks();

            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                updatePlayerPermissions(player);
            }
        }
    }

    public Rank getPlayerRank(UUID playerUUID) {
        String rankName = playerRanks.get(playerUUID);
        if (rankName == null) {
            rankName = defaultRank;
        }
        return getRank(rankName);
    }

    public void createRank(String rankName, String prefix, ChatColor nameColor, ChatColor chatColor) {
        Rank newRank = new Rank(rankName, prefix, nameColor, chatColor, new ArrayList<>());
        ranks.put(rankName.toLowerCase(), newRank);
        saveRanks();
    }

    public void deleteRank(String rankName) {
        ranks.remove(rankName.toLowerCase());
        saveRanks();
    }

    public void addPermission(String rankName, String permission) {
        Rank rank = getRank(rankName);
        if (rank != null) {
            rank.addPermission(permission);
            saveRanks();
            updateAllPlayersPermissions();
        }
    }

    public void removePermission(String rankName, String permission) {
        Rank rank = getRank(rankName);
        if (rank != null) {
            rank.removePermission(permission);
            saveRanks();
            updateAllPlayersPermissions();
        }
    }

    public String getDefaultRank() {
        return defaultRank;
    }

    public void setDefaultRank(String rankName) {
        if (ranks.containsKey(rankName.toLowerCase())) {
            defaultRank = rankName.toLowerCase();
            saveRanks();
        }
    }

    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }

    public void updatePlayerPermissions(Player player) {
        Rank rank = getPlayerRank(player.getUniqueId());
        player.getEffectivePermissions().forEach(permissionAttachmentInfo -> {
            if (permissionAttachmentInfo.getAttachment() != null) {
                permissionAttachmentInfo.getAttachment().unsetPermission(permissionAttachmentInfo.getPermission());
            }
        });
        rank.getPermissions().forEach(permission -> player.addAttachment(plugin, permission, true));
    }

    private void updateAllPlayersPermissions() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayerPermissions(player);
        }
    }
}