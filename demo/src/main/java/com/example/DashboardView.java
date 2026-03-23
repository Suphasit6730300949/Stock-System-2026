package com.example;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DashboardView extends JFrame {
    private ItemModel itemModel;
    private MainController controller;

    private JTable table;
    private DefaultTableModel tableModel;

    // DI: ItemModel injected via constructor
    public DashboardView(ItemModel itemModel, MainController controller) {
        this.itemModel = itemModel;
        this.controller = controller;
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        setTitle("MyStock Manager");
        setSize(700, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // ─── Header ───────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(37, 99, 235));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Inventory");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JButton addBtn = new JButton("Add item");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(Color.WHITE);
        addBtn.setForeground(new Color(37, 99, 235));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        addBtn.addActionListener(e -> controller.openAddItemView());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ─── Table ────────────────────────────────────────────
        String[] columns = {"Name", "Price (฿)","Quantity", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(229, 231, 235));
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(239, 246, 255));
        header.setForeground(new Color(37, 99, 235));
        header.setPreferredSize(new Dimension(0, 40));

        // Double-click → DetailView
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Item selected = itemModel.getItems().get(row);
                        controller.openDetailView(selected);
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

        JButton deleteBtn = new JButton("Delete Item");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Item selected = itemModel.getItems().get(row);
                controller.deleteItem(selected);
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
        tableModel.setRowCount(0);
        List<Item> items = itemModel.getItems();
        for (Item item : items) {
            tableModel.addRow(new Object[]{
                item.getName(),
                item.getQuantity(),
                item.getCategory()
            });
        }
    }
}

