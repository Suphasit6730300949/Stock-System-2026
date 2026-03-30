package com.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * View: หน้า Dashboard หลัก
 *
 * ความรับผิดชอบ:
 *  - แสดงตารางรายการ Items พร้อม zebra-stripe
 *  - Header: logo + searchbox + Add button + stat chips
 *  - Footer: Refresh / Sort / Delete buttons + hint bar
 *  - ส่ง event (click, double-click, right-click) ไปยัง Controller
 *
 * ไม่มี business logic — ทุกการตัดสินใจส่งไปที่ MainController
 */
public class DashboardView extends JFrame {

    // ── Dependencies ─────────────────────────────────────────────────────────
    private final ItemModel      itemModel;
    private final MainController controller;

    // ── Table components ──────────────────────────────────────────────────────
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         searchField;
    private JPanel             statBar; // อ้างอิงเพื่อ refresh stat chips

    // ── Design tokens (สีชุดเดียวกันทั้ง View) ───────────────────────────────
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

    // ── Constructor ──────────────────────────────────────────────────────────

    public DashboardView(ItemModel itemModel, MainController controller) {
        this.itemModel  = itemModel;
        this.controller = controller;
        buildUI();
        refreshTable();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUBLIC API (เรียกจาก Controller)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Refresh ตารางและ stat chips
     * ถ้า search field มีคำค้นอยู่ให้ filter ก่อนแสดง
     */
    public void refreshTable() {
        String  kw            = searchField != null ? searchField.getText().trim() : "";
        boolean isPlaceholder = kw.equals("Search by name or category…");

        if (!kw.isBlank() && !isPlaceholder) {
            filterTable();
        } else {
            populateTable(itemModel.getItems());
        }
        refreshStatBar();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        setTitle("MyStock — Inventory Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 400));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildTableCard(),    BorderLayout.CENTER);
        add(buildFooter(),       BorderLayout.SOUTH);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        // panel ที่วาด gradient เอง
        JPanel headerPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, HDR_FROM, getWidth(), getHeight(), HDR_TO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.add(buildTopBar(), BorderLayout.NORTH);
        headerPanel.add(buildStatBar(), BorderLayout.SOUTH);
        return headerPanel;
    }

    /** Top bar: [Logo]  [SearchBox ←stretch→]  [Add button] */
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy  = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Logo area
        gbc.gridx = 0; gbc.weightx = 0;
        gbc.fill  = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 20);
        topBar.add(buildLogoArea(), gbc);

        // Search (ยืดเต็ม)
        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 16);
        topBar.add(makeSearchBox(), gbc);

        // Add button
        JButton addBtn = makePillBtn("+ Add Item", ACCENT, ACCENT_DARK, Color.WHITE);
        addBtn.addActionListener(e -> controller.openAddItemView());
        gbc.gridx = 2; gbc.weightx = 0;
        gbc.fill  = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        topBar.add(addBtn, gbc);

