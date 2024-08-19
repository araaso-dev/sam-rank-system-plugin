package com.sam.commands;

import com.sam.Main;
import com.sam.manager.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RankTabCompleter implements TabCompleter {

    private Main plugin;
    private static final List<String> COMMANDS = Arrays.asList("create", "delete", "setplayer", "addperm", "removeperm", "setdefault", "list", "listperms");
    private static final List<String> COLORS = Arrays.asList("BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE");

    public RankTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        } else if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length == 4 || args.length == 5) {
                        StringUtil.copyPartialMatches(args[args.length - 1], COLORS, completions);
                    }
                    break;
                case "delete":
                case "addperm":
                case "removeperm":
                case "setdefault":
                case "listperms":
                    if (args.length == 2) {
                        List<String> rankNames = plugin.getRankManager().getAllRanks().stream()
                                .map(Rank::getName)
                                .collect(Collectors.toList());
                        StringUtil.copyPartialMatches(args[1], rankNames, completions);
                    }
                    break;
                case "setplayer":
                    if (args.length == 2) {
                        List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList());
                        StringUtil.copyPartialMatches(args[1], playerNames, completions);
                    } else if (args.length == 3) {
                        List<String> rankNames = plugin.getRankManager().getAllRanks().stream()
                                .map(Rank::getName)
                                .collect(Collectors.toList());
                        StringUtil.copyPartialMatches(args[2], rankNames, completions);
                    }
                    break;
            }
        }

        return completions;
    }
}