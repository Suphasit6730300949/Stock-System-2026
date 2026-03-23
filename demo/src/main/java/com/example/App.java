package com.example;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ItemModel itemModel = new ItemModel();

            // Sample data
            itemModel.addItem(new Item("Laptop", 50, "Electronics"));
            itemModel.addItem(new Item("Floor Cleaner", 200, "Household"));
            itemModel.addItem(new Item("Body Lotion", 100, "Personal Care"));

            MainController controller = new MainController(itemModel);
            controller.start();
        });
    }
}
