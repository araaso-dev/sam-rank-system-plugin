package com.sam.commands;

import com.sam.Main;
import com.sam.manager.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {

    private Main plugin;

    public MessageCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /message <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        Player senderPlayer = (Player) sender;
        Rank senderRank = plugin.getRankManager().getPlayerRank(senderPlayer.getUniqueId());
        Rank targetRank = plugin.getRankManager().getPlayerRank(target.getUniqueId());

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        // Format for sender
        String senderFormat = ChatColor.GRAY + "[" + ChatColor.WHITE + "me" + ChatColor.GRAY + " -> " + targetRank.getPrefix() + " " + targetRank.getNameColor() + target.getName() + ChatColor.GRAY + "]: " + ChatColor.WHITE + message;
        senderPlayer.sendMessage(senderFormat);

        // Format for recipient
        String recipientFormat = ChatColor.GRAY + "[" + senderRank.getPrefix() + " " + senderRank.getNameColor() + senderPlayer.getName() +
                ChatColor.GRAY + " -> " + ChatColor.WHITE + "me" + ChatColor.GRAY + "]: " + ChatColor.WHITE + message;
        target.sendMessage(recipientFormat);

        return true;
    }
}