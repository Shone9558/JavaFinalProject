package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import java.util.List;

/**
 * 所有爬蟲的抽象父類
 * 每個平台的爬蟲都必須繼承這個類別並實作 search() 方法
 */
public abstract class BaseCrawler {

    // 每次搜尋最多抓幾筆商品
    protected static final int MAX_RESULTS = 10;

    // 模擬瀏覽器的 User-Agent（避免被網站擋掉）
    protected static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36";

    /**
     * 搜尋商品（每個子類別必須自己實作）
     * @param keyword 搜尋關鍵字
     * @return 商品清單
     */
    public abstract List<Product> search(String keyword);

    /**
     * 取得平台名稱（每個子類別必須自己實作）
     */
    public abstract String getPlatformName();

    /**
     * 把價格字串轉成數字（共用工具方法）
     * 例如 "NT$1,299" → 1299.0
     */
    protected double parsePrice(String priceText) {
        try {
            // 移除所有非數字字元（除了小數點）
            String cleaned = priceText.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}