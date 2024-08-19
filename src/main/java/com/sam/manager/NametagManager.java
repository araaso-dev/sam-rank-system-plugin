package com.sam.manager;

import com.sam.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagManager {

    private Main plugin;
    private Scoreboard scoreboard;

    public NametagManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void setNameTags(Player player) {
        player.setScoreboard(scoreboard);

        for (Rank rank : plugin.getRankManager().getAllRanks()) {
            Team team = scoreboard.getTeam(rank.getName());
            if (team == null) {
                team = scoreboard.registerNewTeam(rank.getName());
            }
            team.setPrefix(rank.getPrefix() + " ");
            team.setColor(rank.getNameColor());
        }

        updatePlayerNameTag(player);
    }

    public void updatePlayerNameTag(Player player) {
        Rank rank = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        Team team = scoreboard.getTeam(rank.getName());
        if (team != null) {
            team.addEntry(player.getName());
        }
    }

    public void removePlayerNameTag(Player player) {
        for (Team team : scoreboard.getTeams()) {
            team.removeEntry(player.getName());
        }
    }

    public void updateAllPlayerNameTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerNameTag(player);
        }
    }
}