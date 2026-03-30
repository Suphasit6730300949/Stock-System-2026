package com.example;

import java.time.LocalDateTime;

/**
 * Model: ข้อมูลของสินค้าชิ้นหนึ่ง
 * เก็บชื่อ, จำนวน, หมวดหมู่, ราคา, คำอธิบาย และเวลา createdAt / updatedAt
 */
public class Item {

    private String        name;
    private int           quantity;
    private String        category;
    private double        price;
    private String        description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors (overloaded เพื่อรองรับการสร้างจาก DB และจาก UI) ──────────

    public Item(String name, int quantity, String category) {
        this(name, quantity, category, 0.0, "", null, null);
    }

    public Item(String name, int quantity, String category, double price) {
        this(name, quantity, category, price, "", null, null);
    }

    public Item(String name, int quantity, String category, double price, String description) {
        this(name, quantity, category, price, description, null, null);
    }

    public Item(String name, int quantity, String category, double price,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(name, quantity, category, price, "", createdAt, updatedAt);
    }

    /** Full constructor — ใช้ภายใน (รวมทุก field) */
    public Item(String name, int quantity, String category, double price,
                String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name        = name;
        this.quantity    = quantity;
        this.category    = category;
        this.price       = price;
        this.description = description != null ? description : "";
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }

    public int    getQuantity()                    { return quantity; }
    public void   setQuantity(int quantity)        { this.quantity = quantity; }

    public String getCategory()                    { return category; }
    public void   setCategory(String category)     { this.category = category; }

    public double getPrice()                       { return price; }
    public void   setPrice(double price)           { this.price = price; }

    public String getDescription()                 { return description != null ? description : ""; }
    public void   setDescription(String desc)      { this.description = desc != null ? desc : ""; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void          setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()            { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }

    @Override
    public String toString() {
        return name + " (฿" + String.format("%.2f", price) + ")";
    }
}