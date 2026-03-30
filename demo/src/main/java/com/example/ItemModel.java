package com.example;

import java.util.ArrayList;
import java.util.List;

public class ItemModel {
    private List<Item> items = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private MongoDB db;

    public ItemModel() {
        db = new MongoDB();

        items = db.loadAllItems();

        List<String> savedCats = db.loadAllCategories();

        List<String> defaults = List.of("Household", "Personal Care", "Electronics", "Stationery");
        if (savedCats.isEmpty()) {
            for (String cat : defaults) {
                db.insertCategory(cat);
            }
            categories.addAll(defaults);
        } else {
            categories.addAll(savedCats);
        }
    }

    public void addItem(Item item) {
        items.add(item);
        db.insertItem(item);
    }

    public void deleteItem(Item item) {
        items.remove(item);
        db.deleteItem(item.getName());
    }

    public void updateItem(String oldName, Item item) {
        db.updateItem(oldName, item);
        items = db.loadAllItems();
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
            db.insertCategory(category);
        }
    }

    // ลบ category ออกจาก list และ MongoDB
    public void removeCategory(String category) {
        categories.remove(category);
        db.deleteCategory(category);
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }
}