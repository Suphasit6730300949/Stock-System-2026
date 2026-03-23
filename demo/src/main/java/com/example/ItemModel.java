package com.example;

import java.util.ArrayList;
import java.util.List;

public class ItemModel {
    private List<Item> items = new ArrayList<>();
    private List<String> categories = new ArrayList<>();

    public ItemModel() {
        // Default categories
        categories.add("Household");
        categories.add("Personal Care");
        categories.add("Electronics");
        categories.add("Stationery");
    }

    public void addItem(Item item) {
        items.add(item);
    }

    // Business logic: deleteItem
    public void deleteItem(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }
}
