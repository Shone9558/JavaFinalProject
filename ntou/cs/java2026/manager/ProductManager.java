package ntou.cs.java2026.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.model.Product;

public class ProductManager {
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private List<Product> platformFilteredProducts;

    private FavoriteManager favoriteManager;
    private HistoryManager historyManager;
    private FilterManager filterManager;

    public ProductManager() {

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        platformFilteredProducts = new ArrayList<>();

        favoriteManager = new FavoriteManager();
        historyManager = new HistoryManager();

        filterManager = new FilterManager(
                allProducts,
                filteredProducts,
                platformFilteredProducts
        );
    }

    public void searchProducts(Scanner scanner) {

        System.out.print("請輸入搜尋關鍵字：");
        String keyword = scanner.nextLine();

        historyManager.addHistory(keyword);

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

        allProducts.sort((p1, p2) ->
                Double.compare(p1.getPrice(), p2.getPrice())
        );

        if (allProducts.isEmpty()) {

            System.out.println("\n查無商品。");

        } else {

            System.out.println("\n搜尋完成，共找到 "
                    + allProducts.size()
                    + " 筆商品。");

            System.out.println("商品已依價格排序。");
        }
    }

    public void showAllProducts() {

        if (allProducts.isEmpty()) {

            System.out.println("目前沒有搜尋結果。");
            return;
        }

        System.out.println("=== 搜尋結果 ===");

        for (int i = 0; i < allProducts.size(); i++) {

            System.out.println("第 " + (i + 1) + " 筆");

            System.out.println(allProducts.get(i));

            System.out.println("--------------------------------");
        }
    }

    public void filterByPrice(Scanner scanner) {

        filteredProducts.clear();

        filteredProducts.addAll(
                filterManager.filterByPrice(scanner)
        );
    }

    public void filterByPlatform(Scanner scanner) {

        platformFilteredProducts.clear();

        platformFilteredProducts.addAll(
                filterManager.filterByPlatform(scanner)
        );
    }

    public void addFavorite(Scanner scanner) {

        favoriteManager.addFavorite(
                scanner,
                allProducts,
                filteredProducts,
                platformFilteredProducts
        );
    }

    public void showFavorites() {

        favoriteManager.showFavoriteProducts();
    }

    public void removeFavorite(Scanner scanner) {

        favoriteManager.removeFavoriteProduct(scanner);
    }

    public void showHistory() {

        historyManager.showSearchHistory();
    }

    public void removeHistory(Scanner scanner) {

        historyManager.removeSearchHistory(scanner);
    }

    public void saveAll() {

        favoriteManager.saveFavoriteProducts();

        historyManager.saveSearchHistory();
    }
}
