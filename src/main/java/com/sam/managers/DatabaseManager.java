package com.sam.managers;

import com.sam.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private String url;
    private String user;
    private String password;
    private final Main plugin;
    private final String DEFAULT_RANK_NAME = "rookie";

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        loadDatabaseCredentials();
        loadDatabaseDriver();
    }

    private void loadDatabaseCredentials() {
        File configFile = new File(plugin.getDataFolder(), "database.yml");
        if (!configFile.exists()) {
            plugin.saveResource("database.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 5432);
        String dbName = config.getString("database.name", "your_database_name");
        this.user = config.getString("database.user", "your_username");
        this.password = config.getString("database.password", "your_password");

        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    private void loadDatabaseDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // Create ranks table
            stmt.execute("CREATE TABLE IF NOT EXISTS ranks (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(50) UNIQUE NOT NULL, " +
                    "prefix VARCHAR(50) NOT NULL, " +
                    "name_color VARCHAR(20) NOT NULL, " +
                    "chat_color VARCHAR(20) NOT NULL)");

            // Create players table
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "id SERIAL PRIMARY KEY, " +
                    "uuid UUID UNIQUE NOT NULL, " +
                    "rank_id INTEGER REFERENCES ranks(id))");

            // Create rank_permissions table
            stmt.execute("CREATE TABLE IF NOT EXISTS rank_permissions (" +
                    "id SERIAL PRIMARY KEY, " +
                    "rank_id INTEGER REFERENCES ranks(id), " +
                    "permission VARCHAR(100) NOT NULL, " +
                    "UNIQUE (rank_id, permission))");

            // Ensure default rank exists
            String checkDefaultRank = "SELECT COUNT(*) FROM ranks WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkDefaultRank)) {
                pstmt.setString(1, DEFAULT_RANK_NAME);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Default rank doesn't exist, create it
                    String createDefaultRank = "INSERT INTO ranks (name, prefix, name_color, chat_color) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(createDefaultRank)) {
                        insertStmt.setString(1, DEFAULT_RANK_NAME);
                        insertStmt.setString(2, "&7[" + DEFAULT_RANK_NAME + "]");
                        insertStmt.setString(3, "WHITE");
                        insertStmt.setString(4, "GRAY");
                        insertStmt.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer(Player player) {
        String sql = "INSERT INTO players (uuid, rank_id) VALUES (?, ?) ON CONFLICT (uuid) DO NOTHING";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, player.getUniqueId());
            pstmt.setInt(2, getDefaultRankId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getDefaultRankId() {
        String sql = "SELECT id FROM ranks WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plugin.getRankManager().getDefaultRank());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Default rank not found
    }

    public void setPlayerRank(UUID playerUUID, String rankName) {
        String sql = "UPDATE players SET rank_id = (SELECT id FROM ranks WHERE name = ?) WHERE uuid = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rankName);
            pstmt.setObject(2, playerUUID);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createRank(String name, String prefix, ChatColor nameColor, ChatColor chatColor) {
        String sql = "INSERT INTO ranks (name, prefix, name_color, chat_color) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, prefix);
            pstmt.setString(3, nameColor.name());
            pstmt.setString(4, chatColor.name());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRank(String name) {
        String sql = "DELETE FROM ranks WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPermission(String rankName, String permission) {
        String sql = "INSERT INTO rank_permissions (rank_id, permission) " +
                "VALUES ((SELECT id FROM ranks WHERE name = ?), ?) " +
                "ON CONFLICT (rank_id, permission) DO NOTHING";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rankName);
            pstmt.setString(2, permission);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePermission(String rankName, String permission) {
        String sql = "DELETE FROM rank_permissions WHERE rank_id = (SELECT id FROM ranks WHERE name = ?) AND permission = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rankName);
            pstmt.setString(2, permission);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRankPermissions(String rankName) {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT permission FROM rank_permissions rp " +
                "JOIN ranks r ON rp.rank_id = r.id " +
                "WHERE r.name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rankName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString("permission"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public Rank getRank(String rankName) {
        String sql = "SELECT * FROM ranks WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rankName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String prefix = rs.getString("prefix");
                ChatColor nameColor = ChatColor.valueOf(rs.getString("name_color"));
                ChatColor chatColor = ChatColor.valueOf(rs.getString("chat_color"));
                List<String> permissions = getRankPermissions(name);
                return new Rank(name, prefix, nameColor, chatColor, permissions);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Rank> getAllRanks() {
        List<Rank> ranks = new ArrayList<>();
        String sql = "SELECT * FROM ranks";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String prefix = rs.getString("prefix");
                ChatColor nameColor = ChatColor.valueOf(rs.getString("name_color"));
                ChatColor chatColor = ChatColor.valueOf(rs.getString("chat_color"));
                List<String> permissions = getRankPermissions(name);
                ranks.add(new Rank(name, prefix, nameColor, chatColor, permissions));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranks;
    }

    public void updateRankColors(String rankName, ChatColor nameColor, ChatColor chatColor) {
        String sql = "UPDATE ranks SET name_color = ?, chat_color = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameColor.name());
            pstmt.setString(2, chatColor.name());
            pstmt.setString(3, rankName);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating rank colors failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRankPrefix(String rankName, String newPrefix) {
        String sql = "UPDATE ranks SET prefix = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPrefix);
            pstmt.setString(2, rankName);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating rank prefix failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rank getPlayerRank(UUID playerUUID) {
        String sql = "SELECT r.* FROM ranks r " +
                "JOIN players p ON r.id = p.rank_id " +
                "WHERE p.uuid = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String prefix = rs.getString("prefix");
                ChatColor nameColor = ChatColor.valueOf(rs.getString("name_color"));
                ChatColor chatColor = ChatColor.valueOf(rs.getString("chat_color"));
                List<String> permissions = getRankPermissions(name);
                return new Rank(name, prefix, nameColor, chatColor, permissions);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}