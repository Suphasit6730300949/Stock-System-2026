package com.example;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ItemModel itemModel = new ItemModel();

            MainController controller = new MainController(itemModel);
            controller.start();
        });
    }
}