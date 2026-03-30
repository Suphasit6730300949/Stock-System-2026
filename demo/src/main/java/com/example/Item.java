package com.example;

public class Item {
    private String name;
    private int quantity;
    private String category;
    private double price;

    public Item(String name, int quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.price = 0.0;
    }

    public Item(String name, int quantity, String category, double price) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " (à¸¿" + String.format("%.2f", price) + ")";
    }
}