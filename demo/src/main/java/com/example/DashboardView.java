package com.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardView extends JFrame {
    private ItemModel itemModel;
    private MainController controller;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // ── Design tokens ──────────────────────────────────────────────────────────
    private static final Color HDR_FROM    = Color.decode("#0a1628");
    private static final Color HDR_TO      = Color.decode("#0f2d4a");
    private static final Color ACCENT      = Color.decode("#3b82f6");
    private static final Color ACCENT_DARK = Color.decode("#1d4ed8");
    private static final Color DANGER      = Color.decode("#dc2626");
    private static final Color DANGER_DARK = Color.decode("#991b1b");
    private static final Color BG          = Color.decode("#f1f5f9");
    private static final Color TABLE_BG    = Color.WHITE;
    private static final Color ROW_ALT     = Color.decode("#f8fafc");
    private static final Color SEL_BG      = Color.decode("#dbeafe");
    private static final Color SEL_FG      = Color.decode("#1e3a5f");
    private static final Color HDR_TBL_BG  = Color.decode("#1e293b");
    private static final Color HDR_TBL_FG  = Color.decode("#94a3b8");
    private static final Color TEXT_MAIN   = Color.decode("#111827");
    private static final Color TEXT_MUTED  = Color.decode("#6b7280");
    private static final Color BORDER_CLR  = Color.decode("#e2e8f0");

    public DashboardView(ItemModel itemModel, MainController controller) {
        this.itemModel = itemModel;
        this.controller = controller;
        buildUI();
        refreshTable();
    }

    // ── Pill button with gradient + hover ──────────────────────────────────────
    private JButton makePillBtn(String text, Color from, Color to, Color fg) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { setContentAreaFilled(false); setOpaque(false);
              addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, hov ? 55 : 30));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 3, 20, 20);
                // gradient fill
                Color f = hov ? from.darker() : from;
                Color t = hov ? to.darker()   : to;
                g2.setPaint(new GradientPaint(0, 0, f, 0, getHeight(), t));
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, 20, 20);
                // top gloss
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 5, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 12, 22));
        btn.setOpaque(false);
        return btn;
    }

    // ── Search box ─────────────────────────────────────────────────────────────
    private JPanel makeSearchBox() {
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 10));
        searchField.setForeground(TEXT_MAIN);

        // placeholder
        searchField.setText("Search by name or category…");
        searchField.setForeground(new Color(160, 160, 160));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by name or category…")) {
                    searchField.setText(""); searchField.setForeground(TEXT_MAIN);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search by name or category…");
                    searchField.setForeground(new Color(160, 160, 160));
                }
            }
        });

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        searchIcon.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { searchField.requestFocusInWindow(); }
        });

        JPanel wrapper = new JPanel(new BorderLayout()) {
            boolean hov = false, foc = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hov = false; repaint(); }
              });
              searchField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { foc = true;  repaint(); }
                public void focusLost  (FocusEvent e) { foc = false; repaint(); }
              });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), arc = 20;
                // shadow
                g2.setColor(new Color(0, 0, 0, foc ? 70 : hov ? 45 : 20));
                g2.fill(new RoundRectangle2D.Float(3, 4, w - 6, h - 4, arc, arc));
                // fill
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 4, arc, arc));
                // border
                Color bc = foc ? ACCENT : hov ? Color.decode("#93c5fd") : Color.decode("#cbd5e1");
                float bw = foc ? 2f : 1.5f;
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(bw));
                g2.draw(new RoundRectangle2D.Float(bw / 2, bw / 2, w - 3 - bw, h - 4 - bw, arc, arc));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(340, 40));
        wrapper.setMinimumSize(new Dimension(80, 40));
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

    // ── Stat chip (shown in header bar below title) ────────────────────────────
    private JPanel makeStatChip(String value, String label, Color accent) {
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new BoxLayout(chip, BoxLayout.X_AXIS));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        valLbl.setForeground(accent);

        JLabel lblLbl = new JLabel("  " + label);
        lblLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        lblLbl.setForeground(new Color(255, 255, 255, 140));

        chip.add(valLbl);
        chip.add(lblLbl);
        return chip;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("MyStock — Inventory Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(600, 400));

        // ── HEADER ────────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, HDR_FROM, getWidth(), getHeight(), HDR_TO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);

        // top bar: logo + search + add button
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridy = 0;
        hgbc.anchor = GridBagConstraints.CENTER;

        // Logo / Title area
        JPanel logoArea = new JPanel(new BorderLayout(10, 0));
        logoArea.setOpaque(false);

        JLabel logoIcon = new JLabel("📦");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel logoText = new JPanel();
        logoText.setOpaque(false);
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        ShadowLabel titleLabel = new ShadowLabel("MyStock");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        JLabel subLabel = new JLabel("Inventory Manager");
        subLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        subLabel.setForeground(new Color(255, 255, 255, 120));
        logoText.add(titleLabel);
        logoText.add(subLabel);

        logoArea.add(logoIcon, BorderLayout.WEST);
        logoArea.add(logoText, BorderLayout.CENTER);

        hgbc.gridx = 0; hgbc.weightx = 0;
        hgbc.fill = GridBagConstraints.NONE;
        hgbc.insets = new Insets(0, 0, 0, 20);
        topBar.add(logoArea, hgbc);

        // Search
        JPanel searchWrapper = makeSearchBox();
        hgbc.gridx = 1; hgbc.weightx = 1;
        hgbc.fill = GridBagConstraints.HORIZONTAL;
        hgbc.insets = new Insets(0, 0, 0, 16);
        topBar.add(searchWrapper, hgbc);

        // Add button
        JButton addBtn = makePillBtn("+ Add Item", ACCENT, ACCENT_DARK, Color.WHITE);
        addBtn.addActionListener(e -> controller.openAddItemView());
        hgbc.gridx = 2; hgbc.weightx = 0;
        hgbc.fill = GridBagConstraints.NONE;
        hgbc.insets = new Insets(0, 0, 0, 0);
        topBar.add(addBtn, hgbc);

        headerPanel.add(topBar, BorderLayout.NORTH);

        // stat bar
        JPanel statBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statBar.setOpaque(false);
        statBar.setBorder(BorderFactory.createEmptyBorder(0, 24, 14, 24));

        int totalItems = itemModel.getItems().size();
        int totalQty   = itemModel.getItems().stream().mapToInt(Item::getQuantity).sum();
        double totalVal = itemModel.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        int totalCats  = itemModel.getCategories().size();

        statBar.add(makeStatChip(String.valueOf(totalItems), "items",          new Color(147, 197, 253)));
        statBar.add(makeStatChip(String.valueOf(totalQty),   "units in stock", new Color(110, 231, 183)));
        statBar.add(makeStatChip(String.format("%,.0f(THB)", totalVal), "total value", new Color(253, 224, 71)));
        statBar.add(makeStatChip(String.valueOf(totalCats),  "categories",     new Color(216, 180, 254)));

        headerPanel.add(statBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ── TABLE CARD ────────────────────────────────────────────────────────
        String[] columns = {"  Name", "Quantity", "Category", "Price (THB)"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel) {
            // zebra stripe
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        };
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        table.setRowHeight(42);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_CLR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        // column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);

        // right-align Qty and Price
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setForeground(sel ? SEL_FG : Color.decode("#059669"));
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                return this;
            }
        });

        // category badge renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(v != null ? "  " + v + "  " : "") {
                    @Override protected void paintComponent(Graphics g) {
                        if (!table.isRowSelected(row)) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(Color.decode("#e0f2fe"));
                            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 8, 10, 10);
                            g2.dispose();
                        }
                        super.paintComponent(g);
                    }
                };
                lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
                lbl.setForeground(sel ? SEL_FG : Color.decode("#0369a1"));
                lbl.setBackground(sel ? SEL_BG : (row % 2 == 0 ? TABLE_BG : ROW_ALT));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                return lbl;
            }
        });

        // styled table header
        JTableHeader tblHeader = table.getTableHeader();
        tblHeader.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        tblHeader.setBackground(HDR_TBL_BG);
        tblHeader.setForeground(HDR_TBL_FG);
        tblHeader.setPreferredSize(new Dimension(0, 44));
        tblHeader.setReorderingAllowed(false);
        tblHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(v != null ? v.toString().toUpperCase() : "");
                lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
                lbl.setForeground(HDR_TBL_FG);
                lbl.setBackground(HDR_TBL_BG);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, col == 0 ? 16 : 8, 0, 8));
                lbl.setHorizontalAlignment(col >= 1 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return lbl;
            }
        });

        // mouse: double-click + right-click popup
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && row >= 0) {
                    getSelectedItem(row).ifPresent(controller::openItemDetailWindow);
                }
            }
            public void mousePressed (MouseEvent e) { showPopup(e); }
            public void mouseReleased(MouseEvent e) { showPopup(e); }
            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                table.setRowSelectionInterval(row, row);
                getSelectedItem(row).ifPresent(selected -> {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setBorder(BorderFactory.createLineBorder(BORDER_CLR));

                    JMenuItem editItem = new JMenuItem("✏  Edit");
                    editItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    editItem.addActionListener(ev -> controller.openDetailView(selected));

                    JMenuItem viewItem = new JMenuItem("👁  View Details");
                    viewItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    viewItem.addActionListener(ev -> controller.openItemDetailWindow(selected));

                    JMenuItem deleteItem = new JMenuItem("🗑  Delete");
                    deleteItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    deleteItem.setForeground(DANGER);
                    deleteItem.addActionListener(ev -> controller.deleteItem(selected));

                    popup.add(viewItem);
                    popup.add(editItem);
                    popup.addSeparator();
                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                });
            }
        });

        // wrap table in a card panel with rounded border + drop shadow
        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 22));
                g2.fillRoundRect(6, 8, getWidth() - 10, getHeight() - 8, 16, 16);
                // white card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 4));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        tableCard.add(tblHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(BG);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));
        tableWrapper.add(tableCard, BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // ── FOOTER ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));

        JLabel hint = new JLabel("  ↕ double-click to view   ·   right-click for options");
        hint.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnArea.setOpaque(false);

        JButton deleteBtn = makePillBtn("🗑  Delete Selected", DANGER, DANGER_DARK, Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                getSelectedItem(row).ifPresent(controller::deleteItem);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnArea.add(deleteBtn);
        footer.add(hint, BorderLayout.WEST);
        footer.add(btnArea, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // ── Helper: get Item from selected table row ───────────────────────────────
    private java.util.Optional<Item> getSelectedItem(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return java.util.Optional.empty();
        String name = tableModel.getValueAt(row, 0).toString().trim();
        return itemModel.getItems().stream().filter(i -> i.getName().equals(name)).findFirst();
    }

    // ── Refresh / filter ──────────────────────────────────────────────────────
    public void refreshTable() {
        String kw = (searchField != null) ? searchField.getText().trim() : "";
        boolean isPlaceholder = kw.equals("Search by name or category…");
        if (!kw.isBlank() && !isPlaceholder) {
            filterTable();
        } else {
            tableModel.setRowCount(0);
            for (Item item : itemModel.getItems()) {
                tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory(),
                    String.format("%.2f", item.getPrice())
                });
            }
        }
    }

    private void filterTable() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.equals("search by name or category…")) kw = "";
        tableModel.setRowCount(0);
        for (Item item : itemModel.getItems()) {
            if (kw.isEmpty()
                    || item.getName().toLowerCase().contains(kw)
                    || item.getCategory().toLowerCase().contains(kw)) {
                tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory(),
                    String.format("%.2f", item.getPrice())
                });
            }
        }
    }
}
