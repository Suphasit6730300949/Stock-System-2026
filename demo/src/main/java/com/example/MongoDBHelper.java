package com.example;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class MongoDBHelper {
    private static final String URI = "mongodb+srv://StockDB:Tt0897938633@cluster0.nkctc3k.mongodb.net/?appName=Cluster0";
    private static final String DB_NAME = "mystock";
    private static final String COLLECTION = "items";

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public MongoDBHelper() {
        mongoClient = MongoClients.create(URI);
        MongoDatabase db = mongoClient.getDatabase(DB_NAME);
        collection = db.getCollection(COLLECTION);
    }

    // ── โหลดทุก item จาก MongoDB ──
    public List<Item> loadAllItems() {
        List<Item> items = new ArrayList<>();
        for (Document doc : collection.find()) {
            items.add(new Item(
                doc.getString("name"),
                doc.getInteger("quantity"),
                doc.getString("category")
            ));
        }
        return items;
    }

    // ── เพิ่ม item ──
    public void insertItem(Item item) {
        Document doc = new Document()
            .append("name", item.getName())
            //.append("price", item.getPrice())
            .append("quantity", item.getQuantity())
            .append("category", item.getCategory());
        collection.insertOne(doc);
    }

    // ── แก้ไข item ──
    public void updateItem(String oldName, Item item) {
        Document filter = new Document("name", oldName);
        Document update = new Document("$set", new Document()
            .append("name", item.getName())
            //.append("price", item.getPrice())
            .append("quantity", item.getQuantity())
            .append("category", item.getCategory()));
        collection.updateOne(filter, update);
    }

    // ── ลบ item ──
    public void deleteItem(String name) {
        collection.deleteOne(new Document("name", name));
    }

    public void close() {
        mongoClient.close();
    }
}