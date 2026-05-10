package com.moda.model;

/**
 * Базовый класс товара в магазине.
 * Используется в Composite (CartItem) и Builder (Order).
 */
public class Product {
    private final String id;
    private final String name;
    private final String category;   // "tops", "bottoms", "accessories"
    private final double price;
    private final String emoji;      // для красивого UI

    public Product(String id, String name, String category, double price, String emoji) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.emoji = emoji;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
    public String getEmoji()    { return emoji; }

    @Override
    public String toString() {
        return emoji + " " + name + " — $" + price;
    }
}
