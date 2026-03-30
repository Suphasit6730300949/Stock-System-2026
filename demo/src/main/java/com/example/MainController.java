package com.example;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Controller: ตัวกลางระหว่าง View ทั้งหมดกับ ItemModel
 *
 * ความรับผิดชอบ:
 *  - เปิด/ปิด View ต่าง ๆ (Dashboard, DetailView, ItemDetailWindow)
 *  - รับ event จาก View แล้วสั่ง Model ทำงาน
 *  - สั่ง View refresh หลังข้อมูลเปลี่ยน
 */
public class MainController {

    private final ItemModel    itemModel;
    private       DashboardView dashboardView;

    /** รูปแบบวันที่แสดงใน ItemDetailWindow */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    public MainController(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /** สร้างและแสดง DashboardView (เรียกครั้งเดียวตอน start) */
    public void start() {
        dashboardView = new DashboardView(itemModel, this);
        dashboardView.setVisible(true);
    }

    // ── Dashboard callbacks ──────────────────────────────────────────────────

    /** ลบ Item — ขอยืนยันก่อนเสมอ */
    public void deleteItem(Item item) {
        int confirm = JOptionPane.showConfirmDialog(
                dashboardView,
                "Delete \"" + item.getName() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            itemModel.deleteItem(item);
            dashboardView.refreshTable();
        }
    }

    /** เปิด Edit dialog (modal) */
    public void openDetailView(Item item) {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(item);
    }

    /** เปิด Add Item dialog (modal) */
    public void openAddItemView() {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(null); // null = Add mode
    }

    /** เปิด Read-Only detail window (non-modal, เปิดได้หลายหน้าต่าง) */
    public void openItemDetailWindow(Item item) {
        JFrame win = buildDetailWindow(item);
        win.setVisible(true);
    }

    /** เรียกจาก DetailView หลัง save สำเร็จ เพื่อ refresh ตาราง */
    public void onSave() {
        dashboardView.refreshTable();
    }

    // ── Getter ───────────────────────────────────────────────────────────────

    public ItemModel getItemModel() {
        return itemModel;
    }

    // ── Private: สร้าง ItemDetailWindow ─────────────────────────────────────

    /**
     * สร้าง JFrame แบบ read-only สำหรับดูรายละเอียด Item
     * แยกออกมาเป็น method เพื่อให้ openItemDetailWindow() อ่านง่าย
     */
    private JFrame buildDetailWindow(Item item) {
        JFrame win = new JFrame("Detail: " + item.getName());
        win.setSize(460, 520);
        win.setLocationRelativeTo(dashboardView);
        // เพิ่ม offset เล็กน้อยเมื่อเปิดหลายหน้าต่างพร้อมกัน
        win.setLocation(
                win.getX() + (int) (Math.random() * 60) - 30,
                win.getY() + (int) (Math.random() * 60) - 30);
        win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        win.setLayout(new BorderLayout());
        win.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

        win.add(buildDetailHeader(item),        BorderLayout.NORTH);
        win.add(buildDetailBody(item),          BorderLayout.CENTER);
        win.add(buildDetailFooter(item, win),   BorderLayout.SOUTH);
        return win;
    }

    // ── Detail Window: Header ────────────────────────────────────────────────

    private JPanel buildDetailHeader(Item item) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient: dark teal → slightly lighter
                g2.setPaint(new java.awt.GradientPaint(
                        0, 0, Color.decode("#0a3d4a"),
                        getWidth(), getHeight(), Color.decode("#0e5568")));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));

        // category badge (ซ้ายบน)
        JLabel catBadge = new JLabel("  " + item.getCategory() + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        catBadge.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        catBadge.setForeground(new Color(180, 220, 230));
        catBadge.setOpaque(false);
        catBadge.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        // ชื่อ item
        JLabel titleLbl = new JLabel(item.getName());
        titleLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);

        // price chip (ขวาบน) — แสดงมูลค่ารวม (price × quantity)
        JLabel priceChip = new JLabel("(THB) " + String.format("%,.2f", item.getPrice() * item.getQuantity())) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        priceChip.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        priceChip.setForeground(new Color(255, 230, 100));
        priceChip.setOpaque(false);
        priceChip.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setOpaque(false);
        headerTop.add(catBadge,  BorderLayout.WEST);
        headerTop.add(priceChip, BorderLayout.EAST);

        JPanel headerMain = new JPanel(new BorderLayout(0, 6));
        headerMain.setOpaque(false);
        headerMain.add(headerTop, BorderLayout.NORTH);
        headerMain.add(titleLbl,  BorderLayout.CENTER);

        header.add(headerMain, BorderLayout.CENTER);
        return header;
    }

    // ── Detail Window: Body ──────────────────────────────────────────────────

    private JScrollPane buildDetailBody(Item item) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.decode("#f8fafc"));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // stat cards — Quantity และ Price แบบ side-by-side
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        statsRow.add(makeStatCard("Quantity",    String.valueOf(item.getQuantity())));
        statsRow.add(makeStatCard("Price (THB)", String.format("%,.2f", item.getPrice())));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(16));

        // description (แสดงเฉพาะเมื่อมีข้อความ)
        String desc = item.getDescription();
        if (desc != null && !desc.isBlank()) {
            body.add(buildDescCard(desc));
            body.add(Box.createVerticalStrut(16));
        }

        // divider
        body.add(makeDivider());

        // timestamp — Added
        String createdText = (item.getCreatedAt() != null)
                ? item.getCreatedAt().format(DATE_FMT) : "—";
        body.add(makeInfoRow("Added", createdText));

        // timestamp — Last edited (แสดงเฉพาะเมื่อเคยแก้ไข)
        if (item.getUpdatedAt() != null) {
            JPanel editedRow = makeInfoRow("Last edited", item.getUpdatedAt().format(DATE_FMT));
            // override สีค่า เพื่อบ่งบอกว่าเคยถูกแก้ไข
            for (Component c : editedRow.getComponents()) {
                if (c instanceof JLabel lbl && lbl.getFont().getStyle() == Font.PLAIN) {
                    lbl.setForeground(Color.decode("#b45309"));
                }
            }
            body.add(editedRow);
        }

        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.decode("#f8fafc"));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ── Detail Window: Footer ────────────────────────────────────────────────

    private JPanel buildDetailFooter(Item item, JFrame win) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setOpaque(false);

        JButton editBtn  = makeDetailWindowBtn("✏  Edit",  Color.decode("#004e86"), Color.WHITE);
        JButton closeBtn = makeDetailWindowBtn("Close",     Color.decode("#e5e7eb"), Color.decode("#374151"));

        editBtn.addActionListener(e  -> { win.dispose(); openDetailView(item); });
        closeBtn.addActionListener(e -> win.dispose());

        btnRow.add(editBtn);
        btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    // ── Detail Window: helper UI builders ───────────────────────────────────

    /** Stat card: label เล็กด้านบน, ค่า bold ด้านล่าง */
    private JPanel makeStatCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.decode("#e5e7eb"));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        lbl.setForeground(Color.decode("#9ca3af"));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        val.setForeground(Color.decode("#111827"));

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    /** Description card พื้นหลังสีเหลืองอ่อน */
    private JPanel buildDescCard(String desc) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#fffbeb"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.decode("#fde68a"));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = new JLabel("DESCRIPTION");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        title.setForeground(Color.decode("#92400e"));

        JTextArea area = new JTextArea(desc);
        area.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        area.setForeground(Color.decode("#374151"));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setCursor(Cursor.getDefaultCursor());
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());

        card.add(title, BorderLayout.NORTH);
        card.add(area,  BorderLayout.CENTER);
        return card;
    }

    /** แถว label–value แนวนอน (ใช้สำหรับ timestamp) */
    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#6b7280"));
        lbl.setPreferredSize(new Dimension(90, 20));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        val.setForeground(Color.decode("#111827"));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    /** เส้นแบ่ง (thin horizontal divider) */
    private JPanel makeDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Color.decode("#e5e7eb"));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        d.setPreferredSize(new Dimension(0, 10));
        return d;
    }

    /** Button สำหรับ footer ของ ItemDetailWindow */
    private JButton makeDetailWindowBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            {
                setContentAreaFilled(false);
                setOpaque(false);
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        return btn;
    }
}