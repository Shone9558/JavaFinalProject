package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import ntou.cs.java2026.util.ProductMatcher;

/**
 * 博客來爬蟲
 * 使用 jsoup 解析靜態 HTML 頁面
 */
public class BooksCrawler extends BaseCrawler {

    private static final String BASE_URL =
        "https://search.books.com.tw/search/query/key/%s/cat/all";

    @Override
    public String getPlatformName() {
        return "博客來";
    }

    @Override
    public List<Product> search(String keyword) {
        List<Product> results = new ArrayList<>();

        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = String.format(BASE_URL, encodedKeyword);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            Elements items = doc.select("div.table-td[id^=prod-itemlist]");

            for (Element item : items) {
                if (results.size() >= MAX_RESULTS) break;

                try {
                    // 抓商品名稱
                    Element nameEl = item.selectFirst("h4 a");
                    if (nameEl == null) continue;
                    String name = nameEl.text().trim();
                    if (!ProductMatcher.isRelevant(name, keyword)) {
                        continue;
                    }

                    // 抓商品網址
                    String productUrl = nameEl.attr("abs:href");

                    // 抓價格（第二個 <b> 才是實際價格，第一個是折扣）
                    Elements priceEls = item.select("ul.price li b");
                    if (priceEls.size() < 2) continue;
                    double price = parsePrice(priceEls.get(1).text());
                    if (price <= 0) continue;

                    // 抓圖片
                    Element imgEl = item.selectFirst("img");
                    String imageUrl = imgEl != null ? imgEl.attr("abs:src") : "";

                    // 建立商品物件
                    Product product = new Product(name, price, getPlatformName(), productUrl);
                    product.setImageUrl(imageUrl);
                    results.add(product);

                } catch (Exception e) {
                    System.err.println("解析商品失敗: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("博客來爬蟲錯誤: " + e.getMessage());
        }

        return results;
    }
}