        return topBar;
    }

    /** Logo: emoji icon + "MyStock" (ShadowLabel) + subtitle */
    private JPanel buildLogoArea() {
        JPanel logoArea = new JPanel(new BorderLayout(10, 0));
        logoArea.setOpaque(false);
        logoArea.setPreferredSize(new Dimension(240, 70));
        logoArea.setMinimumSize(new Dimension(200, 60));

        JLabel logoIcon = new JLabel("📦");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel logoText = new JPanel();
        logoText.setOpaque(false);
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));

        // ShadowLabel — font ต้อง set ก่อน setMaximumSize เพราะ getPreferredSize อิงจาก font ปัจจุบัน
        ShadowLabel titleLabel = new ShadowLabel("MyStock");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setMaximumSize(titleLabel.getPreferredSize()); // ป้องกัน BoxLayout ยืดเกิน

        JLabel subLabel = new JLabel("Inventory Manager");
        subLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        subLabel.setForeground(new Color(255, 255, 255, 120));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subLabel.setMaximumSize(subLabel.getPreferredSize());

        logoText.add(titleLabel);
        logoText.add(subLabel);

        logoArea.add(logoIcon, BorderLayout.WEST);
        logoArea.add(logoText, BorderLayout.CENTER);
        return logoArea;
    }

    /** Stat bar: row ของ chip แสดงสถิติ (items / units / value / categories) */
    private JPanel buildStatBar() {
        statBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statBar.setOpaque(false);
        statBar.setBorder(BorderFactory.createEmptyBorder(0, 24, 14, 24));
        refreshStatBar(); // เติมข้อมูลครั้งแรก
        return statBar;
    }

    /** อัปเดตค่าใน stat chips ทุกครั้งที่ข้อมูลเปลี่ยน */
    private void refreshStatBar() {
        if (statBar == null) return;
        statBar.removeAll();

        int    totalItems = itemModel.getItems().size();
        int    totalQty   = itemModel.getItems().stream().mapToInt(Item::getQuantity).sum();
        double totalVal   = itemModel.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        int    totalCats  = itemModel.getCategories().size();

        statBar.add(makeStatChip(String.valueOf(totalItems),                "items",          new Color(147, 197, 253)));
        statBar.add(makeStatChip(String.valueOf(totalQty),                  "units in stock", new Color(110, 231, 183)));
        statBar.add(makeStatChip(String.format("%,.0f (THB)", totalVal),    "total value",    new Color(253, 224, 71)));
        statBar.add(makeStatChip(String.valueOf(totalCats),                 "categories",     new Color(216, 180, 254)));

        statBar.revalidate();
        statBar.repaint();
    }

    // ── Table Card ────────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        buildTable(); // สร้าง this.table และ this.tableModel

        // header ของตาราง
        JTableHeader tblHeader = table.getTableHeader();
        tblHeader.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        tblHeader.setBackground(HDR_TBL_BG);
        tblHeader.setForeground(HDR_TBL_FG);
        tblHeader.setPreferredSize(new Dimension(0, 44));
        tblHeader.setReorderingAllowed(false);
        tblHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(v != null ? v.toString().toUpperCase() : "");
                lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
                lbl.setForeground(HDR_TBL_FG);
                lbl.setBackground(HDR_TBL_BG);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, col == 0 ? 16 : 8, 0, 8));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // card panel วาด rounded corner + drop shadow
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

        tableCard.add(tblHeader,  BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // wrapper เพิ่ม padding รอบ card
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(BG);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));
        tableWrapper.add(tableCard, BorderLayout.CENTER);
        return tableWrapper;
    }

    /** สร้าง JTable พร้อม model, renderer, mouse listener */
    private void buildTable() {
        String[] columns = { "  Name", "Quantity", "Category", "Price (THB)" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // zebra stripe override
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        };

        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
        table.setRowHeight(42);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_CLR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        // ความกว้างเริ่มต้นของแต่ละคอลัมน์
        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);

        applyColumnRenderers();
        attachTableMouseListener();
    }

    /** กำหนด cell renderer ของแต่ละคอลัมน์ */
    private void applyColumnRenderers() {
        // Quantity — center align
        DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
        centerAlign.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerAlign);

        // Price — center align + bold + สีเขียว
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.CENTER); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setForeground(sel ? SEL_FG : Color.decode("#059669"));
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                return this;
            }
        });

        // Category — rounded badge สีฟ้า
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
                lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setForeground(sel ? SEL_FG : Color.decode("#0369a1"));
                lbl.setBackground(sel ? SEL_BG : (row % 2 == 0 ? TABLE_BG : ROW_ALT));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                return lbl;
            }
        });
    }

    /** double-click → openItemDetailWindow, right-click → context menu */
    private void attachTableMouseListener() {
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
                // double-click เปิด read-only detail window
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && row >= 0) {
                    getSelectedItem(row).ifPresent(controller::openItemDetailWindow);
                }
            }
            @Override public void mousePressed (MouseEvent e) { showContextMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { showContextMenu(e); }

            private void showContextMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                table.setRowSelectionInterval(row, row);
                getSelectedItem(row).ifPresent(selected -> {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setBorder(BorderFactory.createLineBorder(BORDER_CLR));

                    JMenuItem viewItem   = new JMenuItem("👁  View Details");
                    JMenuItem editItem   = new JMenuItem("✏  Edit");
                    JMenuItem deleteItem = new JMenuItem("🗑  Delete");

                    viewItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    editItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    deleteItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                    deleteItem.setForeground(DANGER);

                    viewItem.addActionListener(ev   -> controller.openItemDetailWindow(selected));
                    editItem.addActionListener(ev   -> controller.openDetailView(selected));
                    deleteItem.addActionListener(ev -> controller.deleteItem(selected));

                    popup.add(viewItem);
                    popup.add(editItem);
                    popup.addSeparator();
                    popup.add(deleteItem);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                });
            }
        });
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));

        // hint ซ้าย
        JLabel hint = new JLabel("  ↕ double-click to view   ·   right-click for options");
        hint.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // button row ขวา
        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnArea.setOpaque(false);
        btnArea.add(buildRefreshButton());
        btnArea.add(buildSortButton());
        btnArea.add(buildDeleteButton());

        footer.add(hint,    BorderLayout.WEST);
        footer.add(btnArea, BorderLayout.EAST);
        return footer;
    }

    private JButton buildRefreshButton() {
        JButton btn = makePillBtn("↻  Refresh", Color.decode("#065f46"), Color.decode("#064e3b"), Color.WHITE);
        btn.addActionListener(e -> {
            itemModel.reloadFromDB();
            refreshTable();
        });
        return btn;
    }

    private JButton buildSortButton() {
        JButton sortBtn = makePillBtn("⇅  Sort", Color.decode("#374151"), Color.decode("#1f2937"), Color.WHITE);

        JPopupMenu sortMenu = new JPopupMenu();
        sortMenu.setBorder(BorderFactory.createLineBorder(BORDER_CLR));

        // label ของ sort option แต่ละรายการ
        String[] sortLabels = {
            "🔤  Name: A → Z",
            "🔤  Name: Z → A",
            "🔢  Quantity: Low → High",
            "🔢  Quantity: High → Low",
            "💰  Price: Low → High",
            "💰  Price: High → Low",
            "📅  Date Added: Newest First",
            "📅  Date Added: Oldest First",
            "⚠  Out of Stock"
        };
        for (String label : sortLabels) {
            JMenuItem mi = new JMenuItem(label);
            mi.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            mi.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            mi.setPreferredSize(new Dimension(260, 38));
            mi.addActionListener(e -> sortAndDisplay(label));
            sortMenu.add(mi);
        }
        // เปิด popup เหนือปุ่ม
        sortBtn.addActionListener(e ->
                sortMenu.show(sortBtn, 0, -sortMenu.getPreferredSize().height));
        return sortBtn;
    }

    private JButton buildDeleteButton() {
        JButton btn = makePillBtn("🗑  Delete Selected", DANGER, DANGER_DARK, Color.WHITE);
        btn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                getSelectedItem(row).ifPresent(controller::deleteItem);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select an item to delete.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        return btn;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    // ── Search ────────────────────────────────────────────────────────────────

    /** Search box ที่วาด rounded border เอง พร้อม placeholder และ focus effect */
    private JPanel makeSearchBox() {
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 10));
        searchField.setForeground(TEXT_MAIN);

        // placeholder behavior
        searchField.setText("Search by name or category…");
        searchField.setForeground(new Color(160, 160, 160));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by name or category…")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_MAIN);
                }
            }
            @Override public void focusLost(FocusEvent e) {
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
            @Override public void mouseClicked(MouseEvent e) { searchField.requestFocusInWindow(); }
        });

        // wrapper วาด rounded bg + border + shadow ตาม hover/focus state
        JPanel wrapper = new JPanel(new BorderLayout()) {
            boolean hov = false, foc = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
                searchField.addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { foc = true;  repaint(); }
                    @Override public void focusLost  (FocusEvent e) { foc = false; repaint(); }
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
                // border (สีเปลี่ยนตาม focus / hover)
                Color  bc = foc ? ACCENT : hov ? Color.decode("#93c5fd") : Color.decode("#cbd5e1");
                float  bw = foc ? 2f : 1.5f;
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
        wrapper.add(searchIcon,  BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);

        // filter เมื่อข้อความเปลี่ยน
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate (javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate (javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        return wrapper;
    }

    // ── Stat chip ─────────────────────────────────────────────────────────────

    /** Chip สีโปร่งใสบน header แสดง value + label */
    private JPanel makeStatChip(String value, String label, Color accent) {
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // พื้นหลังโปร่งใสเล็กน้อย
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // ขอบขาว
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

    // ── Pill button ───────────────────────────────────────────────────────────

    /** ปุ่ม pill shape พร้อม gradient + hover + gloss effect */
    private JButton makePillBtn(String text, Color from, Color to, Color fg) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            {
                setContentAreaFilled(false);
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, hov ? 55 : 30));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 3, 20, 20);
                // gradient fill (เข้มขึ้นเมื่อ hover)
                g2.setPaint(new GradientPaint(0, 0, hov ? from.darker() : from,
                                              0, getHeight(), hov ? to.darker() : to));
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
        return btn;
    }

    // ── Table helpers ─────────────────────────────────────────────────────────

    /** เติมข้อมูล items ลง tableModel */
    private void populateTable(List<Item> items) {
        tableModel.setRowCount(0);
        for (Item item : items) {
            tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory(),
                    String.format("%.2f", item.getPrice())
            });
        }
    }

    /** Filter ตามคำที่พิมพ์ใน search field (name หรือ category) */
    private void filterTable() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.equals("search by name or category…")) kw = "";
        final String keyword = kw;

        List<Item> filtered = itemModel.getItems().stream()
                .filter(i -> keyword.isEmpty()
                        || i.getName().toLowerCase().contains(keyword)
                        || i.getCategory().toLowerCase().contains(keyword))
                .toList();
        populateTable(filtered);
    }

    /** ดึง Item จาก row ที่เลือกใน tableModel */
    private Optional<Item> getSelectedItem(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return Optional.empty();
        String name = tableModel.getValueAt(row, 0).toString().trim();
        return itemModel.getItems().stream()
                .filter(i -> i.getName().equals(name))
                .findFirst();
    }

    // ── Sort ──────────────────────────────────────────────────────────────────

    /** Sort items ตาม label ที่เลือกจาก Sort menu แล้วแสดงในตาราง */
    private void sortAndDisplay(String mode) {
        List<Item> sorted = new ArrayList<>(itemModel.getItems());

        if      (mode.contains("Name") && mode.contains("A → Z"))
            sorted.sort(Comparator.comparing(i -> i.getName().toLowerCase()));
        else if (mode.contains("Name") && mode.contains("Z → A"))
            sorted.sort((a, b) -> b.getName().toLowerCase().compareTo(a.getName().toLowerCase()));
        else if (mode.contains("Quantity") && mode.contains("Low → High"))
            sorted.sort(Comparator.comparingInt(Item::getQuantity));
        else if (mode.contains("Quantity") && mode.contains("High → Low"))
            sorted.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
        else if (mode.contains("Price") && mode.contains("Low → High"))
            sorted.sort(Comparator.comparingDouble(Item::getPrice));
        else if (mode.contains("Price") && mode.contains("High → Low"))
            sorted.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        else if (mode.contains("Date Added") && mode.contains("Newest"))
            sorted.sort((a, b) -> compareNullSafe(b.getCreatedAt(), a.getCreatedAt()));
        else if (mode.contains("Date Added") && mode.contains("Oldest"))
            sorted.sort((a, b) -> compareNullSafe(a.getCreatedAt(), b.getCreatedAt()));
        else if (mode.contains("Out of Stock"))
            sorted.removeIf(i -> i.getQuantity() > 0);

        populateTable(sorted);
    }

    /** เปรียบเทียบ LocalDateTime แบบ null-safe (null จะถือว่ามาทีหลัง) */
    private int compareNullSafe(java.time.LocalDateTime a, java.time.LocalDateTime b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }
}