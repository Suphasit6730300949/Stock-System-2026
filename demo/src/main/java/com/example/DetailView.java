package com.example;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DetailView extends JDialog {
    private MainController controller;
    private Item currentItem;

    private JTextField nameField;
    private JSpinner quantitySpinner;
    private JComboBox<String> categoryCombo;
    private JTextField newCategoryField;
    private JSpinner priceSpinner;
    private JTextArea descriptionArea;

    // ── Design tokens ──────────────────────────────────────────────────────────
    private static final Color BG          = Color.decode("#f8fafc");
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER_IDLE = Color.decode("#d1d5db");
    private static final Color BORDER_HOV  = Color.decode("#93c5fd");
    private static final Color BORDER_FOC  = Color.decode("#3b82f6");
    private static final Color LBL_COLOR   = Color.decode("#374151");
    private static final Color HINT_COLOR  = Color.decode("#9ca3af");

    public DetailView(JFrame parent, MainController controller) {
        super(parent, true);
        this.controller = controller;
    }

    // ── Styled text field with hover + focus border ────────────────────────────
    private JTextField makeStyledField(String text) {
        JTextField f = new JTextField(text) {
            boolean hov = false;
            {
                setBorder(makeBorder(BORDER_IDLE));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  if (!isFocusOwner()) setBorder(makeBorder(BORDER_HOV));  }
                    public void mouseExited (MouseEvent e) { hov = false; if (!isFocusOwner()) setBorder(makeBorder(BORDER_IDLE)); }
                });
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { setBorder(makeBorder(BORDER_FOC));                      }
                    public void focusLost  (FocusEvent e) { setBorder(makeBorder(hov ? BORDER_HOV : BORDER_IDLE)); }
                });
            }
        };
        f.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        f.setBackground(WHITE);
        return f;
    }

    private Border makeBorder(Color c) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(c, 1, true),
            BorderFactory.createEmptyBorder(7, 11, 7, 11));
    }

    // ── Small caps label above each field ─────────────────────────────────────
    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(Color.decode("#6b7280"));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Rounded card wrapper ──────────────────────────────────────────────────
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

    // ── Pill / rounded button ─────────────────────────────────────────────────
    private JButton makePillBtn(String text, Color bg, Color fg) {
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

    // ── Small inline button ───────────────────────────────────────────────────
    private JButton makeSmallBtn(String text, Color bg, Color fg) {
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

    // ── Fix spinner arrow direction ───────────────────────────────────────────
    private void fixSpinnerArrows(JSpinner spinner) {
        for (Component c : spinner.getComponents()) {
            if (c instanceof JButton btn) {
                String action = btn.getActionCommand();
                if ("decrement".equals(action)) {
                    btn.setText("▲");
                    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e5e7eb"));
                    btn.setForeground(LBL_COLOR);
                    btn.setFocusPainted(false); btn.setBorderPainted(false);
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
                    btn.setText("▼");
                    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e5e7eb"));
                    btn.setForeground(LBL_COLOR);
                    btn.setFocusPainted(false); btn.setBorderPainted(false);
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

    // ─────────────────────────────────────────────────────────────────────────
    public void showDetailView(Item item) {
        this.currentItem = item;
        boolean isNew = (item == null);
        setTitle(isNew ? "Add Item" : "Edit Item");
        setSize(460, 620);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // ── HEADER ────────────────────────────────────────────────────────────
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

        // circle icon
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
        titleBlock.add(titleLbl, BorderLayout.NORTH);
        titleBlock.add(subtitleLbl, BorderLayout.CENTER);

        JPanel headerInner = new JPanel(new BorderLayout(14, 0));
        headerInner.setOpaque(false);
        headerInner.add(iconLbl, BorderLayout.WEST);
        headerInner.add(titleBlock, BorderLayout.CENTER);
        header.add(headerInner, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ── BODY ──────────────────────────────────────────────────────────────
        Font fieldFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Name
        nameField = makeStyledField(isNew ? "" : item.getName());
        nameField.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= 30) super.insertString(offs, str, a);
            }
        });
        nameField.setText(isNew ? "" : item.getName());

        JLabel charCountLbl = new JLabel((isNew ? "0" : String.valueOf(item.getName().length())) + " / 30");
        charCountLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        charCountLbl.setForeground(HINT_COLOR);
        charCountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        charCountLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                int len = nameField.getText().length();
                charCountLbl.setText(len + " / 30");
                charCountLbl.setForeground(len == 30 ? Color.decode("#dc2626") : HINT_COLOR);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        JPanel nameFieldPanel = new JPanel(new BorderLayout(0, 2));
        nameFieldPanel.setOpaque(false);
        nameFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameFieldPanel.add(nameField, BorderLayout.CENTER);
        nameFieldPanel.add(charCountLbl, BorderLayout.SOUTH);

        body.add(makeCard(makeFieldLabel("ITEM NAME"), nameFieldPanel));
        body.add(Box.createVerticalStrut(10));

        // Description
        descriptionArea = new JTextArea(isNew ? "" : item.getDescription(), 3, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(WHITE);
        descriptionArea.setBorder(makeBorder(BORDER_IDLE));
        descriptionArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { descriptionArea.setBorder(makeBorder(BORDER_FOC)); }
            public void focusLost  (FocusEvent e) { descriptionArea.setBorder(makeBorder(BORDER_IDLE)); }
        });
        descriptionArea.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!descriptionArea.isFocusOwner()) descriptionArea.setBorder(makeBorder(BORDER_HOV)); }
            public void mouseExited (MouseEvent e) { if (!descriptionArea.isFocusOwner()) descriptionArea.setBorder(makeBorder(BORDER_IDLE)); }
        });

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(makeCard(makeFieldLabel("DESCRIPTION  (optional)"), descScroll));
        body.add(Box.createVerticalStrut(10));

        // Quantity + Price (2-column)
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
        twoCol.add(makeCard(makeFieldLabel("QUANTITY"), quantitySpinner));
        twoCol.add(makeCard(makeFieldLabel("PRICE  (THB)"), priceSpinner));
        body.add(twoCol);
        body.add(Box.createVerticalStrut(10));

        // Category
        List<String> cats = controller.getItemModel().getCategories();
        categoryCombo = new JComboBox<>(cats.toArray(new String[0]));
        if (!isNew) categoryCombo.setSelectedItem(item.getCategory());
        categoryCombo.setFont(fieldFont);
        categoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton removeCatBtn = makeSmallBtn("✕ Remove", Color.decode("#fee2e2"), Color.decode("#9e0000"));
        removeCatBtn.addActionListener(e -> {
            String sel = (String) categoryCombo.getSelectedItem();
            if (sel == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                "Remove category \"" + sel + "\" from list?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                controller.getItemModel().removeCategory(sel);
                categoryCombo.removeItem(sel);
            }
        });

        JPanel catRow = new JPanel(new BorderLayout(8, 0));
        catRow.setOpaque(false);
        catRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        catRow.add(categoryCombo, BorderLayout.CENTER);
        catRow.add(removeCatBtn, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(Color.decode("#e5e7eb"));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        newCategoryField = makeStyledField("");
        newCategoryField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

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
        newCatRow.add(addCatBtn, BorderLayout.EAST);

        JLabel newCatHint = new JLabel("Type a name and click + Add");
        newCatHint.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        newCatHint.setForeground(HINT_COLOR);
        newCatHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(makeCard(
            makeFieldLabel("CATEGORY"),
            catRow,
            sep,
            makeFieldLabel("ADD NEW CATEGORY"),
            newCatRow,
            newCatHint
        ));

        body.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // ── FOOTER ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setOpaque(false);

        JButton cancelBtn = makePillBtn("Cancel", Color.decode("#e5e7eb"), LBL_COLOR);
        cancelBtn.addActionListener(e -> dispose());

        Color saveColor = isNew ? Color.decode("#004e86") : Color.decode("#129469");
        JButton saveBtn = makePillBtn(isNew ? "+ Add Item" : "Save Changes", saveColor, WHITE);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a product name.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isDuplicate = controller.getItemModel().getItems().stream()
                .anyMatch(i -> i.getName().replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))
                    && (isNew || !i.getName().equalsIgnoreCase(currentItem.getName())));
            if (isDuplicate) {
                JOptionPane.showMessageDialog(this, "\"" + name + "\" already exists.", "Duplicate Name", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocusInWindow();
                return;
            }
            int qty      = (int) quantitySpinner.getValue();
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
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        footer.add(btnRow, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }
}