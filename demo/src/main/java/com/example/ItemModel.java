package com.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Model: จัดการรายการสินค้าและหมวดหมู่ทั้งหมด
 * ทำหน้าที่เป็นตัวกลางระหว่าง Controller กับ MongoDB
 */
public class ItemModel {

    private List<Item>   items      = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private final MongoDB db;

    // ── ค่า default ของ category ที่ใส่ไว้ครั้งแรก ──────────────────────────────
    private static final List<String> DEFAULT_CATEGORIES =
            List.of("Household", "Personal Care", "Electronics", "Stationery");

    public ItemModel() {
        db = new MongoDB();

        // โหลด items ทั้งหมดจาก DB
        items = db.loadAllItems();

        // โหลด categories — ถ้า DB ยังว่างให้ใส่ค่า default
        List<String> savedCats = db.loadAllCategories();
        if (savedCats.isEmpty()) {
            for (String cat : DEFAULT_CATEGORIES) {
                db.insertCategory(cat);
            }
            categories.addAll(DEFAULT_CATEGORIES);
        } else {
            categories.addAll(savedCats);
        }
    }

    // ── Item CRUD ────────────────────────────────────────────────────────────

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
        items = db.loadAllItems(); // รีโหลดเพื่อให้ข้อมูล (เช่น updatedAt) ตรงกับ DB
    }

    public List<Item> getItems() {
        return new ArrayList<>(items); // คืน copy เพื่อป้องกัน external mutation
    }

    // ── Category CRUD ────────────────────────────────────────────────────────

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
            db.insertCategory(category);
        }
    }

    public void removeCategory(String category) {
        categories.remove(category);
        db.deleteCategory(category);
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories); // คืน copy
    }

    // ── Reload ───────────────────────────────────────────────────────────────

    /** โหลดข้อมูลใหม่ทั้งหมดจาก MongoDB (ใช้เมื่อกด Refresh) */
    public void reloadFromDB() {
        items = db.loadAllItems();
        List<String> savedCats = db.loadAllCategories();
        categories.clear();
        categories.addAll(savedCats);
    }
}