package ntou.cs.java2026.manager;

import ntou.cs.java2026.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FavoriteManager {

    private List<Product> favoriteProducts;

    public FavoriteManager() {

        favoriteProducts = new ArrayList<>();

        loadFavoriteProducts();
    }

    public void addFavorite(
            Scanner scanner,
            List<Product> allProducts,
            List<Product> filteredProducts,
            List<Product> platformFilteredProducts
    ) {

        if (allProducts.isEmpty()) {

            System.out.println("目前沒有搜尋結果。");
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

                System.out.println("目前沒有價格篩選結果。");
                return;
            }

            sourceList = filteredProducts;

        } else if (sourceChoice == 3) {

            if (platformFilteredProducts.isEmpty()) {

                System.out.println("目前沒有平台篩選結果。");
                return;
            }

            sourceList = platformFilteredProducts;

        } else {

            System.out.println("輸入錯誤。");
            return;
        }

        List<Product> availableProducts = new ArrayList<>();

        for (Product product : sourceList) {

            if (!isAlreadyFavorite(product)) {

                availableProducts.add(product);
            }
        }

        if (availableProducts.isEmpty()) {

            System.out.println("商品都已收藏過。");
            return;
        }

        System.out.println("\n=== 可收藏商品 ===");

        for (int i = 0; i < availableProducts.size(); i++) {

            System.out.println("第 " + (i + 1) + " 筆");

            System.out.println(availableProducts.get(i));

            System.out.println("--------------------------------");
        }

        System.out.print("請輸入商品編號，輸入 0 取消：");

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

        Product selectedProduct =
                availableProducts.get(index - 1);

        favoriteProducts.add(selectedProduct);

        saveFavoriteProducts();

        System.out.println("已收藏商品：");

        System.out.println(selectedProduct);
    }

    public boolean isAlreadyFavorite(Product product) {

        for (Product favorite : favoriteProducts) {

            if (
                    favorite.getUrl()
                            .equals(product.getUrl())
            ) {

                return true;
            }
        }

        return false;
    }

    public void showFavoriteProducts() {

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

    public void removeFavoriteProduct(Scanner scanner) {

        if (favoriteProducts.isEmpty()) {

            System.out.println("目前沒有收藏商品。");
            return;
        }

        showFavoriteProducts();

        System.out.print(
                "請輸入要刪除的收藏商品編號，輸入 0 取消："
        );

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

        Product removedProduct =
                favoriteProducts.remove(index - 1);

        saveFavoriteProducts();

        System.out.println("已刪除收藏商品：");

        System.out.println(removedProduct);
    }

    public void saveFavoriteProducts() {

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter("favorites.csv")
                        )
        ) {

            writer.write(
                    "platform,name,price,url,imageUrl"
            );

            writer.newLine();

            for (Product product : favoriteProducts) {

                writer.write(toCsvLine(product));

                writer.newLine();
            }

        } catch (IOException e) {

            System.out.println(
                    "儲存收藏商品失敗："
                            + e.getMessage()
            );
        }
    }

    public void loadFavoriteProducts() {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader("favorites.csv")
                        )
        ) {

            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] data =
                        line.split(",", -1);

                if (data.length < 5) {

                    continue;
                }

                String platform = data[0];

                String name = data[1];

                double price =
                        Double.parseDouble(data[2]);

                String url = data[3];

                String imageUrl = data[4];

                Product product =
                        new Product(
                                name,
                                price,
                                platform,
                                url,
                                imageUrl
                        );

                favoriteProducts.add(product);
            }

            if (!favoriteProducts.isEmpty()) {

                System.out.println(
                        "已載入 "
                                + favoriteProducts.size()
                                + " 筆收藏商品。"
                );
            }

        } catch (IOException e) {

            // 第一次執行通常沒有檔案
        } catch (NumberFormatException e) {

            System.out.println(
                    "收藏商品檔案格式錯誤。"
            );
        }
    }

    private String toCsvLine(Product product) {

        return escapeCsv(product.getPlatform())
                + ","
                + escapeCsv(product.getName())
                + ","
                + product.getPrice()
                + ","
                + escapeCsv(product.getUrl())
                + ","
                + escapeCsv(product.getImageUrl());
    }

    private String escapeCsv(String text) {

        if (text == null) {

            return "";
        }

        text = text.replace("\"", "\"\"");

        if (
                text.contains(",")
                        || text.contains("\"")
                        || text.contains("\n")
        ) {

            text = "\"" + text + "\"";
        }

        return text;
    }

    public List<Product> getFavoriteProducts() {

        return favoriteProducts;
    }
}