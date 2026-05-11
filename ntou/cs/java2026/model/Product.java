package ntou.cs.java2026.model;

/**
 * 商品資料模型
 * 儲存從各平台爬取到的商品資訊
 */
public class Product {

    private String name;        // 商品名稱
    private double price;       // 目前價格
    private String platform;    // 平台名稱（博客來 / PChome / momo）
    private String url;         // 商品網址
    private String imageUrl;    // 商品圖片網址

    // ── 建構子 ──────────────────────────────────────────────

    public Product() {}

    public Product(String name, double price, String platform, String url) {
        this.name = name;
        this.price = price;
        this.platform = platform;
        this.url = url;
    }

    // ── Getter / Setter ──────────────────────────────────────

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public double getPrice()            { return price; }
    public void setPrice(double price)  { this.price = price; }

    public String getPlatform()                  { return platform; }
    public void setPlatform(String platform)     { this.platform = platform; }

    public String getUrl()              { return url; }
    public void setUrl(String url)      { this.url = url; }

    public String getImageUrl()                  { return imageUrl; }
    public void setImageUrl(String imageUrl)     { this.imageUrl = imageUrl; }

    // ── 顯示用 ───────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] %s - NT$%.0f", platform, name, price);
    }
}