package ntou.cs.java2026.db;

import ntou.cs.java2026.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 資料庫管理器。
 *
 * 注意：Java 本身沒有內建 SQLite JDBC Driver。
 * 如果專案資料夾有 sqlite-jdbc.jar 並且執行時有加入 classpath，
 * 這個類別就會使用 shopping_tracker.db 儲存收藏與搜尋歷史。
 * 如果沒有 sqlite-jdbc.jar，程式會自動退回原本的 CSV / TXT 儲存方式。
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:shopping_tracker.db";
    private static Boolean available = null;

    public static boolean isAvailable() {
        if (available != null) {
            return available;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                createTables(conn);
            }
            available = true;
        } catch (Exception e) {
            available = false;
        }
        return available;
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTables(Connection conn) throws SQLException {
        String favoritesSql = "CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "platform TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "url TEXT NOT NULL UNIQUE," +
                "image_url TEXT," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String historySql = "CREATE TABLE IF NOT EXISTS search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "keyword TEXT NOT NULL UNIQUE," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(favoritesSql);
            stmt.execute(historySql);
        }
    }

    public static List<Product> loadFavorites() {
        List<Product> list = new ArrayList<>();
        if (!isAvailable()) return list;

        String sql = "SELECT platform, name, price, url, image_url FROM favorites ORDER BY id";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("platform"),
                        rs.getString("url"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            System.out.println("讀取 SQLite 收藏失敗：" + e.getMessage());
        }
        return list;
    }

    public static void saveFavorites(List<Product> products) {
        if (!isAvailable()) return;
        String deleteSql = "DELETE FROM favorites";
        String insertSql = "INSERT OR REPLACE INTO favorites(platform, name, price, url, image_url) VALUES(?,?,?,?,?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(deleteSql);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Product p : products) {
                    ps.setString(1, safe(p.getPlatform()));
                    ps.setString(2, safe(p.getName()));
                    ps.setDouble(3, p.getPrice());
                    ps.setString(4, safe(p.getUrl()));
                    ps.setString(5, safe(p.getImageUrl()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("儲存 SQLite 收藏失敗：" + e.getMessage());
        }
    }

    public static List<String> loadHistory() {
        List<String> list = new ArrayList<>();
        if (!isAvailable()) return list;

        String sql = "SELECT keyword FROM search_history ORDER BY id";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("keyword"));
            }
        } catch (SQLException e) {
            System.out.println("讀取 SQLite 搜尋歷史失敗：" + e.getMessage());
        }
        return list;
    }

    public static void saveHistory(List<String> history) {
        if (!isAvailable()) return;
        String deleteSql = "DELETE FROM search_history";
        String insertSql = "INSERT OR IGNORE INTO search_history(keyword) VALUES(?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(deleteSql);
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String keyword : history) {
                    ps.setString(1, safe(keyword));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("儲存 SQLite 搜尋歷史失敗：" + e.getMessage());
        }
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
