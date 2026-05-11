package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * momo 爬蟲
 * 從頁面內嵌的 JSON-LD 結構化資料取得商品資訊
 */
public class MomoCrawler extends BaseCrawler {

    private static final String BASE_URL =
        "https://www.momoshop.com.tw/search/searchShop.jsp?keyword=%s&searchType=1&curPage=1";

    @Override
    public String getPlatformName() {
        return "momo";
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

            // 找頁面裡的 JSON-LD script 標籤
            Element jsonLd = doc.selectFirst("script[type=application/ld+json]");
            if (jsonLd == null) return results;

            // 解析 JSON
            JsonObject root = JsonParser.parseString(jsonLd.html()).getAsJsonObject();
            JsonArray graph = root.getAsJsonArray("@graph");
            if (graph == null) return results;

            // 找 ItemList 區塊
            for (int i = 0; i < graph.size(); i++) {
                JsonObject node = graph.get(i).getAsJsonObject();
                if (!node.get("@type").getAsString().equals("ItemList")) continue;

                JsonArray items = node.getAsJsonArray("itemListElement");
                if (items == null) break;

                for (int j = 0; j < items.size() && results.size() < MAX_RESULTS; j++) {
                    try {
                        JsonObject item = items.get(j).getAsJsonObject();

                        String name = item.get("name").getAsString();
                        String productUrl = item.get("url").getAsString();
                        String imageUrl = item.has("image") ? item.get("image").getAsString() : "";

                        JsonObject offers = item.getAsJsonObject("offers");
                        if (offers == null) continue;
                        double price = offers.get("price").getAsDouble();
                        if (price <= 0) continue;

                        Product product = new Product(name, price, getPlatformName(), productUrl);
                        product.setImageUrl(imageUrl);
                        results.add(product);

                    } catch (Exception e) {
                        System.err.println("解析momo商品失敗: " + e.getMessage());
                    }
                }
                break;
            }

        } catch (Exception e) {
            System.err.println("momo爬蟲錯誤: " + e.getMessage());
        }

        return results;
    }
}