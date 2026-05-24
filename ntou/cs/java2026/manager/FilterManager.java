package ntou.cs.java2026.manager;

import ntou.cs.java2026.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FilterManager {

    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private List<Product> platformFilteredProducts;

    public FilterManager(
            List<Product> allProducts,
            List<Product> filteredProducts,
            List<Product> platformFilteredProducts
    ) {

        this.allProducts = allProducts;
        this.filteredProducts = filteredProducts;
        this.platformFilteredProducts = platformFilteredProducts;
    }

    public List<Product> filterByPrice(Scanner scanner) {

        List<Product> result = new ArrayList<>();

        if (allProducts.isEmpty()) {

            System.out.println("目前沒有搜尋結果。");
            return result;
        }

        System.out.print("請輸入最低價格，若不限制請輸入 0：");
        double minPrice = scanner.nextDouble();

        System.out.print("請輸入最高價格，若不限制請輸入 0：");
        double maxPrice = scanner.nextDouble();

        scanner.nextLine();

        if (minPrice < 0 || maxPrice < 0) {

            System.out.println("價格不能是負數。");
            return result;
        }

        if (maxPrice != 0 && minPrice > maxPrice) {

            System.out.println("最低價格不能大於最高價格。");
            return result;
        }

        for (Product product : allProducts) {

            double price = product.getPrice();

            boolean matchMin =
                    (minPrice == 0 || price >= minPrice);

            boolean matchMax =
                    (maxPrice == 0 || price <= maxPrice);

            if (matchMin && matchMax) {

                result.add(product);
            }
        }

        filteredProducts.clear();
        filteredProducts.addAll(result);

        System.out.println("\n=== 價格篩選結果 ===");

        if (result.isEmpty()) {

            System.out.println("沒有符合預算的商品。");
            return result;
        }

        for (int i = 0; i < result.size(); i++) {

            System.out.println("第 " + (i + 1) + " 筆");

            System.out.println(result.get(i));

            System.out.println("--------------------------------");
        }

        System.out.println(
                "共找到 "
                        + result.size()
                        + " 筆符合預算的商品。"
        );

        return result;
    }

    public List<Product> filterByPlatform(Scanner scanner) {

        List<Product> result = new ArrayList<>();

        if (allProducts.isEmpty()) {

            System.out.println("目前沒有搜尋結果。");
            return result;
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
                System.out.println("輸入錯誤。");
                return result;
        }

        for (Product product : allProducts) {

            if (product.getPlatform().equals(selectedPlatform)) {

                result.add(product);
            }
        }

        platformFilteredProducts.clear();

        platformFilteredProducts.addAll(result);

        if (result.isEmpty()) {

            System.out.println("該平台沒有商品。");
            return result;
        }

        System.out.println(
                "=== "
                        + selectedPlatform
                        + " 商品結果 ==="
        );

        for (int i = 0; i < result.size(); i++) {

            System.out.println("第 " + (i + 1) + " 筆");

            System.out.println(result.get(i));

            System.out.println("--------------------------------");
        }

        System.out.println(
                "共找到 "
                        + result.size()
                        + " 筆 "
                        + selectedPlatform
                        + " 商品。"
        );

        return result;
    }
}