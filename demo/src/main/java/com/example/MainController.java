package com.example;

import javax.swing.*;
import java.awt.*;

public class MainController {
    private ItemModel itemModel;
    private DashboardView dashboardView;

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
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            itemModel.deleteItem(item);
            dashboardView.refreshTable();
        }
    }

    // Double-click → open read-only detail window (non-modal, multiple allowed)
    public void openItemDetailWindow(Item item) {
        JFrame detailWindow = new JFrame("Detail: " + item.getName());
        detailWindow.setSize(380, 300);
        detailWindow.setLocationRelativeTo(dashboardView);
        // Offset each new window slightly so they don't stack exactly
        detailWindow.setLocation(
            detailWindow.getX() + (int)(Math.random() * 60) - 30,
            detailWindow.getY() + (int)(Math.random() * 60) - 30
        );
        detailWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailWindow.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode("#094654"));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel titleLbl = new JLabel(item.getName());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl);
        detailWindow.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        Font lblFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valFont = new Font("Segoe UI", Font.PLAIN, 14);

        String[][] rows = {
            {"Name",     item.getName()},
            {"Category", item.getCategory()},
            {"Quantity", String.valueOf(item.getQuantity())},
            {"Price",    String.format("฿ %,.2f", item.getPrice())}
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(rows[i][0] + ":");
            lbl.setFont(lblFont);
            lbl.setForeground(Color.decode("#6b7280"));
            body.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            JLabel val = new JLabel(rows[i][1]);
            val.setFont(valFont);
            body.add(val, gbc);
        }

        detailWindow.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.decode("#f9fafb"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editBtn.setBackground(Color.decode("#004e86"));
        editBtn.setForeground(Color.WHITE);
        editBtn.setBorderPainted(false);
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        editBtn.addActionListener(e -> {
            detailWindow.dispose();
            openDetailView(item);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setBackground(Color.decode("#e5e7eb"));
        closeBtn.setForeground(Color.decode("#374151"));
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> detailWindow.dispose());

        footer.add(editBtn);
        footer.add(closeBtn);
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