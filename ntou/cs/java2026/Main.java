package ntou.cs.java2026;

import ntou.cs.java2026.manager.ProductManager;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ProductManager manager = new ProductManager();

        boolean running = true;

        while (running) {

            showMenu();

            System.out.print("請選擇功能：");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    manager.searchProducts(scanner);
                    break;

                case 2:
                    manager.showAllProducts();
                    break;

                case 3:
                    manager.filterByPrice(scanner);
                    break;

                case 4:
                    manager.filterByPlatform(scanner);
                    break;

                case 5:
                    manager.addFavorite(scanner);
                    break;

                case 6:
                    manager.showFavorites();
                    break;

                case 7:
                    manager.removeFavorite(scanner);
                    break;

                case 8:
                    manager.showHistory();
                    break;

                case 9:
                    manager.removeHistory(scanner);
                    break;

                case 10:
                    manager.saveAll();
                    running = false;
                    System.out.println("感謝使用！");
                    break;

                default:
                    System.out.println("輸入錯誤");
            }
        }

        scanner.close();
    }

    private static void showMenu() {

        System.out.println("=== 智慧購物比價追蹤器 ===");
        System.out.println("1. 搜尋商品");
        System.out.println("2. 顯示搜尋結果");
        System.out.println("3. 價格篩選");
        System.out.println("4. 平台篩選");
        System.out.println("5. 收藏商品");
        System.out.println("6. 查看收藏");
        System.out.println("7. 刪除收藏");
        System.out.println("8. 查看搜尋歷史");
        System.out.println("9. 刪除搜尋歷史");
        System.out.println("10. 離開");
    }
}