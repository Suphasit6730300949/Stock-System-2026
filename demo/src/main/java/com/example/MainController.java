package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

public class MainController {
    private ItemModel itemModel;
    private DashboardView dashboardView;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    public MainController(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    public void start() {
        dashboardView = new DashboardView(itemModel, this);
        dashboardView.setVisible(true);
    }

    // Called when DashboardView requests delete
    public void deleteItem(Item item) {
        int confirm = JOptionPane.showConfirmDialog(
                dashboardView,
                "delete " + item.getName() + " sure?",
                "delete confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            itemModel.deleteItem(item);
            dashboardView.refreshTable();
        }
    }

    // Double-click → open read-only detail window (non-modal, multiple allowed)
    public void openItemDetailWindow(Item item) {
        JFrame detailWindow = new JFrame("Detail: " + item.getName());
        detailWindow.setSize(460, 520);
        detailWindow.setLocationRelativeTo(dashboardView);
        detailWindow.setLocation(
                detailWindow.getX() + (int) (Math.random() * 60) - 30,
                detailWindow.getY() + (int) (Math.random() * 60) - 30);
        detailWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailWindow.setLayout(new BorderLayout());
        detailWindow.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

        // ── HEADER ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient: dark teal → slightly lighter
                GradientPaint gp = new GradientPaint(
                        0, 0, Color.decode("#0a3d4a"),
                        getWidth(), getHeight(), Color.decode("#0e5568"));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));

        // category badge (top-left, above name)
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

        JLabel titleLbl = new JLabel(item.getName());
        titleLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);

        // price chip (top-right)
        JLabel priceChip = new JLabel("(THB) " + String.format("%,.2f", item.getPrice()*item.getQuantity())) {
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
        headerTop.add(catBadge, BorderLayout.WEST);
        headerTop.add(priceChip, BorderLayout.EAST);

        JPanel headerMain = new JPanel(new BorderLayout(0, 6));
        headerMain.setOpaque(false);
        headerMain.add(headerTop, BorderLayout.NORTH);
        headerMain.add(titleLbl, BorderLayout.CENTER);

        header.add(headerMain, BorderLayout.CENTER);
        detailWindow.add(header, BorderLayout.NORTH);

        // ── BODY ────────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.decode("#f8fafc"));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // helper: thin divider
        java.util.function.Supplier<JPanel> divider = () -> {
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
        };

        // helper: stat card (Quantity / Price side-by-side)
        java.util.function.BiFunction<String, String, JPanel> statCard = (label, value) -> {
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
        };

        // helper: row (label + value, full width)
        java.util.function.BiFunction<String, String, JPanel> infoRow = (label, value) -> {
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
        };

        // ── stat cards row (Qty + Price) ─────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        statsRow.add(statCard.apply("Quantity", String.valueOf(item.getQuantity())));
        statsRow.add(statCard.apply("Price (THB)", String.format("%,.2f", item.getPrice())));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(16));

        // ── Description block ────────────────────────────────────────────
        String desc = item.getDescription();
        if (desc != null && !desc.isBlank()) {
            JPanel descCard = new JPanel(new BorderLayout(0, 8)) {
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
            descCard.setOpaque(false);
            descCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            descCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel descTitle = new JLabel("DESCRIPTION");
            descTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
            descTitle.setForeground(Color.decode("#92400e"));

            JTextArea descArea = new JTextArea(desc);
            descArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            descArea.setForeground(Color.decode("#374151"));
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setEditable(false);
            descArea.setFocusable(false);
            descArea.setCursor(Cursor.getDefaultCursor());
            descArea.setOpaque(false);
            descArea.setBorder(BorderFactory.createEmptyBorder());

            descCard.add(descTitle, BorderLayout.NORTH);
            descCard.add(descArea, BorderLayout.CENTER);
            body.add(descCard);
            body.add(Box.createVerticalStrut(16));
        }

        // ── divider ──────────────────────────────────────────────────────
        body.add(divider.get());

        // ── timestamp rows ───────────────────────────────────────────────
        String createdText = (item.getCreatedAt() != null) ? item.getCreatedAt().format(DATE_FMT) : "—";
        body.add(infoRow.apply("Added", createdText));

        if (item.getUpdatedAt() != null) {
            JPanel editedRow = new JPanel(new BorderLayout(16, 0));
            editedRow.setOpaque(false);
            editedRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            editedRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

            JLabel editedLbl = new JLabel("Last edited");
            editedLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
            editedLbl.setForeground(Color.decode("#6b7280"));
            editedLbl.setPreferredSize(new Dimension(90, 20));

            JLabel editedVal = new JLabel(item.getUpdatedAt().format(DATE_FMT));
            editedVal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            editedVal.setForeground(Color.decode("#b45309"));

            editedRow.add(editedLbl, BorderLayout.WEST);
            editedRow.add(editedVal, BorderLayout.CENTER);
            body.add(editedRow);
        }

        // filler to push content up
        body.add(Box.createVerticalGlue());

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setBackground(Color.decode("#f8fafc"));
        bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailWindow.add(bodyScroll, BorderLayout.CENTER);

        // ── FOOTER ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setOpaque(false);

        // Edit button
        JButton editBtn = new JButton("✏  Edit") {
            boolean hov = false;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Color.decode("#003f70") : Color.decode("#004e86"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void paintBorder(Graphics g) {}
        };
        editBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        editBtn.setForeground(Color.WHITE);
        editBtn.setContentAreaFilled(false);
        editBtn.setFocusPainted(false);
        editBtn.setBorderPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        editBtn.setOpaque(false);
        editBtn.addActionListener(e -> { detailWindow.dispose(); openDetailView(item); });

        // Close button
        JButton closeBtn = new JButton("Close") {
            boolean hov = false;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? Color.decode("#d1d5db") : Color.decode("#e5e7eb"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void paintBorder(Graphics g) {}
        };
        closeBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        closeBtn.setForeground(Color.decode("#374151"));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        closeBtn.setOpaque(false);
        closeBtn.addActionListener(e -> detailWindow.dispose());

        btnRow.add(editBtn);
        btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        detailWindow.add(footer, BorderLayout.SOUTH);

        detailWindow.setVisible(true);
    }

    // Right-click Edit → open edit dialog (modal)
    public void openDetailView(Item item) {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(item);
    }

    // Called after Save in DetailView
    public void onSave() {
        dashboardView.refreshTable();
    }

    // Open add item dialog
    public void openAddItemView() {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(null);
    }

    public ItemModel getItemModel() {
        return itemModel;
    }
}