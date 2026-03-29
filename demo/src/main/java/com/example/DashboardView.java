package com.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DashboardView extends JFrame {
    private ItemModel itemModel;
    private MainController controller;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public DashboardView(ItemModel itemModel, MainController controller) {
        this.itemModel = itemModel;
        this.controller = controller;
        buildUI();
        refreshTable();
    }

    // ✅ Custom rounded button with shadow + hover
    private JButton makeRoundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 18;

                // เงา
                g2.setColor(new Color(0, 0, 0, hovered ? 60 : 35));
                g2.fill(new RoundRectangle2D.Float(3, 4, w - 6, h - 4, arc, arc));

                // พื้นหลัง
                g2.setColor(hovered ? bg.darker() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 4, arc, arc));

                // ไฮไลท์ขอบบน
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 5, h - 6, arc, arc));

                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void paintBorder(Graphics g) {}
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 14, 22));
        btn.setOpaque(false);
        return btn;
    }

    // ✅ Custom rounded search wrapper with shadow + hover + focus effect
    private JPanel makeSearchBox() {
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 10));

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
        searchIcon.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        // คลิกที่ icon → focus ที่ field
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { searchField.requestFocusInWindow(); }
        });

        // wrapper วาด border + เงาเอง
        JPanel wrapper = new JPanel(new BorderLayout()) {
            boolean hovered = false;
            boolean focused = false;

            {
                // hover
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
                // focus จาก searchField
                searchField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { focused = true;  repaint(); }
                    public void focusLost (FocusEvent e) { focused = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 18;
                int shadowAlpha = focused ? 80 : hovered ? 55 : 25;

                // เงา
                g2.setColor(new Color(0, 0, 0, shadowAlpha));
                g2.fill(new RoundRectangle2D.Float(3, 4, w - 6, h - 4, arc, arc));

                // พื้นขาว
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 4, arc, arc));

                // border — น้ำเงินเมื่อ focus, ฟ้าอ่อนเมื่อ hover, เทาเมื่อปกติ
                Color borderColor = focused
                    ? new Color(37, 99, 235)
                    : hovered
                        ? new Color(96, 165, 250)
                        : new Color(203, 213, 225);
                float borderWidth = focused ? 2f : 1.5f;
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.draw(new RoundRectangle2D.Float(
                    borderWidth / 2, borderWidth / 2,
                    w - 3 - borderWidth, h - 4 - borderWidth,
                    arc, arc
                ));

                g2.dispose();
            }
        };

        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(60, 38));
        wrapper.setPreferredSize(new Dimension(320, 38));
        // padding เผื่อเงาล่าง
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 3));
        wrapper.add(searchIcon, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        return wrapper;
    }

    private void buildUI() {
        setTitle("MyStock Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.decode("#cdd0d5"));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(480, 360));

        // ─── Header ───────────────────────────────────────────
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(Color.decode("#091A36"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridy = 0;
        hgbc.anchor = GridBagConstraints.CENTER;

        ShadowLabel titleLabel = new ShadowLabel("Inventory");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        hgbc.gridx = 0; hgbc.weightx = 0;
        hgbc.fill = GridBagConstraints.NONE;
        hgbc.insets = new Insets(0, 0, 0, 12);
        headerPanel.add(titleLabel, hgbc);

        // ✅ search box โค้งมน + เงา + hover/focus
        JPanel searchWrapper = makeSearchBox();

        hgbc.gridx = 1; hgbc.weightx = 1;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.insets = new Insets(0, 0, 0, 12);
        headerPanel.add(searchWrapper, hgbc);

        JButton addBtn = makeRoundedButton("Add item", 
                                                Color.decode("#cdd0d5"), //สีปุ่ม
                                                Color.decode("#000000")); //สีตัวหนังสือ
        addBtn.addActionListener(e -> controller.openAddItemView());

        hgbc.gridx = 2; hgbc.weightx = 0;
        hgbc.fill = GridBagConstraints.NONE;
        hgbc.insets = new Insets(0, 0, 0, 0);
        headerPanel.add(addBtn, hgbc);

        add(headerPanel, BorderLayout.NORTH);

        // ─── Table ────────────────────────────────────────────
        String[] columns = {"Name", "Quantity", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setSelectionBackground(Color.decode("#abbed7")); 
        table.setSelectionForeground(Color.decode("#000000"));
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#cdd0d5"));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.decode("#6a7686")); //สีหัวตาราง
        header.setForeground(Color.decode("#ffffff")); //สีตัวหนังสือ ??
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String selectedName = (String) tableModel.getValueAt(row, 0);
                        Item selected = itemModel.getItems().stream()
                            .filter(i -> i.getName().equals(selectedName))
                            .findFirst().orElse(null);
                        if (selected != null) controller.openDetailView(selected);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ─── Bottom toolbar ───────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));

        JButton deleteBtn = makeRoundedButton("Delete Item", 
                                                    Color.decode("#9e0000"), //สีปุ่ม
                                                    Color.decode("#ffffff")); //สีตัวหนังสือ
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String selectedName = (String) tableModel.getValueAt(row, 0);
                Item selected = itemModel.getItems().stream()
                    .filter(i -> i.getName().equals(selectedName))
                    .findFirst().orElse(null);
                if (selected != null) controller.deleteItem(selected);
            } else {
                JOptionPane.showMessageDialog(this, "Please select item to delete", "Notification", JOptionPane.WARNING_MESSAGE);
            }
        });

        JLabel hint = new JLabel("double click for edit");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(107, 114, 128));

        bottomPanel.add(hint);
        bottomPanel.add(deleteBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        if (searchField != null && !searchField.getText().isBlank()) {
            filterTable();
        } else {
            tableModel.setRowCount(0);
            for (Item item : itemModel.getItems()) {
                tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory()
                });
            }
        }
    }

    private void filterTable() {
        String keyword = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (Item item : itemModel.getItems()) {
            if (keyword.isEmpty()
                    || item.getName().toLowerCase().contains(keyword)
                    || item.getCategory().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory()
                });
            }
        }
    }
}
