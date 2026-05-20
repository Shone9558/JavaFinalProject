package ntou.cs.java2026;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<Product> allProducts = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            showMenu();

            System.out.print("請選擇功能：");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 清除換行字元

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
        System.out.println("4. 離開");
    }

    private static void searchProducts() {
        System.out.print("請輸入搜尋關鍵字：");
        String keyword = scanner.nextLine();

        allProducts.clear();

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
        scanner.nextLine(); // 清除換行字元

        System.out.println("\n=== 價格篩選結果 ===");

        int count = 0;

        for (Product product : allProducts) {
            if (maxPrice == 0 || product.getPrice() <= maxPrice) {
                count++;
                System.out.println("第 " + count + " 筆");
                System.out.println(product);
                System.out.println("--------------------------------");
            }
        }

        if (count == 0) {
            System.out.println("沒有符合預算的商品。");
        }
    }
}