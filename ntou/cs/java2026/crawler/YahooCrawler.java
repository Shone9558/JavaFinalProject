package ntou.cs.java2026.crawler;

import ntou.cs.java2026.model.Product;
import ntou.cs.java2026.util.ProductMatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class YahooCrawler extends BaseCrawler {

    private static final String BASE_URL =
        "https://tw.buy.yahoo.com/search/product?p=%s&first=1";

    @Override
    public String getPlatformName() {
        return "Yahoo購物";
    }

    @Override
    public List<Product> search(String keyword) {
        List<Product> results = new ArrayList<>();

        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = String.format(BASE_URL, encodedKeyword);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-TW,zh;q=0.9")
                    .header("Referer", "https://tw.buy.yahoo.com/")
                    .timeout(10000)
                    .get();

            Elements items = doc.select("ul.gridList li:has(img)");

            for (Element item : items) {
                if (results.size() >= MAX_RESULTS) break;

                try {
                    Element linkEl = item.selectFirst("a");
                    if (linkEl == null) continue;
                    String productUrl = linkEl.attr("abs:href");

                    Element imgEl = item.selectFirst("img");
                    if (imgEl == null) continue;
                    String name = imgEl.attr("alt").trim();
                    if (name.isEmpty()) continue;
                    if (!ProductMatcher.isRelevant(name, keyword)) continue;

                    // 從商品頁面抓圖片
                    String imageUrl = "";
                    try {
                        Document productDoc = Jsoup.connect(productUrl)
                                .userAgent(USER_AGENT)
                                .timeout(5000)
                                .get();

                        // 先找 MipImages
                        Element productImg = productDoc.selectFirst("img[src*=MipImages]");

                        // 找不到再找其他 yec.tw 圖片，過濾廣告圖
                        if (productImg == null) {
                            Elements imgs = productDoc.select("img[src*=yec.tw]");
                            for (Element img : imgs) {
                                String src = img.attr("src");
                                if (!src.contains("no-image") && !src.contains(".svg")
                                    && !src.contains("marketingInfo") && !src.contains("banner")
                                    && !src.contains("icon") && !src.contains("logo")) {
                                    productImg = img;
                                    break;
                                }
                            }
                        }

                        if (productImg != null) {
                            imageUrl = productImg.attr("abs:src");
                        }
                    } catch (Exception e) {
                        // 抓不到就算了
                    }

                    // 先找含逗號的價格格式
                    double price = 0;
                    Elements spans = item.select("span");
                    for (Element span : spans) {
                        String text = span.ownText().trim();
                        if (text.matches("^[0-9,]+$") && text.contains(",")) {
                            try {
                                price = Double.parseDouble(text.replace(",", ""));
                                if (price > 0) break;
                            } catch (NumberFormatException e) {
                                // 繼續找
                            }
                        }
                    }

                    // 沒找到含逗號的，再找純數字
                    if (price <= 0) {
                        for (Element span : spans) {
                            String text = span.ownText().trim();
                            if (text.matches("^[0-9]+$") && text.length() >= 3 && text.length() <= 6) {
                                try {
                                    price = Double.parseDouble(text);
                                    if (price >= 100) break;
                                } catch (NumberFormatException e) {
                                    // 繼續找
                                }
                            }
                        }
                    }

                    if (price <= 0) continue;

                    Product product = new Product(name, price, getPlatformName(), productUrl);
                    product.setImageUrl(imageUrl);
                    results.add(product);

                } catch (Exception e) {
                    System.err.println("解析Yahoo商品失敗: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Yahoo爬蟲錯誤: " + e.getMessage());
        }

        return results;
    }
}