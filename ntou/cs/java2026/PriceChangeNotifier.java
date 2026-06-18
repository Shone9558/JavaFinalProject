package ntou.cs.java2026;

import ntou.cs.java2026.model.Product;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class PriceChangeNotifier {

    private static final double PRICE_EPSILON = 1.0;
    private static final Map<String, Double> priceHistory = new HashMap<>();
    private static int loadedUserId = Integer.MIN_VALUE;
    private static TrayIcon trayIcon;

    private PriceChangeNotifier() {
    }

    public static synchronized boolean checkAndNotify(Product product, int userId) {
        if (!isValidProduct(product)) return false;

        loadHistory(userId);

        String key = makeKey(product);
        double currentPrice = product.getPrice();
        Double oldPrice = priceHistory.get(key);

        boolean changed = oldPrice != null && isPriceChanged(oldPrice, currentPrice);
        if (changed) {
            showPriceChangeNotification(product, oldPrice, currentPrice);
        }

        priceHistory.put(key, currentPrice);
        saveHistory(userId);
        return changed;
    }

    public static synchronized void rememberPrice(Product product, int userId) {
        if (!isValidProduct(product)) return;
        loadHistory(userId);
        priceHistory.put(makeKey(product), product.getPrice());
        saveHistory(userId);
    }

    public static synchronized boolean notifyDirectChange(Product product, double oldPrice, double newPrice, int userId) {
        if (product == null || !isPriceChanged(oldPrice, newPrice)) return false;
        showPriceChangeNotification(product, oldPrice, newPrice);

        loadHistory(userId);
        priceHistory.put(makeKey(product), newPrice);
        saveHistory(userId);
        return true;
    }

    public static boolean isPriceChanged(double oldPrice, double newPrice) {
        if (oldPrice <= 0 || newPrice <= 0) return false;
        return Math.abs(oldPrice - newPrice) >= PRICE_EPSILON;
    }

    private static boolean isValidProduct(Product product) {
        return product != null
                && product.getName() != null
                && !product.getName().trim().isEmpty()
                && product.getPrice() > 0;
    }

    private static void loadHistory(int userId) {
        if (loadedUserId == userId) return;

        priceHistory.clear();
        loadedUserId = userId;

        Path path = getHistoryPath(userId);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length != 2) continue;

                try {
                    priceHistory.put(parts[0], Double.parseDouble(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveHistory(int userId) {
        Path path = getHistoryPath(userId);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Double> entry : priceHistory.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getHistoryPath(int userId) {
        return Paths.get("price_history_user_" + userId + ".csv");
    }

    private static String makeKey(Product product) {
        String platform = clean(product.getPlatform());
        String url = clean(product.getUrl());
        String name = clean(product.getName());

        String identity;
        if (!url.isEmpty()) {
            identity = platform + "\u001Furl:" + url;
        } else {
            identity = platform + "\u001Fname:" + name;
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(identity.getBytes(StandardCharsets.UTF_8));
    }

    private static String clean(String text) {
        if (text == null) return "";
        return text.trim().replace("\r", " ").replace("\n", " ");
    }

    private static void showPriceChangeNotification(Product product, double oldPrice, double newPrice) {
        String title = newPrice < oldPrice ? "商品降價提醒" : "商品漲價提醒";
        String message = product.getPlatform()
                + "｜" + shorten(product.getName(), 48)
                + "\n原價 " + formatPrice(oldPrice)
                + " → 現在 " + formatPrice(newPrice);

        showNotification(title, message, newPrice < oldPrice);
    }

    private static String formatPrice(double price) {
        return String.format("NT$ %,.0f", price);
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.length() <= maxLength) return trimmed;
        return trimmed.substring(0, maxLength) + "...";
    }

    private static void showNotification(String title, String message, boolean isPriceDown) {
        if (!SystemTray.isSupported()) {
            System.out.println(title + "：" + message);
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            if (trayIcon == null) {
                trayIcon = new TrayIcon(createTrayImage(isPriceDown), "智慧購物比價平台");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            } else {
                trayIcon.setImage(createTrayImage(isPriceDown));
            }

            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Image createTrayImage(boolean isPriceDown) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setColor(isPriceDown ? new Color(70, 150, 95) : new Color(190, 95, 65));
        g2.fillOval(1, 1, 14, 14);
        g2.setColor(Color.WHITE);
        g2.drawString("$", 5, 12);
        g2.dispose();

        return image;
    }
}
