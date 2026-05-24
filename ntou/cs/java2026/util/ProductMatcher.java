package ntou.cs.java2026.util;

public class ProductMatcher {

    private static final String[] ACCESSORY_WORDS = {
            "殼", "套", "貼", "膜",
            "架", "線", "掛繩", "吊繩",
            "收納", "保護", "充電",
            "支架", "包", "配件",
            "背帶", "肩背帶", "夾片",
            "掛片", "腕帶", "扣環",
            "掛鉤", "頸掛", "手機鍊",
            "保護殼", "保護套",
            "玻璃貼", "螢幕貼", "鏡頭貼",
            "傳輸線", "充電線", "充電器",
            "行動電源", "耳機", "轉接頭"
    };

    public static boolean isRelevant(String productName, String keyword) {
        if (productName == null || keyword == null) {
            return false;
        }

        String name = normalize(productName);
        String key = normalize(keyword);

        if (key.isEmpty()) {
            return false;
        }

        if (name.contains(key)) {
            if (isAccessory(name) && !isAccessory(key)) {
                return false;
            }

            return true;
        }

        String[] tokens = key.split("\\s+");
        int matchCount = 0;

        for (String token : tokens) {
            if (!token.isEmpty() && name.contains(token)) {
                matchCount++;
            }
        }

        if (matchCount == 0) {
            return false;
        }

        if (isAccessory(name) && !isAccessory(key)) {
            return false;
        }

        return matchCount >= Math.ceil(tokens.length * 0.6);
    }

    public static boolean isAccessory(String text) {
        String normalized = normalize(text);

        for (String word : ACCESSORY_WORDS) {
            if (normalized.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[\\p{Punct}\\s]+", " ")
                .trim();
    }
}