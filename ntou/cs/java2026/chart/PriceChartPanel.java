package ntou.cs.java2026.chart;

import ntou.cs.java2026.model.Product;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

public class PriceChartPanel extends JPanel {

    private static final Color BG = new Color(247, 242, 234);
    private static final String[] PLATFORMS = {"博客來", "PChome", "momo", "Yahoo購物"};
    private static final Color[] COLORS = {
        new Color(128, 98, 70),
        new Color(180, 140, 100),
        new Color(210, 100, 80),
        new Color(80, 140, 180)
    };

    private List<Product> currentProducts = new ArrayList<>();
    private ChartPanel chartPanel;

    public PriceChartPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        JLabel hint = new JLabel("搜尋商品後點擊「比價圖表」即可顯示", SwingConstants.CENTER);
        hint.setForeground(new Color(125, 113, 100));
        hint.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        add(hint, BorderLayout.CENTER);
    }

    public void showPriceChart(List<Product> products) {
        removeAll();
        currentProducts = products;

        if (products.isEmpty()) {
            JLabel hint = new JLabel("沒有商品可以顯示", SwingConstants.CENTER);
            hint.setForeground(new Color(125, 113, 100));
            hint.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            add(hint, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // 每個平台一個 XYSeries
        Map<String, XYSeries> seriesMap = new LinkedHashMap<>();
        for (String platform : PLATFORMS) {
            seriesMap.put(platform, new XYSeries(platform));
        }

        // 把商品加進對應平台的 series
        // X 軸：平台編號（0=博客來, 1=PChome, 2=momo, 3=Yahoo購物）
        // Y 軸：價格
        // 同平台多筆商品用隨機小偏移讓點不重疊
        Random rand = new Random(42);
        for (Product p : products) {
            int platformIndex = getPlatformIndex(p.getPlatform());
            if (platformIndex < 0) continue;
            double x = platformIndex + (rand.nextDouble() - 0.5) * 0.4;
            seriesMap.get(p.getPlatform()).add(x, p.getPrice());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries series : seriesMap.values()) {
            dataset.addSeries(series);
        }

        // 建立散佈圖
        SymbolAxis xAxis = new SymbolAxis("平台", PLATFORMS);
        xAxis.setLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        xAxis.setTickLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        xAxis.setRange(-0.5, PLATFORMS.length - 0.5);

        NumberAxis yAxis = new NumberAxis("價格 (NT$)");
        yAxis.setLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        yAxis.setTickLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        yAxis.setAutoRangeIncludesZero(false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        Shape dot = new Ellipse2D.Double(-6, -6, 12, 12);
        for (int i = 0; i < PLATFORMS.length; i++) {
            renderer.setSeriesPaint(i, COLORS[i]);
            renderer.setSeriesShape(i, dot);
            renderer.setSeriesToolTipGenerator(i, (d, series, item) -> {
                // 找到對應商品
                String platform = PLATFORMS[series];
                double price = d.getYValue(series, item);
                for (Product p : currentProducts) {
                    if (p.getPlatform().equals(platform) &&
                        Math.abs(p.getPrice() - price) < 1) {
                        return String.format("<html><b>%s</b><br>%s<br>NT$%.0f</html>",
                            p.getPlatform(), p.getName(), p.getPrice());
                    }
                }
                return String.format("NT$%.0f", price);
            });
        }

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(new Color(255, 253, 249));
        plot.setRangeGridlinePaint(new Color(226, 216, 202));
        plot.setDomainGridlinesVisible(false);

        JFreeChart chart = new JFreeChart(
            "各平台商品價格散佈圖（共 " + products.size() + " 筆）",
            new Font("Microsoft JhengHei", Font.BOLD, 16),
            plot,
            true
        );
        chart.setBackgroundPaint(BG);
        chart.getLegend().setItemFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));

        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(BG);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(true);
        add(chartPanel, BorderLayout.CENTER);

        // 說明文字
        JLabel hint = new JLabel("💡 滑鼠移到圓點上可查看商品名稱與價格，滾輪可縮放Y軸", SwingConstants.CENTER);
        hint.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        hint.setForeground(new Color(125, 113, 100));
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        add(hint, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    public void showFavoritePriceChart(List<Product> favoriteProducts) {
        showPriceChart(favoriteProducts);
    }

    private int getPlatformIndex(String platform) {
        for (int i = 0; i < PLATFORMS.length; i++) {
            if (PLATFORMS[i].equals(platform)) return i;
        }
        return -1;
    }
}