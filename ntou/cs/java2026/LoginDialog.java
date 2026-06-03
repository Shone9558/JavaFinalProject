package ntou.cs.java2026;

import ntou.cs.java2026.db.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel("請登入或註冊新帳號");
    private boolean loginSuccess = false;
    private int userId = -1;
    private String username = "";

    public LoginDialog(Frame owner) {
        super(owner, "使用者登入", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setResizable(false);
        setLocationRelativeTo(owner);
        initLayout();
    }

    private void initLayout() {
        Font base = new Font("Microsoft JhengHei", Font.PLAIN, 14);
        Font titleFont = new Font("Microsoft JhengHei", Font.BOLD, 22);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", new Font("Microsoft JhengHei", Font.BOLD, 14));
        UIManager.put("TextField.font", base);
        UIManager.put("PasswordField.font", base);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        root.setBackground(new Color(247, 242, 234));
        setContentPane(root);

        JLabel title = new JLabel("智慧購物比價追蹤器");
        title.setFont(titleFont);
        title.setForeground(new Color(88, 65, 45));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
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
        messageLabel.setForeground(new Color(125, 113, 100));
        form.add(messageLabel, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton registerButton = new JButton("註冊");
        JButton loginButton = new JButton("登入");
        JButton guestButton = new JButton("訪客進入");
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
