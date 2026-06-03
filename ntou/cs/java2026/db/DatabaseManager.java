package ntou.cs.java2026.db;

import ntou.cs.java2026.model.Product;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 資料庫管理器。
 *
 * 本版本新增 users / user_favorites / user_search_history，
 * 讓不同登入使用者的收藏與搜尋歷史可以分開保存。
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:shopping_tracker.db";
    private static Boolean available = null;

    public static boolean isAvailable() {
        if (available != null) return available;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                createTables(conn);
            }
            available = true;
        } catch (Exception e) {
            available = false;
            System.out.println("SQLite 無法使用，將改用 CSV/TXT 備援儲存：" + e.getMessage());
        }
        return available;
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTables(Connection conn) throws SQLException {
        String usersSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String userFavoritesSql = "CREATE TABLE IF NOT EXISTS user_favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "platform TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "url TEXT NOT NULL," +
                "image_url TEXT," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(user_id, url)" +
                ")";

        String userHistorySql = "CREATE TABLE IF NOT EXISTS user_search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "keyword TEXT NOT NULL," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(user_id, keyword)" +
                ")";

        // 保留舊表，避免舊版資料庫存在時發生錯誤；新版 GUI 不再共用舊表。
        String legacyFavoritesSql = "CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "platform TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "url TEXT NOT NULL UNIQUE," +
                "image_url TEXT," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String legacyHistorySql = "CREATE TABLE IF NOT EXISTS search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "keyword TEXT NOT NULL UNIQUE," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(userFavoritesSql);
            stmt.execute(userHistorySql);
            stmt.execute(legacyFavoritesSql);
            stmt.execute(legacyHistorySql);
        }
    }

    public static int registerUser(String username, String password) {
        if (!isAvailable()) return -1;
        username = safe(username).trim();
        if (username.isEmpty() || password == null || password.isEmpty()) return -1;

        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("註冊失敗：" + e.getMessage());
        }
        return -1;
    }

    public static int loginUser(String username, String password) {
        if (!isAvailable()) return -1;
        username = safe(username).trim();
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("登入失敗：" + e.getMessage());
        }
        return -1;
    }

    public static boolean usernameExists(String username) {
        if (!isAvailable()) return false;
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safe(username).trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static List<Product> loadFavorites(int userId) {
        List<Product> list = new ArrayList<>();
        if (!isAvailable()) return list;

        String sql = "SELECT platform, name, price, url, image_url FROM user_favorites WHERE user_id = ? ORDER BY id";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Product(
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("platform"),
                            rs.getString("url"),
                            rs.getString("image_url")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("讀取 SQLite 使用者收藏失敗：" + e.getMessage());
        }
        return list;
    }

    public static void saveFavorites(int userId, List<Product> products) {
        if (!isAvailable()) return;
        String deleteSql = "DELETE FROM user_favorites WHERE user_id = ?";
        String insertSql = "INSERT OR REPLACE INTO user_favorites(user_id, platform, name, price, url, image_url) VALUES(?,?,?,?,?,?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delete = conn.prepareStatement(deleteSql)) {
                delete.setInt(1, userId);
                delete.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Product p : products) {
                    ps.setInt(1, userId);
                    ps.setString(2, safe(p.getPlatform()));
                    ps.setString(3, safe(p.getName()));
                    ps.setDouble(4, p.getPrice());
                    ps.setString(5, safe(p.getUrl()));
                    ps.setString(6, safe(p.getImageUrl()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("儲存 SQLite 使用者收藏失敗：" + e.getMessage());
        }
    }

    public static List<String> loadHistory(int userId) {
        List<String> list = new ArrayList<>();
        if (!isAvailable()) return list;

        String sql = "SELECT keyword FROM user_search_history WHERE user_id = ? ORDER BY id";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("keyword"));
            }
        } catch (SQLException e) {
            System.out.println("讀取 SQLite 使用者搜尋歷史失敗：" + e.getMessage());
        }
        return list;
    }

    public static void saveHistory(int userId, List<String> history) {
        if (!isAvailable()) return;
        String deleteSql = "DELETE FROM user_search_history WHERE user_id = ?";
        String insertSql = "INSERT OR IGNORE INTO user_search_history(user_id, keyword) VALUES(?, ?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delete = conn.prepareStatement(deleteSql)) {
                delete.setInt(1, userId);
                delete.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String keyword : history) {
                    ps.setInt(1, userId);
                    ps.setString(2, safe(keyword));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("儲存 SQLite 使用者搜尋歷史失敗：" + e.getMessage());
        }
    }

    // 舊 CLI / 舊程式碼相容用：使用 user_id = 0 當作共用帳號。
    public static List<Product> loadFavorites() { return loadFavorites(0); }
    public static void saveFavorites(List<Product> products) { saveFavorites(0, products); }
    public static List<String> loadHistory() { return loadHistory(0); }
    public static void saveHistory(List<String> history) { saveHistory(0, history); }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
