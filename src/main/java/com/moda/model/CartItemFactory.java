package com.moda.model;

import java.util.ArrayList;
import java.util.List;

/**
 * COMPOSITE паттерн (Structural Pattern #2)
 * ─────────────────────────────────────────
 * Проблема: корзина может содержать как отдельные товары,
 * так и наборы (bundle). Хотим работать с ними одинаково.
 *
 * Решение: SingleItem и Bundle оба реализуют CartItem.
 * Код, который считает сумму, не знает — это один товар или набор.
 */

// ─── Leaf: одиночный товар ───────────────────────────────────────────────────
class SingleItem implements CartItem {
    private final Product product;

    public SingleItem(Product product) {
        this.product = product;
    }

    @Override public String getName()        { return product.getName(); }
    @Override public double getPrice()       { return product.getPrice(); }
    @Override public boolean isBundle()      { return false; }
    @Override public String getDescription() {
        return product.getEmoji() + " " + product.getName() + " — $" + product.getPrice();
    }
}

// ─── Composite: набор товаров ────────────────────────────────────────────────
class Bundle implements CartItem {
    private final String name;
    private final List<CartItem> items = new ArrayList<>();
    private final double discountPercent;   // скидка на набор, например 10%

    public Bundle(String name, double discountPercent) {
        this.name = name;
        this.discountPercent = discountPercent;
    }

    public void add(CartItem item) { items.add(item); }

    @Override public String getName() { return name; }

    // Цена набора = сумма всех товаров минус скидка
    @Override
    public double getPrice() {
        double total = items.stream().mapToDouble(CartItem::getPrice).sum();
        return Math.round(total * (1 - discountPercent / 100) * 100.0) / 100.0;
    }

    @Override public boolean isBundle() { return true; }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("🎁 Bundle: " + name + " (-" + (int)discountPercent + "%) [$" + getPrice() + "]\n");
        for (CartItem item : items) {
            sb.append("  └ ").append(item.getDescription()).append("\n");
        }
        return sb.toString().trim();
    }
}

// ─── Фабричный метод для создания CartItem ───────────────────────────────────
public class CartItemFactory {
    public static CartItem single(Product product) {
        return new SingleItem(product);
    }

    public static CartItem bundle(String name, double discountPercent, Product... products) {
        Bundle bundle = new Bundle(name, discountPercent);
        for (Product p : products) {
            bundle.add(new SingleItem(p));
        }
        return bundle;
    }
}
