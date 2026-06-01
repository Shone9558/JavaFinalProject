package ntou.cs.java2026;

import ntou.cs.java2026.crawler.BooksCrawler;
import ntou.cs.java2026.crawler.MomoCrawler;
import ntou.cs.java2026.crawler.PChomeCrawler;
import ntou.cs.java2026.model.Product;
import ntou.cs.java2026.manager.FavoriteManager;
import ntou.cs.java2026.manager.HistoryManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainUI extends JFrame {

    private final JTextField keywordField = new JTextField();
    private final JCheckBox booksCheck = new JCheckBox("博客來", true);
    private final JCheckBox pchomeCheck = new JCheckBox("PChome", true);
    private final JCheckBox momoCheck = new JCheckBox("momo", true);
    private final JComboBox<String> sortBox = new JComboBox<>(new String[]{"價格由低到高", "價格由高到低", "平台名稱", "商品名稱"});
    private final JTextField minPriceField = new JTextField();
    private final JTextField maxPriceField = new JTextField();
    private final JButton searchButton = new JButton("搜尋商品");
    private final JButton favoriteButton = new JButton("加入收藏");
    private final JButton deleteFavoriteButton = new JButton("刪除收藏");
    private final JButton openButton = new JButton("開啟商品頁");
    private final JButton useHistoryButton = new JButton("用此關鍵字搜尋");
    private final JButton deleteHistoryButton = new JButton("刪除搜尋歷史");
    private final JButton clearHistoryButton = new JButton("清空搜尋歷史");
    private final JLabel statusLabel = new JLabel("請輸入關鍵字開始搜尋");

    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyListModel);

    private final DefaultTableModel resultModel = new DefaultTableModel(new String[]{"平台", "商品名稱", "價格", "網址"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel favoriteModel = new DefaultTableModel(new String[]{"平台", "商品名稱", "價格", "網址"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable resultTable = new JTable(resultModel);
    private final JTable favoriteTable = new JTable(favoriteModel);

    private final List<Product> allProducts = new ArrayList<>();
    private final FavoriteManager favoriteManager = new FavoriteManager();
    private final List<Product> favoriteProducts = favoriteManager.getFavoriteProducts();
    private final HistoryManager historyManager = new HistoryManager();
    private final List<String> searchHistory = historyManager.getSearchHistory();

    public MainUI() {
        setTitle("智慧購物比價追蹤器");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLookAndFeel();
        initLayout();
        initEvents();
        refreshFavoriteTable();
        refreshHistoryList();
    }

    private void initLookAndFeel() {
        UIManager.put("Button.font", new Font("Microsoft JhengHei", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Microsoft JhengHei", Font.PLAIN, 14));
        UIManager.put("CheckBox.font", new Font("Microsoft JhengHei", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Microsoft JhengHei", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Microsoft JhengHei", Font.PLAIN, 13));
        UIManager.put("Table.rowHeight", 30);
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        JLabel title = new JLabel("智慧購物比價追蹤器");
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
        title.setForeground(new Color(35, 48, 68));

        JLabel subtitle = new JLabel("整合博客來、PChome、momo 商品搜尋，快速比較價格與收藏商品");
        subtitle.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        subtitle.setForeground(new Color(95, 105, 120));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel searchPanel = createCardPanel();
        searchPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        searchPanel.add(new JLabel("關鍵字"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        keywordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        searchPanel.add(keywordField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        stylePrimaryButton(searchButton);
        searchPanel.add(searchButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        searchPanel.add(new JLabel("平台"), gbc);
        JPanel platformPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        platformPanel.setOpaque(false);
        platformPanel.add(booksCheck);
        platformPanel.add(pchomeCheck);
        platformPanel.add(momoCheck);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        searchPanel.add(platformPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2;
        searchPanel.add(new JLabel("價格範圍"), gbc);
        JPanel pricePanel = new JPanel(new GridLayout(1, 4, 8, 0));
        pricePanel.setOpaque(false);
        pricePanel.add(minPriceField);
        pricePanel.add(new JLabel("到"));
        pricePanel.add(maxPriceField);
        pricePanel.add(sortBox);
        minPriceField.setToolTipText("最低價格，可留空");
        maxPriceField.setToolTipText("最高價格，可留空");
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        searchPanel.add(pricePanel, gbc);

        root.add(searchPanel, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));

        setupTable(resultTable);
        setupTable(favoriteTable);

        JPanel resultPanel = createTablePanel(resultTable);
        JPanel favoritePanel = createTablePanel(favoriteTable);
        JPanel historyPanel = createHistoryPanel();

        tabs.addTab("搜尋結果", resultPanel);
        tabs.addTab("我的收藏", favoritePanel);
        tabs.addTab("搜尋歷史", historyPanel);

        root.add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        styleSecondaryButton(favoriteButton);
        styleSecondaryButton(openButton);
        styleSecondaryButton(deleteFavoriteButton);
        buttonPanel.add(favoriteButton);
        buttonPanel.add(deleteFavoriteButton);
        buttonPanel.add(openButton);

        statusLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(85, 95, 110));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        root.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JPanel createTablePanel(JTable table) {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JLabel hintLabel = new JLabel("雙擊歷史關鍵字，或選取後按按鈕，即可重新搜尋");
        hintLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        hintLabel.setForeground(new Color(85, 95, 110));
        panel.add(hintLabel, BorderLayout.NORTH);

        historyList.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

    private void setupTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(34);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(450);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(420);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        header.setBackground(new Color(235, 239, 245));
        header.setForeground(new Color(35, 48, 68));
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(new Color(45, 105, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(45, 105, 255));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 105, 255)),
                new EmptyBorder(8, 16, 8, 16)
        ));
    }

    private void initEvents() {
        searchButton.addActionListener(e -> searchProducts());
        favoriteButton.addActionListener(e -> addFavorite());
        deleteFavoriteButton.addActionListener(e -> removeFavorite());
        openButton.addActionListener(e -> openSelectedProduct());
        useHistoryButton.addActionListener(e -> searchFromSelectedHistory());
        deleteHistoryButton.addActionListener(e -> deleteSelectedHistory());
        clearHistoryButton.addActionListener(e -> clearSearchHistory());

        MouseAdapter openByDoubleClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedProduct();
                }
            }
        };
        resultTable.addMouseListener(openByDoubleClick);
        favoriteTable.addMouseListener(openByDoubleClick);

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    searchFromSelectedHistory();
                }
            }
        });
    }

    private void searchProducts() {
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先輸入搜尋關鍵字", "輸入提醒", JOptionPane.WARNING_MESSAGE);
            keywordField.requestFocus();
            return;
        }

        if (!booksCheck.isSelected() && !pchomeCheck.isSelected() && !momoCheck.isSelected()) {
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

        searchButton.setEnabled(false);
        favoriteButton.setEnabled(false);
        openButton.setEnabled(false);
        deleteFavoriteButton.setEnabled(false);
        resultModel.setRowCount(0);
        allProducts.clear();
        statusLabel.setText("準備搜尋，請稍候...");
        historyManager.addHistory(keyword);
        refreshHistoryList();

        SwingWorker<List<Product>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() {
                List<Product> list = new ArrayList<>();

                if (booksCheck.isSelected()) {
                    publish("正在搜尋博客來...");
                    try {
                        list.addAll(new BooksCrawler().search(keyword));
                    } catch (Exception ex) {
                        publish("博客來搜尋失敗，已略過：" + ex.getMessage());
                    }
                }
                if (pchomeCheck.isSelected()) {
                    publish("正在搜尋 PChome...");
                    try {
                        list.addAll(new PChomeCrawler().search(keyword));
                    } catch (Exception ex) {
                        publish("PChome 搜尋失敗，已略過：" + ex.getMessage());
                    }
                }
                if (momoCheck.isSelected()) {
                    publish("正在搜尋 momo...");
                    try {
                        list.addAll(new MomoCrawler().search(keyword));
                    } catch (Exception ex) {
                        publish("momo 搜尋失敗，已略過：" + ex.getMessage());
                    }
                }

                publish("正在套用價格篩選與排序...");
                return applyFilterAndSort(list);
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    allProducts.addAll(get());
                    refreshResultTable();
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
                    searchButton.setEnabled(true);
                    favoriteButton.setEnabled(true);
                    openButton.setEnabled(true);
                    deleteFavoriteButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private List<Product> applyFilterAndSort(List<Product> list) {
        double min = parseOptionalPrice(minPriceField.getText());
        double max = parseOptionalPrice(maxPriceField.getText());

        List<Product> filtered = new ArrayList<>();
        for (Product product : list) {
            boolean matchMin = min == 0 || product.getPrice() >= min;
            boolean matchMax = max == 0 || product.getPrice() <= max;
            if (matchMin && matchMax) {
                filtered.add(product);
            }
        }

        String sort = (String) sortBox.getSelectedItem();
        if ("價格由高到低".equals(sort)) {
            filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        } else if ("平台名稱".equals(sort)) {
            filtered.sort(Comparator.comparing(Product::getPlatform));
        } else if ("商品名稱".equals(sort)) {
            filtered.sort(Comparator.comparing(Product::getName));
        } else {
            filtered.sort(Comparator.comparingDouble(Product::getPrice));
        }

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
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + "不能小於 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "請輸入數字，例如 100 或 1000");
        }
    }

    private void refreshResultTable() {
        resultModel.setRowCount(0);
        for (Product product : allProducts) {
            resultModel.addRow(new Object[]{
                    product.getPlatform(),
                    product.getName(),
                    String.format("NT$%.0f", product.getPrice()),
                    product.getUrl()
            });
        }
    }

    private void refreshFavoriteTable() {
        favoriteModel.setRowCount(0);
        for (Product product : favoriteProducts) {
            favoriteModel.addRow(new Object[]{
                    product.getPlatform(),
                    product.getName(),
                    String.format("NT$%.0f", product.getPrice()),
                    product.getUrl()
            });
        }
    }

    private void refreshHistoryList() {
        historyListModel.clear();
        for (int i = searchHistory.size() - 1; i >= 0; i--) {
            historyListModel.addElement(searchHistory.get(i));
        }
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

        int option = JOptionPane.showConfirmDialog(
                this,
                "確定要刪除這筆搜尋歷史？\n" + keyword,
                "刪除搜尋歷史",
                JOptionPane.YES_NO_OPTION
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

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

        int option = JOptionPane.showConfirmDialog(
                this,
                "確定要清空所有搜尋歷史？",
                "清空搜尋歷史",
                JOptionPane.YES_NO_OPTION
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        searchHistory.clear();
        historyManager.saveSearchHistory();
        refreshHistoryList();
        statusLabel.setText("已清空搜尋歷史");
    }

    private void addFavorite() {
        int row = resultTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "請先在搜尋結果中選擇商品");
            return;
        }

        Product selected = allProducts.get(row);
        for (Product product : favoriteProducts) {
            if (isSameProduct(product, selected)) {
                JOptionPane.showMessageDialog(this, "這個商品已經收藏過了，收藏清單不會重複加入", "重複收藏", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("此商品已在收藏中：" + selected.getName());
                return;
            }
        }

        favoriteProducts.add(selected);
        favoriteManager.saveFavoriteProducts();
        refreshFavoriteTable();
        statusLabel.setText("已加入收藏：" + selected.getName());
    }

    private boolean isSameProduct(Product a, Product b) {
        if (a == null || b == null) return false;
        if (a.getUrl() != null && b.getUrl() != null && !a.getUrl().isEmpty() && !b.getUrl().isEmpty()) {
            return a.getUrl().equals(b.getUrl());
        }
        return a.getPlatform().equals(b.getPlatform()) && a.getName().equals(b.getName());
    }

    private void removeFavorite() {
        int row = favoriteTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "請先在我的收藏中選擇要刪除的商品");
            return;
        }

        Product selected = favoriteProducts.get(row);
        int option = JOptionPane.showConfirmDialog(
                this,
                "確定要刪除收藏商品？\n" + selected.getName(),
                "刪除收藏",
                JOptionPane.YES_NO_OPTION
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        favoriteProducts.remove(row);
        favoriteManager.saveFavoriteProducts();
        refreshFavoriteTable();
        statusLabel.setText("已刪除收藏：" + selected.getName());
    }

    private void openSelectedProduct() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "請先選擇一個商品");
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(selected.getUrl()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "無法開啟網址：" + e.getMessage());
        }
    }

    private Product getSelectedProduct() {
        if (resultTable.getSelectedRow() >= 0) {
            return allProducts.get(resultTable.getSelectedRow());
        }
        if (favoriteTable.getSelectedRow() >= 0) {
            return favoriteProducts.get(favoriteTable.getSelectedRow());
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}
