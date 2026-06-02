package ntou.cs.java2026;

import ntou.cs.java2026.chart.PriceChartPanel;
import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.crawler.YahooCrawler;
import ntou.cs.java2026.model.Product;
import ntou.cs.java2026.manager.FavoriteManager;
import ntou.cs.java2026.manager.HistoryManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainUI extends JFrame {

    private static final Color BG = new Color(247, 242, 234);
    private static final Color CARD = new Color(255, 253, 249);
    private static final Color LINE = new Color(226, 216, 202);
    private static final Color MAIN = new Color(128, 98, 70);
    private static final Color MAIN_DARK = new Color(88, 65, 45);
    private static final Color TEXT = new Color(48, 43, 38);
    private static final Color MUTED = new Color(125, 113, 100);
    private static final Color CHIP = new Color(239, 229, 215);

    private final JTextField keywordField = new JTextField();
    private final JCheckBox booksCheck = new JCheckBox("博客來", true);
    private final JCheckBox pchomeCheck = new JCheckBox("PChome", true);
    private final JCheckBox momoCheck = new JCheckBox("momo", true);
    private final JCheckBox yahooCheck = new JCheckBox("Yahoo購物", true);
    private final JComboBox<String> sortBox = new JComboBox<>(new String[]{"價格由低到高", "價格由高到低", "平台名稱", "商品名稱"});
    private final JTextField minPriceField = new JTextField();
    private final JTextField maxPriceField = new JTextField();

    private final JButton searchButton = new JButton("搜尋");
    private final JButton favoriteButton = new JButton("加入收藏");
    private final JButton deleteFavoriteButton = new JButton("刪除收藏");
    private final JButton refreshFavoritePriceButton = new JButton("刷新收藏價格");
    private final JButton exportFavoriteCsvButton = new JButton("匯出 CSV");
    private final JButton openButton = new JButton("開啟商品頁");
    private final JButton useHistoryButton = new JButton("用此關鍵字搜尋");
    private final JButton deleteHistoryButton = new JButton("刪除歷史");
    private final JButton clearHistoryButton = new JButton("清空歷史");
    private final JButton showChartButton = new JButton("比價圖表");

    private final JLabel statusLabel = new JLabel("輸入關鍵字，開始找今天最值得買的商品");
    private final JLabel imageLabel = new JLabel("選取商品後顯示圖片", SwingConstants.CENTER);
    private final JLabel imageTitleLabel = new JLabel("商品圖片", SwingConstants.CENTER);
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyListModel);

    private final JPanel resultGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
    private final JPanel favoriteGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));

    private final List<Product> allProducts = new ArrayList<>();
    private final FavoriteManager favoriteManager = new FavoriteManager();
    private final List<Product> favoriteProducts = favoriteManager.getFavoriteProducts();
    private final HistoryManager historyManager = new HistoryManager();
    private final List<String> searchHistory = historyManager.getSearchHistory();

    private final PriceChartPanel priceChartPanel = new PriceChartPanel();

    private Product selectedProduct = null;
    private boolean selectedFromFavorite = false;

    public MainUI() {
        setTitle("智慧購物比價追蹤器");
        setSize(1180, 760);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLookAndFeel();
        initLayout();
        initEvents();
        refreshFavoriteCards();
        refreshHistoryList();
    }

    private boolean isFavorite(Product product) {
        if (product == null) return false;
        for (Product p : favoriteProducts) {
            if (isSameProduct(p, product)) return true;
        }
        return false;
    }

    private void removeFavoriteProduct(Product product) {
        if (product == null) return;
        favoriteProducts.removeIf(p -> isSameProduct(p, product));
    }

    private void initLookAndFeel() {
        Font base = new Font("Microsoft JhengHei", Font.PLAIN, 14);
        UIManager.put("Button.font", new Font("Microsoft JhengHei", Font.BOLD, 14));
        UIManager.put("Label.font", base);
        UIManager.put("CheckBox.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("TabbedPane.font", new Font("Microsoft JhengHei", Font.BOLD, 14));
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 22, 18, 22));
        setContentPane(root);

        root.add(createHeroPanel(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.addTab("搜尋結果", createScrollCardPanel(resultGrid));
        tabs.addTab("我的收藏", createScrollCardPanel(favoriteGrid));
        tabs.addTab("搜尋歷史", createHistoryPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, createImagePanel());
        splitPane.setResizeWeight(0.78);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        root.add(splitPane, BorderLayout.CENTER);

        root.add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeroPanel() {
        JPanel hero = roundPanel(CARD, 22);
        hero.setLayout(new BorderLayout(18, 14));
        hero.setBorder(new EmptyBorder(22, 24, 20, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("智慧購物比價追蹤器");
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        title.setForeground(MAIN_DARK);

        JLabel subtitle = new JLabel("用更舒服的方式，找到真正值得買的商品");
        subtitle.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        hero.add(titleBox, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new BorderLayout(12, 10));
        searchBox.setOpaque(false);

        JPanel searchLine = new JPanel(new BorderLayout(10, 0));
        searchLine.setOpaque(false);
        keywordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(10, 14, 10, 14)
        ));
        keywordField.setToolTipText("輸入想搜尋的商品，例如：手機、耳機、鍵盤");
        searchLine.add(keywordField, BorderLayout.CENTER);
        stylePrimaryButton(searchButton);
        searchLine.add(searchButton, BorderLayout.EAST);
        searchBox.add(searchLine, BorderLayout.NORTH);

        JPanel filterLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterLine.setOpaque(false);
        styleChip(booksCheck);
        styleChip(pchomeCheck);
        styleChip(momoCheck);
        styleChip(yahooCheck);
        filterLine.add(new JLabel("平台"));
        filterLine.add(booksCheck);
        filterLine.add(pchomeCheck);
        filterLine.add(momoCheck);
        filterLine.add(yahooCheck);

        minPriceField.setColumns(6);
        maxPriceField.setColumns(6);
        minPriceField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(6, 8, 6, 8)));
        maxPriceField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(6, 8, 6, 8)));
        sortBox.setBackground(Color.WHITE);

        filterLine.add(Box.createHorizontalStrut(10));
        filterLine.add(new JLabel("價格"));
        filterLine.add(minPriceField);
        filterLine.add(new JLabel("—"));
        filterLine.add(maxPriceField);
        filterLine.add(sortBox);

        searchBox.add(filterLine, BorderLayout.SOUTH);
        hero.add(searchBox, BorderLayout.CENTER);

        return hero;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setOpaque(false);

        statusLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        statusLabel.setForeground(MUTED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        styleSecondaryButton(favoriteButton);
        styleSecondaryButton(openButton);
        styleSecondaryButton(deleteFavoriteButton);
        styleSecondaryButton(refreshFavoritePriceButton);
        styleSecondaryButton(exportFavoriteCsvButton);
        styleSecondaryButton(showChartButton);
        buttonPanel.add(favoriteButton);
        buttonPanel.add(deleteFavoriteButton);
        buttonPanel.add(refreshFavoritePriceButton);
        buttonPanel.add(exportFavoriteCsvButton);
        buttonPanel.add(showChartButton);
        buttonPanel.add(openButton);

        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(buttonPanel, BorderLayout.EAST);
        return bottom;
    }

    private JScrollPane createScrollCardPanel(JPanel grid) {
        grid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = roundPanel(CARD, 18);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel hintLabel = new JLabel("雙擊歷史關鍵字，或選取後按按鈕，即可重新搜尋");
        hintLabel.setForeground(MUTED);
        panel.add(hintLabel, BorderLayout.NORTH);

        historyList.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setFixedCellHeight(36);
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        historyButtonPanel.setOpaque(false);
        styleSecondaryButton(useHistoryButton);
        styleSecondaryButton(deleteHistoryButton);
        styleSecondaryButton(clearHistoryButton);
        historyButtonPanel.add(useHistoryButton);
        historyButtonPanel.add(deleteHistoryButton);
        historyButtonPanel.add(clearHistoryButton);
        panel.add(historyButtonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = roundPanel(CARD, 18);
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        imageTitleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        imageTitleLabel.setForeground(MAIN_DARK);
        panel.add(imageTitleLabel, BorderLayout.NORTH);

        imageLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        imageLabel.setForeground(MUTED);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(252, 249, 244));
        imageLabel.setBorder(BorderFactory.createLineBorder(LINE));
        panel.add(imageLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProductCard(Product product, boolean fromFavorite) {
        JPanel card = roundPanel(CARD, 18);
        card.setPreferredSize(new Dimension(245, 220));
        card.setLayout(new BorderLayout(10, 8));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel platform = new JLabel(product.getPlatform());
        platform.setOpaque(true);
        platform.setBackground(CHIP);
        platform.setForeground(MAIN_DARK);
        platform.setBorder(new EmptyBorder(4, 9, 4, 9));
        platform.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));

        JLabel price = new JLabel(String.format("NT$ %.0f", product.getPrice()));
        price.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        price.setForeground(MAIN_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(platform, BorderLayout.WEST);
        top.add(price, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JTextArea name = new JTextArea(product.getName());
        name.setLineWrap(true);
        name.setWrapStyleWord(true);
        name.setEditable(false);
        name.setOpaque(false);
        name.setForeground(TEXT);
        name.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        card.add(name, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);
        JButton view = new JButton("查看");
        boolean favoriteNow = isFavorite(product);
        JButton fav = new JButton(favoriteNow ? "已收藏" : "收藏");
        fav.setIcon(new HeartIcon(favoriteNow));
        styleMiniButton(view, true);
        styleFavoriteButton(fav, favoriteNow);
        actions.add(fav);
        actions.add(view);
        card.add(actions, BorderLayout.SOUTH);

        MouseAdapter selectAction = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectProduct(product, fromFavorite);
            }
        };
        card.addMouseListener(selectAction);
        name.addMouseListener(selectAction);

        view.addActionListener(e -> {
            selectProduct(product, fromFavorite);
            openSelectedProduct();
        });

        fav.addActionListener(e -> {
            boolean nowFavorite = isFavorite(product);
            if (nowFavorite) {
                removeFavoriteProduct(product);
                favoriteManager.saveFavoriteProducts();
                selectedProduct = product;
                selectedFromFavorite = false;
                statusLabel.setText("已取消收藏：" + product.getName());
            } else {
                favoriteProducts.add(product);
                favoriteManager.saveFavoriteProducts();
                selectedProduct = product;
                selectedFromFavorite = false;
                statusLabel.setText("已加入收藏：" + product.getName());
            }
            refreshResultCards();
            refreshFavoriteCards();
        });

        return card;
    }

    private void selectProduct(Product product, boolean fromFavorite) {
        selectedProduct = product;
        selectedFromFavorite = fromFavorite;
        showProductImage(product);
        statusLabel.setText("已選取：" + product.getName());
    }

    private JPanel roundPanel(Color color, int radius) {
        return new JPanel() {
            { setOpaque(false); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.setColor(LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private void styleChip(JCheckBox box) {
        box.setOpaque(true);
        box.setBackground(CHIP);
        box.setForeground(MAIN_DARK);
        box.setFocusPainted(false);
        box.setBorder(new EmptyBorder(6, 10, 6, 10));
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(MAIN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(CARD);
        button.setForeground(MAIN_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAIN),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleMiniButton(JButton button, boolean filled) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (filled) {
            button.setBackground(MAIN);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(CARD);
            button.setForeground(MAIN_DARK);
        }
    }

    private void styleFavoriteButton(JButton button, boolean favorite) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(6);
        if (favorite) {
            button.setBackground(new Color(255, 241, 241));
            button.setForeground(new Color(170, 57, 57));
            button.setBorder(BorderFactory.createLineBorder(new Color(210, 120, 120)));
        } else {
            button.setBackground(CARD);
            button.setForeground(MAIN_DARK);
            button.setBorder(BorderFactory.createLineBorder(MAIN));
        }
    }

    private void initEvents() {
        searchButton.addActionListener(e -> searchProducts());
        favoriteButton.addActionListener(e -> addFavorite());
        deleteFavoriteButton.addActionListener(e -> removeFavorite());
        refreshFavoritePriceButton.addActionListener(e -> refreshFavoritePrices());
        exportFavoriteCsvButton.addActionListener(e -> exportFavoritesCsv());
        openButton.addActionListener(e -> openSelectedProduct());
        useHistoryButton.addActionListener(e -> searchFromSelectedHistory());
        deleteHistoryButton.addActionListener(e -> deleteSelectedHistory());
        clearHistoryButton.addActionListener(e -> clearSearchHistory());
        showChartButton.addActionListener(e -> showPriceChart());

        keywordField.addActionListener(e -> searchProducts());

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) searchFromSelectedHistory();
            }
        });
    }

    private void showPriceChart() {
        if (allProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先搜尋商品", "比價圖表", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "價格比較圖表", false);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        priceChartPanel.showPriceChart(allProducts);
        dialog.add(priceChartPanel);
        dialog.setVisible(true);
    }

    private void searchProducts() {
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先輸入搜尋關鍵字", "輸入提醒", JOptionPane.WARNING_MESSAGE);
            keywordField.requestFocus();
            return;
        }

        if (!booksCheck.isSelected() && !pchomeCheck.isSelected() && !momoCheck.isSelected() && !yahooCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "請至少選擇一個搜尋平台", "平台提醒", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double min = parseOptionalPrice(minPriceField.getText(), "最低價格");
            double max = parseOptionalPrice(maxPriceField.getText(), "最高價格");
            if (min > 0 && max > 0 && min > max) {
                JOptionPane.showMessageDialog(this, "最低價格不能大於最高價格", "價格提醒", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "價格格式錯誤", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setButtonsEnabled(false);
        resultGrid.removeAll();
        allProducts.clear();
        selectedProduct = null;
        imageLabel.setIcon(null);
        imageLabel.setText("選取商品後顯示圖片");
        statusLabel.setText("正在為你尋找商品...");
        historyManager.addHistory(keyword);
        refreshHistoryList();

        SwingWorker<List<Product>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() {
                List<Product> list = new ArrayList<>();

                if (booksCheck.isSelected()) {
                    publish("正在搜尋博客來...");
                    try { list.addAll(new BooksCrawler().search(keyword)); }
                    catch (Exception ex) { publish("博客來搜尋失敗，已略過：" + ex.getMessage()); }
                }
                if (pchomeCheck.isSelected()) {
                    publish("正在搜尋 PChome...");
                    try { list.addAll(new PChomeCrawler().search(keyword)); }
                    catch (Exception ex) { publish("PChome 搜尋失敗，已略過：" + ex.getMessage()); }
                }
                if (momoCheck.isSelected()) {
                    publish("正在搜尋 momo...");
                    try { list.addAll(new MomoCrawler().search(keyword)); }
                    catch (Exception ex) { publish("momo 搜尋失敗，已略過：" + ex.getMessage()); }
                }
                if (yahooCheck.isSelected()) {
                    publish("正在搜尋 Yahoo購物...");
                    try { list.addAll(new YahooCrawler().search(keyword)); }
                    catch (Exception ex) { publish("Yahoo購物搜尋失敗，已略過：" + ex.getMessage()); }
                }

                publish("正在套用篩選與排序...");
                return applyFilterAndSort(list);
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    allProducts.addAll(get());
                    refreshResultCards();
                    if (allProducts.isEmpty()) {
                        statusLabel.setText("搜尋完成，但沒有符合條件的商品");
                        JOptionPane.showMessageDialog(MainUI.this, "沒有找到符合條件的商品，可以換關鍵字或放寬價格範圍。", "搜尋結果", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("搜尋完成，共找到 " + allProducts.size() + " 筆商品");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("搜尋失敗：" + ex.getMessage());
                    JOptionPane.showMessageDialog(MainUI.this, "搜尋失敗：" + ex.getMessage(), "搜尋錯誤", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        searchButton.setEnabled(enabled);
        favoriteButton.setEnabled(enabled);
        openButton.setEnabled(enabled);
        deleteFavoriteButton.setEnabled(enabled);
        refreshFavoritePriceButton.setEnabled(enabled);
        exportFavoriteCsvButton.setEnabled(enabled);
        showChartButton.setEnabled(enabled);
    }

    private List<Product> applyFilterAndSort(List<Product> list) {
        double min = parseOptionalPrice(minPriceField.getText());
        double max = parseOptionalPrice(maxPriceField.getText());

        List<Product> filtered = new ArrayList<>();
        for (Product product : list) {
            boolean matchMin = min == 0 || product.getPrice() >= min;
            boolean matchMax = max == 0 || product.getPrice() <= max;
            if (matchMin && matchMax) filtered.add(product);
        }

        String sort = (String) sortBox.getSelectedItem();
        if ("價格由高到低".equals(sort)) filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        else if ("平台名稱".equals(sort)) filtered.sort(Comparator.comparing(Product::getPlatform));
        else if ("商品名稱".equals(sort)) filtered.sort(Comparator.comparing(Product::getName));
        else filtered.sort(Comparator.comparingDouble(Product::getPrice));

        return filtered;
    }

    private double parseOptionalPrice(String text) {
        return parseOptionalPrice(text, "價格");
    }

    private double parseOptionalPrice(String text, String fieldName) {
        text = text.trim();
        if (text.isEmpty()) return 0;
        try {
            double value = Double.parseDouble(text);
            if (value < 0) throw new IllegalArgumentException(fieldName + "不能小於 0");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "請輸入數字，例如 100 或 1000");
        }
    }

    private void refreshResultCards() {
        resultGrid.removeAll();
        if (allProducts.isEmpty()) {
            resultGrid.add(emptyHint("目前沒有搜尋結果"));
        } else {
            for (Product product : allProducts) resultGrid.add(createProductCard(product, false));
        }
        resultGrid.revalidate();
        resultGrid.repaint();
    }

    private void refreshFavoriteCards() {
        favoriteGrid.removeAll();
        if (favoriteProducts.isEmpty()) {
            favoriteGrid.add(emptyHint("目前沒有收藏商品"));
        } else {
            for (Product product : favoriteProducts) favoriteGrid.add(createProductCard(product, true));
        }
        favoriteGrid.revalidate();
        favoriteGrid.repaint();
    }

    private JLabel emptyHint(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(MUTED);
        label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        label.setPreferredSize(new Dimension(300, 120));
        return label;
    }

    private void refreshHistoryList() {
        historyListModel.clear();
        for (int i = searchHistory.size() - 1; i >= 0; i--) historyListModel.addElement(searchHistory.get(i));
    }

    private void searchFromSelectedHistory() {
        String keyword = historyList.getSelectedValue();
        if (keyword == null || keyword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先選擇一筆搜尋歷史", "搜尋歷史", JOptionPane.WARNING_MESSAGE);
            return;
        }
        keywordField.setText(keyword);
        searchProducts();
    }

    private void deleteSelectedHistory() {
        String keyword = historyList.getSelectedValue();
        if (keyword == null || keyword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先選擇要刪除的搜尋歷史", "搜尋歷史", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "確定要刪除這筆搜尋歷史？\n" + keyword, "刪除搜尋歷史", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;
        searchHistory.remove(keyword);
        historyManager.saveSearchHistory();
        refreshHistoryList();
        statusLabel.setText("已刪除搜尋歷史：" + keyword);
    }

    private void clearSearchHistory() {
        if (searchHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有搜尋歷史可以清空", "搜尋歷史", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "確定要清空所有搜尋歷史？", "清空搜尋歷史", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;
        searchHistory.clear();
        historyManager.saveSearchHistory();
        refreshHistoryList();
        statusLabel.setText("已清空搜尋歷史");
    }

    private void addFavorite() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "請先選擇一個搜尋結果商品");
            return;
        }
        if (selectedFromFavorite) {
            JOptionPane.showMessageDialog(this, "這個商品已經在收藏中");
            return;
        }
        for (Product product : favoriteProducts) {
            if (isSameProduct(product, selectedProduct)) {
                JOptionPane.showMessageDialog(this, "這個商品已經收藏過了", "重複收藏", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        favoriteProducts.add(selectedProduct);
        favoriteManager.saveFavoriteProducts();
        refreshFavoriteCards();
        statusLabel.setText("已加入收藏：" + selectedProduct.getName());
    }

    private boolean isSameProduct(Product a, Product b) {
        if (a == null || b == null) return false;
        if (a.getUrl() != null && b.getUrl() != null && !a.getUrl().isEmpty() && !b.getUrl().isEmpty()) return a.getUrl().equals(b.getUrl());
        return a.getPlatform().equals(b.getPlatform()) && a.getName().equals(b.getName());
    }

    private void removeFavorite() {
        if (selectedProduct == null || !selectedFromFavorite) {
            JOptionPane.showMessageDialog(this, "請先在我的收藏中選擇要刪除的商品");
            return;
        }
        int index = favoriteProducts.indexOf(selectedProduct);
        if (index < 0) return;
        int option = JOptionPane.showConfirmDialog(this, "確定要刪除收藏商品？\n" + selectedProduct.getName(), "刪除收藏", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;
        Product removed = favoriteProducts.remove(index);
        selectedProduct = null;
        favoriteManager.saveFavoriteProducts();
        refreshFavoriteCards();
        statusLabel.setText("已刪除收藏：" + removed.getName());
    }

    private void refreshFavoritePrices() {
        if (favoriteProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有收藏商品可以更新", "重新整理收藏價格", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "將依收藏商品名稱重新搜尋目前價格，可能需要一些時間。是否繼續？", "重新整理收藏價格", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;

        setButtonsEnabled(false);
        statusLabel.setText("正在重新整理收藏價格...");

        SwingWorker<Integer, String> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int updatedCount = 0;
                for (Product oldProduct : favoriteProducts) {
                    publish("正在更新收藏價格：" + oldProduct.getName());
                    try {
                        List<Product> candidates = searchSamePlatform(oldProduct);
                        Product matched = findBestMatch(oldProduct, candidates);
                        if (matched != null) {
                            oldProduct.setPrice(matched.getPrice());
                            if (matched.getImageUrl() != null && !matched.getImageUrl().isEmpty()) oldProduct.setImageUrl(matched.getImageUrl());
                            if (matched.getUrl() != null && !matched.getUrl().isEmpty()) oldProduct.setUrl(matched.getUrl());
                            updatedCount++;
                        }
                    } catch (Exception ex) {
                        publish("更新失敗，已略過：" + oldProduct.getName());
                    }
                }
                return updatedCount;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    int updatedCount = get();
                    favoriteManager.saveFavoriteProducts();
                    refreshFavoriteCards();
                    statusLabel.setText("收藏價格更新完成，共更新 " + updatedCount + " 筆商品");
                    JOptionPane.showMessageDialog(MainUI.this, "收藏價格更新完成，共更新 " + updatedCount + " 筆商品。", "重新整理收藏價格", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainUI.this, "更新收藏價格失敗：" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("更新收藏價格失敗");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private List<Product> searchSamePlatform(Product product) {
        String platform = product.getPlatform();
        String keyword = simplifyKeyword(product.getName());
        if ("博客來".equalsIgnoreCase(platform)) return new BooksCrawler().search(keyword);
        if ("PChome".equalsIgnoreCase(platform)) return new PChomeCrawler().search(keyword);
        if ("momo".equalsIgnoreCase(platform)) return new MomoCrawler().search(keyword);
        if ("Yahoo購物".equalsIgnoreCase(platform)) return new YahooCrawler().search(keyword);
        return new ArrayList<>();
    }

    private String simplifyKeyword(String name) {
        if (name == null) return "";
        String cleaned = name.replaceAll("[【】\\[\\]（）(){}<>].*?[【】\\[\\]（）(){}<>]?", " ").trim();
        if (cleaned.length() > 35) cleaned = cleaned.substring(0, 35);
        return cleaned.trim().isEmpty() ? name : cleaned;
    }

    private Product findBestMatch(Product oldProduct, List<Product> candidates) {
        if (candidates == null || candidates.isEmpty()) return null;
        for (Product candidate : candidates) if (isSameProduct(oldProduct, candidate)) return candidate;
        String oldName = oldProduct.getName() == null ? "" : oldProduct.getName().toLowerCase();
        for (Product candidate : candidates) {
            String candidateName = candidate.getName() == null ? "" : candidate.getName().toLowerCase();
            if (!candidateName.isEmpty() && (oldName.contains(candidateName) || candidateName.contains(oldName))) return candidate;
        }
        return candidates.get(0);
    }

    private void exportFavoritesCsv() {
        if (favoriteProducts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有收藏商品可以匯出", "匯出收藏 CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("匯出收藏商品 CSV");
        chooser.setSelectedFile(new File("favorites_export.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("platform,name,price,url,imageUrl");
            writer.newLine();
            for (Product product : favoriteProducts) {
                writer.write(toCsvLine(product));
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "收藏商品已匯出到：\n" + file.getAbsolutePath(), "匯出完成", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("已匯出收藏 CSV：" + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "匯出 CSV 失敗：" + ex.getMessage(), "匯出錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String toCsvLine(Product product) {
        return escapeCsv(product.getPlatform()) + "," + escapeCsv(product.getName()) + "," + product.getPrice() + "," + escapeCsv(product.getUrl()) + "," + escapeCsv(product.getImageUrl());
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        text = text.replace("\"", "\"\"");
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) return "\"" + text + "\"";
        return text;
    }

    private void showProductImage(Product product) {
        if (product == null) {
            imageLabel.setIcon(null);
            imageLabel.setText("選取商品後顯示圖片");
            return;
        }
        String imageUrl = product.getImageUrl();
        imageTitleLabel.setText("商品圖片");
        imageLabel.setIcon(null);
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.startsWith("data:")) {
            imageLabel.setText("此商品沒有圖片");
            return;
        }
        imageLabel.setText("圖片載入中...");
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                ImageIO.scanForPlugins();
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(imageUrl).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                conn.setRequestProperty("Accept", "image/jpeg,image/png,image/gif,image/webp,*/*");
                conn.setRequestProperty("Accept-Language", "zh-TW,zh;q=0.9");
                conn.setInstanceFollowRedirects(true);
                String referer;
                if (imageUrl.contains("yec.tw") || imageUrl.contains("yahoo")) referer = "https://tw.buy.yahoo.com/";
                else if (imageUrl.contains("pchome") || imageUrl.contains("ecimg")) referer = "https://24h.pchome.com.tw/";
                else if (imageUrl.contains("momo")) referer = "https://www.momoshop.com.tw/";
                else referer = "https://www.books.com.tw/";
                conn.setRequestProperty("Referer", referer);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.connect();
                if (conn.getResponseCode() != 200) return null;
                java.io.InputStream is = conn.getInputStream();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) != -1) baos.write(buffer, 0, n);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
                BufferedImage image = ImageIO.read(bais);
                if (image == null) return null;
                Image scaled = scaleImage(image, 220, 260);
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon == null) {
                        imageLabel.setText("圖片無法載入");
                        return;
                    }
                    imageLabel.setText("");
                    imageLabel.setIcon(icon);
                } catch (Exception ex) {
                    imageLabel.setText("圖片載入失敗");
                }
            }
        };
        worker.execute();
    }

    private Image scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = Math.max(1, (int) (width * scale));
        int newHeight = Math.max(1, (int) (height * scale));
        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    private void openSelectedProduct() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "請先選擇一個商品");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(selectedProduct.getUrl()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "無法開啟網址：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }

    static class HeartIcon implements Icon {
        private final boolean filled;
        HeartIcon(boolean filled) { this.filled = filled; }
        @Override public int getIconWidth() { return 15; }
        @Override public int getIconHeight() { return 14; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D.Double heart = new Path2D.Double();
            heart.moveTo(x + 7.5, y + 13);
            heart.curveTo(x + 1, y + 8, x + 0, y + 4, x + 3.5, y + 2);
            heart.curveTo(x + 5.5, y + 0.8, x + 7, y + 2, x + 7.5, y + 3.5);
            heart.curveTo(x + 8, y + 2, x + 9.5, y + 0.8, x + 11.5, y + 2);
            heart.curveTo(x + 15, y + 4, x + 14, y + 8, x + 7.5, y + 13);
            heart.closePath();
            if (filled) {
                g2.setColor(new Color(210, 65, 65));
                g2.fill(heart);
            } else {
                g2.setColor(new Color(128, 98, 70));
                g2.setStroke(new BasicStroke(1.8f));
                g2.draw(heart);
            }
            g2.dispose();
        }
    }

    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (getHgap() * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) rowWidth += getHgap();
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + getVgap() * 2;
                return dim;
            }
        }
        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }
}