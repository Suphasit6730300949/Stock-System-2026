package com.example;

import javax.swing.*;

public class MainController {
    private ItemModel itemModel;
    private DashboardView dashboardView;

    public MainController(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    public void start() {
        // Inject ItemModel via DashboardView's constructor (DI)
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

    // Switch to DetailView when double-clicking item (DI via showDetailView)
    public void openDetailView(Item item) {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(item); // Inject item via method
    }

    // Called after Save in DetailView
    public void onSave() {
        dashboardView.refreshTable();
    }

    // Open add item dialog
    public void openAddItemView() {
        DetailView detailView = new DetailView(dashboardView, this);
        detailView.showDetailView(null); // null = new item
    }

    public ItemModel getItemModel() {
        return itemModel;
    }
}