package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * PChome 爬蟲
 * 使用 PChome 公開 JSON API 取得商品資料
 */
public class PChomeCrawler extends BaseCrawler {

    private static final String API_URL =
        "https://ecshweb.pchome.com.tw/search/v3.3/all/results?q=%s&page=1&sort=sale/dc";

    @Override
    public String getPlatformName() {
        return "PChome";
    }

    @Override
    public List<Product> search(String keyword) {
        List<Product> results = new ArrayList<>();

        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String apiUrl = String.format(API_URL, encodedKeyword);

            // 建立 HTTP 連線
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 讀取回傳的 JSON 字串
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8")
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 用 Gson 解析 JSON
            JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonArray prods = root.getAsJsonArray("prods");
            if (prods == null) return results;

            for (int i = 0; i < prods.size() && results.size() < MAX_RESULTS; i++) {
                try {
                    JsonObject prod = prods.get(i).getAsJsonObject();

                    String name = prod.get("name").getAsString();
                    double price = prod.get("price").getAsDouble();
                    String prodId = prod.get("Id").getAsString();
                    String productUrl = "https://24h.pchome.com.tw/prod/" + prodId;

                    // 圖片網址
                    String picB = prod.has("picB") ? prod.get("picB").getAsString() : "";
                    String imageUrl = picB.isEmpty() ? "" :
                        "https://a.ecimg.tw" + picB;

                    Product product = new Product(name, price, getPlatformName(), productUrl);
                    product.setImageUrl(imageUrl);
                    results.add(product);

                } catch (Exception e) {
                    System.err.println("解析PChome商品失敗: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("PChome爬蟲錯誤: " + e.getMessage());
        }

        return results;
    }
}