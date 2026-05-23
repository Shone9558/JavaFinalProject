package ntou.cs.java2026;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static List<Product> allProducts = new ArrayList<>();
    private static List<Product> filteredProducts = new ArrayList<>();
    private static List<Product> platformFilteredProducts = new ArrayList<>();
    private static List<Product> favoriteProducts = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadFavoriteProducts();

        boolean running = true;

        while (running) {
            showMenu();

            System.out.print("請選擇功能：");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    searchProducts();
                    break;
                case 2:
                    showAllProducts();
                    break;
                case 3:
                    filterByPrice();
                    break;
                case 4:
                    filterByPlatform();
                    break;
                case 5:
                    addFavoriteProduct();
                    break;
                case 6:
                    showFavoriteProducts();
                    break;
                case 7:
                    removeFavoriteProduct();
                    break;
                case 8:
                    saveFavoriteProducts();
                    System.out.println("收藏商品已儲存。");
                    System.out.println("感謝使用智慧購物比價追蹤器！");
                    running = false;
                    break;
                default:
                    System.out.println("輸入錯誤，請重新選擇。");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void showMenu() {
        System.out.println("=== 智慧購物比價追蹤器 ===");
        System.out.println("1. 搜尋商品");
        System.out.println("2. 顯示目前搜尋結果");
        System.out.println("3. 價格篩選");
        System.out.println("4. 平台篩選");
        System.out.println("5. 收藏商品");
        System.out.println("6. 查看收藏商品");
        System.out.println("7. 刪除指定收藏商品");
        System.out.println("8. 離開");
    }

    private static void searchProducts() {
        System.out.print("請輸入搜尋關鍵字：");
        String keyword = scanner.nextLine();

        allProducts.clear();
        filteredProducts.clear();
        platformFilteredProducts.clear();

        System.out.println("\n正在搜尋博客來...");
        BooksCrawler books = new BooksCrawler();
        allProducts.addAll(books.search(keyword));

        System.out.println("正在搜尋 PChome...");
        PChomeCrawler pchome = new PChomeCrawler();
        allProducts.addAll(pchome.search(keyword));

        System.out.println("正在搜尋 momo...");
        MomoCrawler momo = new MomoCrawler();
        allProducts.addAll(momo.search(keyword));

        allProducts.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));

        if (allProducts.isEmpty()) {
            System.out.println("\n查無商品，請換一個關鍵字試試看。");
        } else {
            System.out.println("\n搜尋完成，共找到 " + allProducts.size() + " 筆商品。");
            System.out.println("商品已依價格由低到高排序。");
        }
    }

    private static void showAllProducts() {
        if (allProducts.isEmpty()) {
            System.out.println("目前沒有搜尋結果，請先搜尋商品。");
            return;
        }

        System.out.println("=== 目前搜尋結果：價格由低到高 ===");

        for (int i = 0; i < allProducts.size(); i++) {
            System.out.println("第 " + (i + 1) + " 筆");
            System.out.println(allProducts.get(i));
            System.out.println("--------------------------------");
        }
    }

    private static void filterByPrice() {
        if (allProducts.isEmpty()) {
            System.out.println("目前沒有搜尋結果，請先搜尋商品。");
            return;
        }

        System.out.print("請輸入最高預算，若不想限制請輸入 0：");
        double maxPrice = scanner.nextDouble();
        scanner.nextLine();

        filteredProducts.clear();

        for (Product product : allProducts) {
            if (maxPrice == 0 || product.getPrice() <= maxPrice) {
                filteredProducts.add(product);
            }
        }

        System.out.println("\n=== 價格篩選結果 ===");

        if (filteredProducts.isEmpty()) {
            System.out.println("沒有符合預算的商品。");
            return;
        }

        for (int i = 0; i < filteredProducts.size(); i++) {
            System.out.println("第 " + (i + 1) + " 筆");
            System.out.println(filteredProducts.get(i));
            System.out.println("--------------------------------");
        }

        System.out.println("共找到 " + filteredProducts.size() + " 筆符合預算的商品。");
    }

    private static void filterByPlatform() {
        if (allProducts.isEmpty()) {
            System.out.println("目前沒有搜尋結果，請先搜尋商品。");
            return;
        }

        System.out.println("請選擇平台篩選：");
        System.out.println("1. 博客來");
        System.out.println("2. PChome");
        System.out.println("3. momo");
        System.out.print("請輸入選項：");

        int choice = scanner.nextInt();
        scanner.nextLine();

        String selectedPlatform;

        switch (choice) {
            case 1:
                selectedPlatform = "博客來";
                break;
            case 2:
                selectedPlatform = "PChome";
                break;
            case 3:
                selectedPlatform = "momo";
                break;
            default:
                System.out.println("輸入錯誤，已取消平台篩選。");
                return;
        }

        platformFilteredProducts.clear();

        for (Product product : allProducts) {
            if (product.getPlatform().equals(selectedPlatform)) {
                platformFilteredProducts.add(product);
            }
        }

        if (platformFilteredProducts.isEmpty()) {
            System.out.println("該平台沒有搜尋到商品。");
            return;
        }

        System.out.println("=== " + selectedPlatform + " 商品結果 ===");

        for (int i = 0; i < platformFilteredProducts.size(); i++) {
            System.out.println("第 " + (i + 1) + " 筆");
            System.out.println(platformFilteredProducts.get(i));
            System.out.println("--------------------------------");
        }

        System.out.println("共找到 " + platformFilteredProducts.size() + " 筆 " + selectedPlatform + " 商品。");
    }

    private static void addFavoriteProduct() {
        if (allProducts.isEmpty()) {
            System.out.println("目前沒有搜尋結果，請先搜尋商品。");
            return;
        }

        System.out.println("請選擇收藏來源：");
        System.out.println("1. 從全部搜尋結果收藏");
        System.out.println("2. 從價格篩選結果收藏");
        System.out.println("3. 從平台篩選結果收藏");
        System.out.print("請選擇：");

        int sourceChoice = scanner.nextInt();
        scanner.nextLine();

        List<Product> sourceList;

        if (sourceChoice == 1) {
            sourceList = allProducts;
        } else if (sourceChoice == 2) {
            if (filteredProducts.isEmpty()) {
                System.out.println("目前沒有價格篩選結果，請先使用價格篩選功能。");
                return;
            }
            sourceList = filteredProducts;
        } else if (sourceChoice == 3) {
            if (platformFilteredProducts.isEmpty()) {
                System.out.println("目前沒有平台篩選結果，請先使用平台篩選功能。");
                return;
            }
            sourceList = platformFilteredProducts;
        } else {
            System.out.println("輸入錯誤，已取消收藏。");
            return;
        }

        List<Product> availableProducts = new ArrayList<>();

        for (Product product : sourceList) {
            if (!isAlreadyFavorite(product)) {
                availableProducts.add(product);
            }
        }

        if (availableProducts.isEmpty()) {
            System.out.println("此清單中的商品都已經收藏過了。");
            return;
        }

        System.out.println("\n=== 可收藏商品列表 ===");

        for (int i = 0; i < availableProducts.size(); i++) {
            System.out.println("第 " + (i + 1) + " 筆");
            System.out.println(availableProducts.get(i));
            System.out.println("--------------------------------");
        }

        System.out.print("請輸入要收藏的商品編號，輸入 0 取消：");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == 0) {
            System.out.println("已取消收藏。");
            return;
        }

        if (index < 1 || index > availableProducts.size()) {
            System.out.println("商品編號錯誤。");
            return;
        }

        Product selectedProduct = availableProducts.get(index - 1);

        favoriteProducts.add(selectedProduct);
        saveFavoriteProducts();

        System.out.println("已收藏商品：");
        System.out.println(selectedProduct);
    }

    private static boolean isAlreadyFavorite(Product product) {
        for (Product favorite : favoriteProducts) {
            if (favorite.getUrl().equals(product.getUrl())) {
                return true;
            }
        }

        return false;
    }

    private static void showFavoriteProducts() {
        if (favoriteProducts.isEmpty()) {
            System.out.println("目前沒有收藏商品。");
            return;
        }

        System.out.println("=== 我的收藏商品 ===");

        for (int i = 0; i < favoriteProducts.size(); i++) {
            System.out.println("第 " + (i + 1) + " 筆");
            System.out.println(favoriteProducts.get(i));
            System.out.println("--------------------------------");
        }
    }

    private static void saveFavoriteProducts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("favorites.csv"))) {
            writer.write("platform,name,price,url,imageUrl");
            writer.newLine();

            for (Product product : favoriteProducts) {
                writer.write(toCsvLine(product));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("儲存收藏商品失敗：" + e.getMessage());
        }
    }

    private static void loadFavoriteProducts() {
        try (BufferedReader reader = new BufferedReader(new FileReader("favorites.csv"))) {
            String line = reader.readLine(); // 讀掉標題列

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);

                if (data.length < 5) {
                    continue;
                }

                String platform = data[0];
                String name = data[1];
                double price = Double.parseDouble(data[2]);
                String url = data[3];
                String imageUrl = data[4];

                Product product = new Product(name, price, platform, url, imageUrl);
                favoriteProducts.add(product);
            }

            if (!favoriteProducts.isEmpty()) {
                System.out.println("已載入 " + favoriteProducts.size() + " 筆收藏商品。");
            }

        } catch (IOException e) {
            // 第一次執行通常還沒有 favorites.csv，這是正常情況
        } catch (NumberFormatException e) {
            System.out.println("收藏商品檔案格式錯誤，部分資料無法讀取。");
        }
    }

    private static String toCsvLine(Product product) {
        return escapeCsv(product.getPlatform()) + ","
                + escapeCsv(product.getName()) + ","
                + product.getPrice() + ","
                + escapeCsv(product.getUrl()) + ","
                + escapeCsv(product.getImageUrl());
    }

    private static String escapeCsv(String text) {
        if (text == null) {
            return "";
        }

        text = text.replace("\"", "\"\"");

        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text + "\"";
        }

        return text;
    }

    private static void removeFavoriteProduct() {
        if (favoriteProducts.isEmpty()) {
            System.out.println("目前沒有收藏商品可以刪除。");
            return;
        }

        showFavoriteProducts();

        System.out.print("請輸入要刪除的收藏商品編號，輸入 0 取消：");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == 0) {
            System.out.println("已取消刪除。");
            return;
        }

        if (index < 1 || index > favoriteProducts.size()) {
            System.out.println("收藏商品編號錯誤。");
            return;
        }

        Product removedProduct = favoriteProducts.remove(index - 1);

        saveFavoriteProducts();

        System.out.println("已刪除收藏商品：");
        System.out.println(removedProduct);
    }
}