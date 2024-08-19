package com.sam.commands;

import com.sam.Main;
import com.sam.managers.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    private Main plugin;

    public RankCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /rank <create|delete|setplayer|addperm|removeperm|setdefault|list|listperms>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (!sender.hasPermission("samrank.create") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank create <name> <prefix> <nameColor> <chatColor>");
                    return true;
                }
                createRank(sender, args[1], args[2], args[3], args[4]);
                break;
            case "delete":
                if (!sender.hasPermission("samrank.delete") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank delete <name>");
                    return true;
                }
                deleteRank(sender, args[1]);
                break;
            case "setplayer":
                if (!sender.hasPermission("samrank.setplayer") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank setplayer <player> <rank>");
                    return true;
                }
                setPlayerRank(sender, args[1], args[2]);
                break;
            case "addperm":
                if (!sender.hasPermission("samrank.addperm") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank addperm <rank> <permission>");
                    return true;
                }
                addPermission(sender, args[1], args[2]);
                break;
            case "removeperm":
                if (!sender.hasPermission("samrank.removeperm") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank removeperm <rank> <permission>");
                    return true;
                }
                removePermission(sender, args[1], args[2]);
                break;
            case "setdefault":
                if (!sender.hasPermission("samrank.setdefault") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank setdefault <rank>");
                    return true;
                }
                setDefaultRank(sender, args[1]);
                break;
            case "list":
                if (!sender.hasPermission("samrank.list") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                listRanks(sender);
                break;
            case "setcolors":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank setcolors <rankName> <nameColor> <chatColor>");
                    return true;
                }
                setRankColors(sender, args[1], args[2], args[3]);
                break;
            case "setprefix":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank setprefix <rankName> <newPrefix>");
                    return true;
                }
                setRankPrefix(sender, args[1], args[2]);
                break;
            case "listperms":
                if (!sender.hasPermission("samrank.listperms") && !sender.hasPermission("samrank.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank listperms <rank>");
                    return true;
                }
                listRankPermissions(sender, args[1]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /rank for help.");
        }

        return true;
    }

    private void createRank(CommandSender sender, String name, String prefix, String nameColor, String chatColor) {
        try {
            ChatColor nameColorEnum = ChatColor.valueOf(nameColor.toUpperCase());
            ChatColor chatColorEnum = ChatColor.valueOf(chatColor.toUpperCase());
            plugin.getRankManager().createRank(name, ChatColor.translateAlternateColorCodes('&', prefix), nameColorEnum, chatColorEnum);
            sender.sendMessage(ChatColor.GREEN + "Rank " + name + " created successfully.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid color code. Use color names like RED, BLUE, etc.");
        }
    }

    private void deleteRank(CommandSender sender, String name) {
        if (plugin.getRankManager().getRank(name) == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + name + " does not exist.");
            return;
        }
        plugin.getRankManager().deleteRank(name);
        sender.sendMessage(ChatColor.GREEN + "Rank " + name + " deleted successfully.");
    }

    private void setPlayerRank(CommandSender sender, String playerName, String rankName) {
        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        if (plugin.getRankManager().getRank(rankName) == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + rankName + " does not exist.");
            return;
        }
        plugin.getRankManager().setPlayerRank(target.getUniqueId(), rankName);
        sender.sendMessage(ChatColor.GREEN + "Set " + playerName + "'s rank to " + rankName);
        target.sendMessage(ChatColor.GREEN + "Your rank has been set to " + rankName);
    }

    private void addPermission(CommandSender sender, String rankName, String permission) {
        if (plugin.getRankManager().getRank(rankName) == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + rankName + " does not exist.");
            return;
        }
        plugin.getRankManager().addPermission(rankName, permission);
        sender.sendMessage(ChatColor.GREEN + "Added permission " + permission + " to rank " + rankName);
    }

    private void removePermission(CommandSender sender, String rankName, String permission) {
        if (plugin.getRankManager().getRank(rankName) == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + rankName + " does not exist.");
            return;
        }
        plugin.getRankManager().removePermission(rankName, permission);
        sender.sendMessage(ChatColor.GREEN + "Removed permission " + permission + " from rank " + rankName);
    }

    private void setDefaultRank(CommandSender sender, String rankName) {
        if (plugin.getRankManager().getRank(rankName) == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + rankName + " does not exist.");
            return;
        }
        plugin.getRankManager().setDefaultRank(rankName);
        sender.sendMessage(ChatColor.GREEN + "Set default rank to " + rankName);
    }

    private void listRanks(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Available ranks:");
        for (Rank rank : plugin.getRankManager().getAllRanks()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + rank.getName() + ": " + rank.getPrefix());
        }
    }

    private void listRankPermissions(CommandSender sender, String rankName) {
        Rank rank = plugin.getRankManager().getRank(rankName);
        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank " + rankName + " does not exist.");
            return;
        }
        sender.sendMessage(ChatColor.YELLOW + "Permissions for rank " + rankName + ":");
        for (String permission : rank.getPermissions()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + permission);
        }
    }

    private void setRankColors(CommandSender sender, String rankName, String nameColorStr, String chatColorStr) {
        try {
            ChatColor nameColor = ChatColor.valueOf(nameColorStr.toUpperCase());
            ChatColor chatColor = ChatColor.valueOf(chatColorStr.toUpperCase());
            plugin.getRankManager().updateRankColors(rankName, nameColor, chatColor);
            sender.sendMessage(ChatColor.GREEN + "Updated colors for rank " + rankName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid color name. Use color names like RED, BLUE, etc.");
        }
    }

    private void setRankPrefix(CommandSender sender, String rankName, String newPrefix) {
        plugin.getRankManager().updateRankPrefix(rankName, newPrefix);
        sender.sendMessage(ChatColor.GREEN + "Updated prefix for rank " + rankName);
    }
}