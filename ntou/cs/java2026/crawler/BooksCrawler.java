package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import ntou.cs.java2026.util.ProductMatcher;

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
                    Element nameEl = item.selectFirst("h4 a");
                    if (nameEl == null) continue;
                    String name = nameEl.text().trim();
                    if (!ProductMatcher.isRelevant(name, keyword)) continue;

                    String productUrl = nameEl.attr("abs:href");

                    Elements priceEls = item.select("ul.price li b");
                    if (priceEls.size() < 2) continue;
                    double price = parsePrice(priceEls.get(1).text());
                    if (price <= 0) continue;

                    // 抓圖片，優先用 data-src，過濾 base64 佔位圖
                    Element imgEl = item.selectFirst("img");
                    String imageUrl = "";
                    if (imgEl != null) {
                        String dataSrc = imgEl.attr("data-src");
                        String src = imgEl.attr("abs:src");
                        imageUrl = !dataSrc.isEmpty() ? dataSrc : src;
                        if (imageUrl.startsWith("data:")) imageUrl = "";
                        // 要求回傳 JPG 格式，避免 WebP
                        if (!imageUrl.isEmpty() && imageUrl.contains("book.com.tw")) {
                            imageUrl = imageUrl + "&type=jpg";
                        }
                    }

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