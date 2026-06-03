package ntou.cs.java2026.manager;

import ntou.cs.java2026.db.DatabaseManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private List<String> searchHistory;
    private int userId;

    public HistoryManager() {
        this(0);
    }

    public HistoryManager(int userId) {
        this.userId = userId;
        searchHistory = new ArrayList<>();
        loadSearchHistory();
    }

    public void addHistory(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        if (!searchHistory.contains(keyword)) {

            searchHistory.add(keyword);

            saveSearchHistory();
        }
    }

    public void showSearchHistory() {

        if (searchHistory.isEmpty()) {

            System.out.println("目前沒有搜尋歷史紀錄。");
            return;
        }

        System.out.println("=== 搜尋歷史紀錄 ===");

        for (int i = 0; i < searchHistory.size(); i++) {

            System.out.println(
                    (i + 1) + ". " + searchHistory.get(i)
            );
        }
    }

    public void removeSearchHistory(java.util.Scanner scanner) {

        if (searchHistory.isEmpty()) {

            System.out.println("目前沒有搜尋歷史可以刪除。");
            return;
        }

        showSearchHistory();

        System.out.print("請輸入要刪除的搜尋歷史編號，輸入 0 取消：");

        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == 0) {

            System.out.println("已取消刪除。");
            return;
        }

        if (index < 1 || index > searchHistory.size()) {

            System.out.println("搜尋歷史編號錯誤。");
            return;
        }

        String removedKeyword = searchHistory.remove(index - 1);

        saveSearchHistory();

        System.out.println("已刪除搜尋歷史：" + removedKeyword);
    }

    public void saveSearchHistory() {

        DatabaseManager.saveHistory(userId, searchHistory);

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(getFallbackFileName())
                        )
        ) {

            for (String keyword : searchHistory) {

                writer.write(keyword);

                writer.newLine();
            }

        } catch (IOException e) {

            System.out.println(
                    "儲存搜尋歷史失敗："
                            + e.getMessage()
            );
        }
    }

    public void loadSearchHistory() {

        if (DatabaseManager.isAvailable()) {
            searchHistory.addAll(DatabaseManager.loadHistory(userId));
            if (!searchHistory.isEmpty()) {
                System.out.println("已從 SQLite 載入 " + searchHistory.size() + " 筆搜尋歷史紀錄。");
            }
            return;
        }

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(getFallbackFileName())
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {

                    searchHistory.add(line);
                }
            }

            if (!searchHistory.isEmpty()) {

                System.out.println(
                        "已載入 "
                                + searchHistory.size()
                                + " 筆搜尋歷史紀錄。"
                );
            }

        } catch (IOException e) {

            // 第一次執行通常還沒有檔案
        }
    }

    private String getFallbackFileName() {
        return userId > 0 ? "search_history_user_" + userId + ".txt" : "search_history.txt";
    }

    public List<String> getSearchHistory() {

        return searchHistory;
    }
}
