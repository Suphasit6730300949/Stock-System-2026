package com.example;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * View: Dialog สำหรับ Add Item และ Edit Item (modal)
 *
 * ความรับผิดชอบ:
 *  - แสดง form กรอกข้อมูล (name, description, quantity, price, category)
 *  - Validate ก่อน save (ชื่อห้ามว่าง, ห้ามซ้ำ)
 *  - ส่งข้อมูลที่ถูกต้องไปยัง Controller ผ่าน controller.onSave()
 *
 * เรียกใช้งานผ่าน showDetailView(item) — null = Add mode, non-null = Edit mode
 */
public class DetailView extends JDialog {

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final MainController controller;
    private       Item           currentItem; // null ถ้าเป็น Add mode

    // ── Form fields ───────────────────────────────────────────────────────────
    private JTextField         nameField;
    private JSpinner           quantitySpinner;
    private JComboBox<String>  categoryCombo;
    private JTextField         newCategoryField;
    private JSpinner           priceSpinner;
    private JTextArea          descriptionArea;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color BG          = Color.decode("#f8fafc");
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER_IDLE = Color.decode("#d1d5db");
    private static final Color BORDER_HOV  = Color.decode("#93c5fd");
    private static final Color BORDER_FOC  = Color.decode("#3b82f6");
    private static final Color LBL_COLOR   = Color.decode("#374151");
    private static final Color HINT_COLOR  = Color.decode("#9ca3af");

    // ── Constructor ──────────────────────────────────────────────────────────

