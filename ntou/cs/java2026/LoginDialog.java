package ntou.cs.java2026;

import ntou.cs.java2026.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    private boolean loginSuccess = false;
    private int userId = -1;
    private String username = "";

    public LoginDialog(Frame owner) {
        super(owner, "使用者登入", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initLayout();

        pack();
        setMinimumSize(new Dimension(500, 320));
        setLocationRelativeTo(owner);
    }

    private void initLayout() {
        getContentPane().removeAll();

        Font base = new Font("Microsoft JhengHei", Font.PLAIN, 14);
        Font titleFont = new Font("Microsoft JhengHei", Font.BOLD, 22);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(new Color(247, 242, 234));
        setContentPane(root);

        JLabel title = new JLabel("智慧購物比價平台");
        title.setFont(titleFont);
        title.setForeground(new Color(88, 65, 45));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);
        messageLabel = new JLabel("請登入或註冊新帳號");

        usernameField.setFont(base);
        passwordField.setFont(base);
        usernameField.setPreferredSize(new Dimension(230, 28));
        passwordField.setPreferredSize(new Dimension(230, 28));
        messageLabel.setFont(base);
        messageLabel.setForeground(new Color(125, 113, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("帳號"), gbc);

        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("密碼"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(messageLabel, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        JButton guestButton = new JButton("訪客進入");
        JButton registerButton = new JButton("註冊");
        JButton loginButton = new JButton("登入");

        buttons.add(guestButton);
        buttons.add(registerButton);
        buttons.add(loginButton);

        root.add(buttons, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        guestButton.addActionListener(e -> enterAsGuest());
        passwordField.addActionListener(e -> login());
    }

    private void login() {
        String name = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!validateInput(name, password)) return;

        int id = DatabaseManager.loginUser(name, password);

        if (id > 0) {
            loginSuccess = true;
            userId = id;
            username = name;
            dispose();
        } else {
            messageLabel.setText("登入失敗：帳號或密碼錯誤");
            messageLabel.setForeground(new Color(170, 57, 57));
        }
    }

    private void register() {
        String name = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!validateInput(name, password)) return;

        if (DatabaseManager.usernameExists(name)) {
            messageLabel.setText("註冊失敗：這個帳號已經存在");
            messageLabel.setForeground(new Color(170, 57, 57));
            return;
        }

        int id = DatabaseManager.registerUser(name, password);

        if (id > 0) {
            loginSuccess = true;
            userId = id;
            username = name;
            JOptionPane.showMessageDialog(this, "註冊成功，已自動登入。", "註冊成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            messageLabel.setText("註冊失敗：請確認 SQLite jar 是否有加入 classpath");
            messageLabel.setForeground(new Color(170, 57, 57));
        }
    }

    private boolean validateInput(String name, String password) {
        if (name.isEmpty() || password.isEmpty()) {
            messageLabel.setText("帳號與密碼都不能空白");
            messageLabel.setForeground(new Color(170, 57, 57));
            return false;
        }

        if (name.length() < 3) {
            messageLabel.setText("帳號至少需要 3 個字元");
            messageLabel.setForeground(new Color(170, 57, 57));
            return false;
        }

        if (password.length() < 4) {
            messageLabel.setText("密碼至少需要 4 個字元");
            messageLabel.setForeground(new Color(170, 57, 57));
            return false;
        }

        return true;
    }

    private void enterAsGuest() {
        loginSuccess = true;
        userId = 0;
        username = "訪客";
        dispose();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}