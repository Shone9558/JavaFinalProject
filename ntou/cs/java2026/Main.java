package ntou.cs.java2026;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 智慧購物比價追蹤器 ===");
        System.out.print("請輸入搜尋關鍵字：");
        String keyword = scanner.nextLine();

        List<Product> allProducts = new ArrayList<>();

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

        System.out.println("\n=== 比價結果：價格由低到高 ===");

        if (allProducts.isEmpty()) {
            System.out.println("查無商品，請換一個關鍵字試試看。");
        } else {
            for (int i = 0; i < allProducts.size(); i++) {
                System.out.println("第 " + (i + 1) + " 筆");
                System.out.println(allProducts.get(i));
                System.out.println("--------------------------------");
            }
        }

        scanner.close();
    }
}