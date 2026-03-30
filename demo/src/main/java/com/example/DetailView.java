package com.example;

import javax.swing.*;
import java.awt.*;
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

    public DetailView(JFrame parent, MainController controller) {
        super(parent, true);
        this.controller = controller;
    }

    // ── Replace the "−" decrement button with an "▲" up-arrow button ──────────
    private void fixSpinnerArrows(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        // The spinner UI has two arrow buttons; find them
        for (Component c : spinner.getComponents()) {
            if (c instanceof JButton btn) {
                // The increment button already shows "▲" by default in most L&Fs,
                // but the decrement shows "▼" or "−". We identify it by its action:
                // actionCommand is "increment" or "decrement".
                String action = btn.getActionCommand();
                if ("decrement".equals(action)) {
                    btn.setText("▲");
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e0e0e0"));
                    btn.setForeground(Color.decode("#333333"));
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.setContentAreaFilled(true);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btn.setToolTipText("Decrease");

                    // Wire it up: clicking ▲ on the BOTTOM button still decrements,
                    // so swap — remove original listeners and add increment action
                    for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                        btn.removeActionListener(al);
                    }
                    btn.addActionListener(e -> {
                        try {
                            spinner.commitEdit();
                        } catch (java.text.ParseException ignored) {
                        }
                        SpinnerModel model = spinner.getModel();
                        Object next = model.getNextValue();
                        if (next != null)
                            model.setValue(next);
                    });
                }
                if ("increment".equals(action)) {
                    btn.setText("▼");
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    btn.setBackground(Color.decode("#e0e0e0"));
                    btn.setForeground(Color.decode("#333333"));
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.setContentAreaFilled(true);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btn.setToolTipText("Increase");

                    for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                        btn.removeActionListener(al);
                    }
                    btn.addActionListener(e -> {
                        try {
                            spinner.commitEdit();
                        } catch (java.text.ParseException ignored) {
                        }
                        SpinnerModel model = spinner.getModel();
                        Object prev = model.getPreviousValue();
                        if (prev != null)
                            model.setValue(prev);
                    });
                }
            }
        }
    }

    public void showDetailView(Item item) {
        this.currentItem = item;
        boolean isNew = (item == null);
        setTitle(isNew ? "Add new" : "Edit");
        setSize(420, 580);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.decode("#cdd0d5"));

        // ─── Header ───────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(isNew ? Color.decode("#004e86") : Color.decode("#094654")); // Header ของ Add/Edit ?
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel(isNew ? "Add new" : "Edit: " + item.getName());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.decode("#ffffff")); // Color.decode("#2563eb")
        headerPanel.add(titleLbl);
        add(headerPanel, BorderLayout.NORTH);

        // ─── Form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.decode("#ffffff"));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel nameLbl = new JLabel("Name:");
        nameLbl.setFont(labelFont);
        formPanel.add(nameLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        nameField = new JTextField();
        nameField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null)
                    return;
                if ((getLength() + str.length()) <= 30)
                    super.insertString(offs, str, a);
            }
        });
        nameField.setText(isNew ? "" : item.getName());
        nameField.setFont(fieldFont);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#d1d5db")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel charCountLbl = new JLabel((isNew ? "0" : String.valueOf(item.getName().length())) + " / 30");
        charCountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        charCountLbl.setForeground(Color.decode("#9ca3af"));
        charCountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                int len = nameField.getText().length();
                charCountLbl.setText(len + " / 30");
                charCountLbl.setForeground(len == 30 ? Color.decode("#dc2626") : Color.decode("#9ca3af"));
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        });

        JPanel namePanel = new JPanel(new BorderLayout(0, 2));
        namePanel.setBackground(Color.WHITE);
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(charCountLbl, BorderLayout.SOUTH);
        formPanel.add(namePanel, gbc);

        // Description (รองจาก Name)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel descLbl = new JLabel("Description:");
        descLbl.setFont(labelFont);
        formPanel.add(descLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setText(isNew ? "" : item.getDescription());
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#d1d5db")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formPanel.add(descriptionArea, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(labelFont);
        formPanel.add(qtyLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(
                isNew ? 0 : item.getQuantity(), 0, 99999, 1));
        quantitySpinner.setFont(fieldFont);
        fixSpinnerArrows(quantitySpinner);
        formPanel.add(quantitySpinner, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel priceLbl = new JLabel("Price (฿):");
        priceLbl.setFont(labelFont);
        formPanel.add(priceLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        priceSpinner = new JSpinner(new SpinnerNumberModel(
                isNew ? 0.0 : item.getPrice(), 0.0, 9999999.0, 0.01));
        priceSpinner.setFont(fieldFont);
        JSpinner.NumberEditor priceEditor = new JSpinner.NumberEditor(priceSpinner, "#,##0.00");
        priceSpinner.setEditor(priceEditor);
        fixSpinnerArrows(priceSpinner);
        formPanel.add(priceSpinner, gbc);

        // Category (dropdown + remove button)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(labelFont);
        formPanel.add(catLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        List<String> cats = controller.getItemModel().getCategories();
        categoryCombo = new JComboBox<>(cats.toArray(new String[0]));
        if (!isNew)
            categoryCombo.setSelectedItem(item.getCategory());
        categoryCombo.setFont(fieldFont);

        // ปุ่ม Remove category
        JButton removeCatBtn = new JButton("Remove");
        removeCatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        removeCatBtn.setBackground(Color.decode("#9e0000"));
        removeCatBtn.setForeground(Color.decode("#ffffff"));
        removeCatBtn.setBorderPainted(false);
        removeCatBtn.setFocusPainted(false);
        removeCatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeCatBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        removeCatBtn.addActionListener(e -> {
            String selectedCat = (String) categoryCombo.getSelectedItem();
            if (selectedCat == null)
                return;

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "ลบ category \"" + selectedCat + "\" ออกจากรายการ?",
                    "ยืนยันการลบ",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.getItemModel().removeCategory(selectedCat);
                categoryCombo.removeItem(selectedCat);
            }
        });

        JPanel catPanel = new JPanel(new BorderLayout(6, 0));
        catPanel.setBackground(Color.WHITE);
        catPanel.add(categoryCombo, BorderLayout.CENTER);
        catPanel.add(removeCatBtn, BorderLayout.EAST);
        formPanel.add(catPanel, gbc);

        // Add new category
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        JLabel newCatLbl = new JLabel("new Category:");
        newCatLbl.setFont(labelFont);
        formPanel.add(newCatLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel newCatPanel = new JPanel(new BorderLayout(6, 0));
        newCatPanel.setBackground(Color.WHITE);
        newCategoryField = new JTextField();
        newCategoryField.setFont(fieldFont);
        newCategoryField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#d1d5db")),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JButton addCatBtn = new JButton("Add");
        addCatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addCatBtn.setBackground(Color.decode("#004e86"));
        addCatBtn.setForeground(Color.decode("#ffffff"));
        addCatBtn.setBorderPainted(false);
        addCatBtn.setFocusPainted(false);
        addCatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addCatBtn.addActionListener(e -> {
            String newCat = newCategoryField.getText().trim();
            if (!newCat.isEmpty()) {
                controller.getItemModel().addCategory(newCat);
                categoryCombo.addItem(newCat);
                categoryCombo.setSelectedItem(newCat);
                newCategoryField.setText("");
            }
        });
        newCatPanel.add(newCategoryField, BorderLayout.CENTER);
        newCatPanel.add(addCatBtn, BorderLayout.EAST);
        formPanel.add(newCatPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ─── Buttons ──────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(Color.decode("#f9fafb"));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JButton cancelBtn = new JButton("cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setBackground(Color.decode("#e5e7eb"));
        cancelBtn.setForeground(Color.decode("#374151"));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("save");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(Color.decode("#129469"));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter product name", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isDuplicate = controller.getItemModel().getItems().stream()
                    .anyMatch(i -> i.getName().replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))
                            && (isNew || !i.getName().equalsIgnoreCase(currentItem.getName())));
            if (isDuplicate) {
                JOptionPane.showMessageDialog(this, "Product name \"" + name + "\" already exists", "Duplicate Name",
                        JOptionPane.WARNING_MESSAGE);
                nameField.requestFocusInWindow();
                return;
            }
            int qty = (int) quantitySpinner.getValue();
            String cat = (String) categoryCombo.getSelectedItem();
            double price = ((Number) priceSpinner.getValue()).doubleValue();
            String desc = descriptionArea.getText().trim();

            if (isNew) {
                Item newItem = new Item(name, qty, cat, price, desc);
                controller.getItemModel().addItem(newItem);
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

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}