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

    public DetailView(JFrame parent, MainController controller) {
        super(parent, true);
        this.controller = controller;
    }

    public void showDetailView(Item item) {
        this.currentItem = item;
        boolean isNew = (item == null);
        setTitle(isNew ? "Add new" : "Edit");
        setSize(420, 420);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // ─── Header ───────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(isNew ? new Color(16, 185, 129) : new Color(37, 99, 235));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel(isNew ? "Add new" : "Edit: " + item.getName());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl);
        add(headerPanel, BorderLayout.NORTH);

        // ─── Form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel nameLbl = new JLabel("name:");
        nameLbl.setFont(labelFont);
        formPanel.add(nameLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        nameField = new JTextField(isNew ? "" : item.getName());
        nameField.setFont(fieldFont);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(nameField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(labelFont);
        formPanel.add(qtyLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(
            isNew ? 0 : item.getQuantity(), 0, 99999, 1
        ));
        quantitySpinner.setFont(fieldFont);
        formPanel.add(quantitySpinner, gbc);

        // Category (dropdown + remove button)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(labelFont);
        formPanel.add(catLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        List<String> cats = controller.getItemModel().getCategories();
        categoryCombo = new JComboBox<>(cats.toArray(new String[0]));
        if (!isNew) categoryCombo.setSelectedItem(item.getCategory());
        categoryCombo.setFont(fieldFont);

        // ✅ ปุ่ม Remove category
        JButton removeCatBtn = new JButton("Remove");
        removeCatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        removeCatBtn.setBackground(new Color(239, 68, 68));
        removeCatBtn.setForeground(Color.WHITE);
        removeCatBtn.setBorderPainted(false);
        removeCatBtn.setFocusPainted(false);
        removeCatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeCatBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        removeCatBtn.addActionListener(e -> {
            String selectedCat = (String) categoryCombo.getSelectedItem();
            if (selectedCat == null) return;

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "ลบ category \"" + selectedCat + "\" ออกจากรายการ?",
                "ยืนยันการลบ",
                JOptionPane.YES_NO_OPTION
            );
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
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel newCatLbl = new JLabel("new Category:");
        newCatLbl.setFont(labelFont);
        formPanel.add(newCatLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JPanel newCatPanel = new JPanel(new BorderLayout(6, 0));
        newCatPanel.setBackground(Color.WHITE);
        newCategoryField = new JTextField();
        newCategoryField.setFont(fieldFont);
        newCategoryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JButton addCatBtn = new JButton("Add");
        addCatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addCatBtn.setBackground(new Color(16, 185, 129));
        addCatBtn.setForeground(Color.WHITE);
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
        btnPanel.setBackground(new Color(249, 250, 251));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancelBtn = new JButton("cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setBackground(new Color(229, 231, 235));
        cancelBtn.setForeground(new Color(55, 65, 81));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("save");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกชื่อสินค้า", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qty = (int) quantitySpinner.getValue();
            String cat = (String) categoryCombo.getSelectedItem();

            if (isNew) {
                controller.getItemModel().addItem(new Item(name, qty, cat));
            } else {
                String oldName = currentItem.getName();
                currentItem.setName(name);
                currentItem.setQuantity(qty);
                currentItem.setCategory(cat);
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