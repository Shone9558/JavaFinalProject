package ntou.cs.java2026;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.model.Product;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 智慧購物比價追蹤器 ===");
        System.out.print("請輸入搜尋關鍵字：");
        String keyword = scanner.nextLine();

        System.out.println("搜尋關鍵字：" + keyword + "\n");

        // 博客來
        System.out.println("【博客來】");
        BooksCrawler books = new BooksCrawler();
        for (Product p : books.search(keyword)) System.out.println(p);

        System.out.println();

        // PChome
        System.out.println("【PChome】");
        PChomeCrawler pchome = new PChomeCrawler();
        for (Product p : pchome.search(keyword)) System.out.println(p);

        System.out.println();

        // momo
        System.out.println("【momo】");
        MomoCrawler momo = new MomoCrawler();
        for (Product p : momo.search(keyword)) System.out.println(p);

        scanner.close();
    }
}