package com.example;

import java.util.ArrayList;
import java.util.List;


public class ItemModel {
    private List<Item> items = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private MongoDBHelper db; // ✅ เพิ่ม

    public ItemModel() {
        db = new MongoDBHelper(); // ✅ เชื่อม MongoDB

        // โหลดข้อมูลจาก DB มาแสดง
        items = db.loadAllItems();

        categories.add("Household");
        categories.add("Personal Care");
        categories.add("Electronics");
        categories.add("Stationery");
    }

    public void addItem(Item item) {
        items.add(item);
        db.insertItem(item); // ✅ บันทึกลง MongoDB
    }

    public void deleteItem(Item item) {
        items.remove(item);
        db.deleteItem(item.getName()); // ✅ ลบจาก MongoDB
    }

    public void updateItem(String oldName, Item item) {
        db.updateItem(oldName, item); // ✅ อัปเดต MongoDB
        // refresh list
        items = db.loadAllItems();
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