package com.example;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ItemModel itemModel = new ItemModel(); //สร้าง Model
            MainController controller = new MainController(itemModel); //สร้าง Controller
            controller.start(); //สั่ง start บนEDT
        });
    }
}