    public DetailView(JFrame parent, MainController controller) {
        super(parent, true); // modal
        this.controller = controller;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * เปิด dialog พร้อมข้อมูล item
     * @param item null = Add mode, non-null = Edit mode
     */
    public void showDetailView(Item item) {
        this.currentItem = item;
        boolean isNew = (item == null);

        setTitle(isNew ? "Add Item" : "Edit Item");
        setSize(460, 620);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(item, isNew), BorderLayout.NORTH);
        add(buildBody(item, isNew),   BorderLayout.CENTER);
        add(buildFooter(isNew),       BorderLayout.SOUTH);

        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ════════════════════════════════════════════════════════════════════════

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader(Item item, boolean isNew) {
        Color hdrFrom = isNew ? Color.decode("#004e86") : Color.decode("#0a3d4a");
        Color hdrTo   = isNew ? Color.decode("#0369a1") : Color.decode("#0e5568");

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, hdrFrom, getWidth(), getHeight(), hdrTo));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));

        // icon วงกลมโปร่งใส
        JLabel iconLbl = new JLabel(isNew ? "+" : "✎") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        iconLbl.setForeground(new Color(255, 255, 255, 200));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(44, 44));
        iconLbl.setOpaque(false);

        JLabel titleLbl = new JLabel(isNew ? "Add New Item" : "Edit: " + item.getName());
        titleLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel(isNew ? "Fill in the details below" : "Update the item information");
        subtitleLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        subtitleLbl.setForeground(new Color(255, 255, 255, 160));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLbl,    BorderLayout.NORTH);
        titleBlock.add(subtitleLbl, BorderLayout.CENTER);

        JPanel inner = new JPanel(new BorderLayout(14, 0));
        inner.setOpaque(false);
        inner.add(iconLbl,    BorderLayout.WEST);
        inner.add(titleBlock, BorderLayout.CENTER);
        header.add(inner, BorderLayout.CENTER);
        return header;
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JScrollPane buildBody(Item item, boolean isNew) {
        Font fieldFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Name field + char counter
        body.add(buildNameCard(item, isNew));
        body.add(Box.createVerticalStrut(10));

        // Description textarea
        body.add(buildDescriptionCard(item, isNew, fieldFont));
        body.add(Box.createVerticalStrut(10));

        // Quantity + Price (2 columns)
        body.add(buildQtyPriceRow(item, isNew, fieldFont));
        body.add(Box.createVerticalStrut(10));

        // Category picker + Add new category
        body.add(buildCategoryCard(item, isNew, fieldFont));

        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    /** Card: Item Name + ตัวนับตัวอักษร (max 30) */
    private JPanel buildNameCard(Item item, boolean isNew) {
        nameField = makeStyledField(isNew ? "" : item.getName());
        // จำกัดความยาวไม่เกิน 30 ตัวอักษร
        nameField.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str != null && (getLength() + str.length()) <= 30)
                    super.insertString(offs, str, a);
            }
        });
        nameField.setText(isNew ? "" : item.getName());

        JLabel charCountLbl = new JLabel((isNew ? "0" : String.valueOf(item.getName().length())) + " / 30");
        charCountLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        charCountLbl.setForeground(HINT_COLOR);
        charCountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        charCountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // อัปเดต counter เมื่อพิมพ์
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                int len = nameField.getText().length();
                charCountLbl.setText(len + " / 30");
                charCountLbl.setForeground(len == 30 ? Color.decode("#dc2626") : HINT_COLOR);
            }
            @Override public void insertUpdate (javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate (javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JPanel nameFieldPanel = new JPanel(new BorderLayout(0, 2));
        nameFieldPanel.setOpaque(false);
        nameFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameFieldPanel.add(nameField,    BorderLayout.CENTER);
        nameFieldPanel.add(charCountLbl, BorderLayout.SOUTH);

        return makeCard(makeFieldLabel("ITEM NAME"), nameFieldPanel);
    }

    /** Card: Description textarea */
    private JPanel buildDescriptionCard(Item item, boolean isNew, Font fieldFont) {
        descriptionArea = new JTextArea(isNew ? "" : item.getDescription(), 3, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(WHITE);
        descriptionArea.setBorder(makeBorder(BORDER_IDLE));

        // border เปลี่ยนตาม focus / hover
        descriptionArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { descriptionArea.setBorder(makeBorder(BORDER_FOC));  }
            @Override public void focusLost  (FocusEvent e) { descriptionArea.setBorder(makeBorder(BORDER_IDLE)); }
        });
        descriptionArea.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (!descriptionArea.isFocusOwner()) descriptionArea.setBorder(makeBorder(BORDER_HOV));  }
            @Override public void mouseExited (MouseEvent e) { if (!descriptionArea.isFocusOwner()) descriptionArea.setBorder(makeBorder(BORDER_IDLE)); }
        });

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        return makeCard(makeFieldLabel("DESCRIPTION  (optional)"), descScroll);
    }

    /** Row 2 คอลัมน์: Quantity | Price */
    private JPanel buildQtyPriceRow(Item item, boolean isNew, Font fieldFont) {
        quantitySpinner = new JSpinner(new SpinnerNumberModel(isNew ? 0 : item.getQuantity(), 0, 99999, 1));
        quantitySpinner.setFont(fieldFont);
        fixSpinnerArrows(quantitySpinner);
        quantitySpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        priceSpinner = new JSpinner(new SpinnerNumberModel(isNew ? 0.0 : item.getPrice(), 0.0, 9999999.0, 0.01));
        priceSpinner.setFont(fieldFont);
        priceSpinner.setEditor(new JSpinner.NumberEditor(priceSpinner, "#,##0.00"));
        fixSpinnerArrows(priceSpinner);
        priceSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 10, 0));
        twoCol.setOpaque(false);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        twoCol.add(makeCard(makeFieldLabel("QUANTITY"),       quantitySpinner));
        twoCol.add(makeCard(makeFieldLabel("PRICE  (THB)"),   priceSpinner));
        return twoCol;
    }

    /** Card: dropdown เลือก category + ลบ category + เพิ่ม category ใหม่ */
    private JPanel buildCategoryCard(Item item, boolean isNew, Font fieldFont) {
        List<String> cats = controller.getItemModel().getCategories();
        categoryCombo = new JComboBox<>(cats.toArray(new String[0]));
        if (!isNew) categoryCombo.setSelectedItem(item.getCategory());
        categoryCombo.setFont(fieldFont);
        categoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ปุ่มลบ category ที่เลือก
        JButton removeCatBtn = makeSmallBtn("✕ Remove", Color.decode("#fee2e2"), Color.decode("#9e0000"));
        removeCatBtn.addActionListener(e -> {
            String sel = (String) categoryCombo.getSelectedItem();
            if (sel == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Remove category \"" + sel + "\" from list?",
                    "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                controller.getItemModel().removeCategory(sel);
                categoryCombo.removeItem(sel);
            }
        });

        JPanel catRow = new JPanel(new BorderLayout(8, 0));
        catRow.setOpaque(false);
        catRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        catRow.add(categoryCombo,  BorderLayout.CENTER);
        catRow.add(removeCatBtn,   BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(Color.decode("#e5e7eb"));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        newCategoryField = makeStyledField("");
        newCategoryField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        // ปุ่มเพิ่ม category ใหม่
        JButton addCatBtn = makeSmallBtn("+ Add", Color.decode("#dbeafe"), Color.decode("#1d4ed8"));
        addCatBtn.addActionListener(e -> {
            String newCat = newCategoryField.getText().trim();
            if (!newCat.isEmpty()) {
                controller.getItemModel().addCategory(newCat);
                categoryCombo.addItem(newCat);
                categoryCombo.setSelectedItem(newCat);
                newCategoryField.setText("");
            }
        });

        JPanel newCatRow = new JPanel(new BorderLayout(8, 0));
        newCatRow.setOpaque(false);
        newCatRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        newCatRow.add(newCategoryField, BorderLayout.CENTER);
        newCatRow.add(addCatBtn,        BorderLayout.EAST);

        JLabel newCatHint = new JLabel("Type a name and click + Add");
        newCatHint.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        newCatHint.setForeground(HINT_COLOR);
        newCatHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        return makeCard(
                makeFieldLabel("CATEGORY"),
                catRow,
                sep,
                makeFieldLabel("ADD NEW CATEGORY"),
                newCatRow,
                newCatHint);
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter(boolean isNew) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setOpaque(false);

        JButton cancelBtn = makePillBtn("Cancel", Color.decode("#e5e7eb"), LBL_COLOR);
        cancelBtn.addActionListener(e -> dispose());

        Color  saveColor = isNew ? Color.decode("#004e86") : Color.decode("#129469");
        JButton saveBtn  = makePillBtn(isNew ? "+ Add Item" : "Save Changes", saveColor, WHITE);
        saveBtn.addActionListener(e -> handleSave(isNew));

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SAVE LOGIC
    // ════════════════════════════════════════════════════════════════════════

    /** Validate และบันทึกข้อมูล ก่อนปิด dialog */
    private void handleSave(boolean isNew) {
        String name = nameField.getText().trim();

        // Validate: ชื่อต้องไม่ว่าง
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a product name.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate: ชื่อต้องไม่ซ้ำกับ item อื่น (เปรียบเทียบโดย ignore whitespace + case)
        boolean isDuplicate = controller.getItemModel().getItems().stream()
                .anyMatch(i -> i.getName().replaceAll("\\s+", "")
                                .equalsIgnoreCase(name.replaceAll("\\s+", ""))
                        && (isNew || !i.getName().equalsIgnoreCase(currentItem.getName())));
        if (isDuplicate) {
            JOptionPane.showMessageDialog(this,
                    "\"" + name + "\" already exists.", "Duplicate Name", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }

        int    qty   = (int)    quantitySpinner.getValue();
        String cat   = (String) categoryCombo.getSelectedItem();
        double price = ((Number) priceSpinner.getValue()).doubleValue();
        String desc  = descriptionArea.getText().trim();

        if (isNew) {
            controller.getItemModel().addItem(new Item(name, qty, cat, price, desc));
        } else {
            String oldName = currentItem.getName();
            currentItem.setName(name);
            currentItem.setQuantity(qty);
            currentItem.setCategory(cat);
            currentItem.setPrice(price);
            currentItem.setDescription(desc);
            controller.getItemModel().updateItem(oldName, currentItem);
        }

        controller.onSave();
        dispose();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPER FACTORIES
    // ════════════════════════════════════════════════════════════════════════

    // ── Field / Label ─────────────────────────────────────────────────────────

    /** Text field พร้อม hover + focus border effect */
    private JTextField makeStyledField(String text) {
        JTextField f = new JTextField(text) {
            boolean hov = false;
            {
                setBorder(makeBorder(BORDER_IDLE));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  if (!isFocusOwner()) setBorder(makeBorder(BORDER_HOV));  }
                    @Override public void mouseExited (MouseEvent e) { hov = false; if (!isFocusOwner()) setBorder(makeBorder(BORDER_IDLE)); }
                });
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { setBorder(makeBorder(BORDER_FOC));                      }
                    @Override public void focusLost  (FocusEvent e) { setBorder(makeBorder(hov ? BORDER_HOV : BORDER_IDLE)); }
                });
            }
        };
        f.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        f.setBackground(WHITE);
        return f;
    }

    /** Compound border: rounded line + inner padding */
    private Border makeBorder(Color c) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c, 1, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11));
    }

    /** Label เล็ก ๆ บอกชื่อ field (all-caps style) */
    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(Color.decode("#6b7280"));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    /** Panel สี่เหลี่ยมมนพร้อม border — ใช้ห่อ field แต่ละกลุ่ม */
    private JPanel makeCard(JComponent... contents) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.decode("#e5e7eb"));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : contents) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(c);
            card.add(Box.createVerticalStrut(5));
        }
        return card;
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    /** ปุ่มหลัก (full pill) */
    private JButton makePillBtn(String text, Color bg, Color fg) {
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
                g2.setColor(hov ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        return btn;
    }

    /** ปุ่มเล็ก inline (เช่น "+ Add" / "✕ Remove" ของ category) */
    private JButton makeSmallBtn(String text, Color bg, Color fg) {
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
                g2.setColor(hov ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    // ── Spinner ───────────────────────────────────────────────────────────────

    /**
     * สลับ label ▲/▼ ของ spinner ให้ถูกต้อง
     * (Swing ค่า default สลับ increment/decrement กัน)
     */
    private void fixSpinnerArrows(JSpinner spinner) {
        for (Component c : spinner.getComponents()) {
            if (c instanceof JButton btn) {
                String action = btn.getActionCommand();

                if ("decrement".equals(action)) {
                    // ตั้ง label และ action เป็น "ขึ้น" (▲ = เพิ่มค่า)
                    btn.setText("▲");
                    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e5e7eb"));
                    btn.setForeground(LBL_COLOR);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.setContentAreaFilled(true);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
                    btn.addActionListener(e -> {
                        try { spinner.commitEdit(); } catch (java.text.ParseException ignored) {}
                        Object next = spinner.getModel().getNextValue();
                        if (next != null) spinner.getModel().setValue(next);
                    });
                }

                if ("increment".equals(action)) {
                    // ตั้ง label และ action เป็น "ลง" (▼ = ลดค่า)
                    btn.setText("▼");
                    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e5e7eb"));
                    btn.setForeground(LBL_COLOR);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.setContentAreaFilled(true);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
                    btn.addActionListener(e -> {
                        try { spinner.commitEdit(); } catch (java.text.ParseException ignored) {}
                        Object prev = spinner.getModel().getPreviousValue();
                        if (prev != null) spinner.getModel().setValue(prev);
                    });
                }
            }
        }
    }
}