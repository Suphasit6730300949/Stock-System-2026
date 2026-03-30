package com.example;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class MongoDB {
    private static final String URI = "mongodb+srv://StockDB:Tt0897938633@cluster0.nkctc3k.mongodb.net/?appName=Cluster0";
    private static final String DB_NAME = "mystock";
    private static final String COLLECTION = "items";
    private static final String CAT_COLLECTION = "categories";

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    private MongoCollection<Document> catCollection;

    public MongoDB() {
        mongoClient = MongoClients.create(URI);
        MongoDatabase db = mongoClient.getDatabase(DB_NAME);
        collection = db.getCollection(COLLECTION);
        catCollection = db.getCollection(CAT_COLLECTION);
    }

    // ── โหลดทุก item จาก MongoDB ──
    public List<Item> loadAllItems() {
        List<Item> items = new ArrayList<>();
        for (Document doc : collection.find()) {
            double price = doc.containsKey("price") ? doc.getDouble("price") : 0.0;
            items.add(new Item(
                    doc.getString("name"),
                    doc.getInteger("quantity"),
                    doc.getString("category"),
                    price));
        }
        return items;
    }

    // ── เพิ่ม item ──
    public void insertItem(Item item) {
        Document doc = new Document()
                .append("name", item.getName())
                .append("quantity", item.getQuantity())
                .append("category", item.getCategory())
                .append("price", item.getPrice());
        collection.insertOne(doc);
    }

    // ── แก้ไข item ──
    public void updateItem(String oldName, Item item) {
        Document filter = new Document("name", oldName);
        Document update = new Document("$set", new Document()
                .append("name", item.getName())
                .append("quantity", item.getQuantity())
                .append("category", item.getCategory())
                .append("price", item.getPrice()));
        collection.updateOne(filter, update);
    }

    // ── ลบ item ──
    public void deleteItem(String name) {
        collection.deleteOne(new Document("name", name));
    }

    // โหลด categories ทั้งหมดจาก MongoDB
    public List<String> loadAllCategories() {
        List<String> cats = new ArrayList<>();
        for (Document doc : catCollection.find()) {
            cats.add(doc.getString("name"));
        }
        return cats;
    }

    // บันทึก category ใหม่ลง MongoDB (เช็คก่อนว่ามีอยู่แล้วหรือเปล่า)
    public void insertCategory(String categoryName) {
        Document existing = catCollection.find(new Document("name", categoryName)).first();
        if (existing == null) {
            catCollection.insertOne(new Document("name", categoryName));
        }
    }

    // ลบ category ออกจาก MongoDB
    public void deleteCategory(String categoryName) {
        catCollection.deleteOne(new Document("name", categoryName));
    }

    public void close() {
        mongoClient.close();
    }
}