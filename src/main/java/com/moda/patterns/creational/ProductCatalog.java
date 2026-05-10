package com.moda.patterns.creational;

import com.moda.model.Product;

import java.util.*;

/**
 * Второй Singleton в проекте — каталог товаров.
 * Товары загружаются один раз и доступны всему приложению.
 *
 * COMPILE TIME: private конструктор виден компилятору.
 * RUNTIME: список товаров инициализируется один раз при первом вызове.
 */
public class ProductCatalog {

    private static volatile ProductCatalog instance;
    private final List<Product> products = new ArrayList<>();

    private ProductCatalog() {
        loadProducts();
    }

    public static ProductCatalog getInstance() {
        if (instance == null) {
            synchronized (ProductCatalog.class) {
                if (instance == null) {
                    instance = new ProductCatalog();
                }
            }
        }
        return instance;
    }

    private void loadProducts() {
        // Tops
        products.add(new Product("T001", "Cherry Blossom Crop Top",  "tops",        29.99, "🌸"));
        products.add(new Product("T002", "Soft Cloud Knit Sweater",  "tops",        49.99, "☁️"));
        products.add(new Product("T003", "Hanbok-inspired Blouse",   "tops",        39.99, "🎎"));
        products.add(new Product("T004", "Pastel Oversized Hoodie",  "tops",        44.99, "🍑"));
        // Bottoms
        products.add(new Product("B001", "Mini Pleated Skirt",       "bottoms",     34.99, "🩷"));
        products.add(new Product("B002", "Wide-leg Linen Pants",     "bottoms",     42.99, "🌿"));
        products.add(new Product("B003", "Denim Micro Skirt",        "bottoms",     28.99, "💙"));
        // Accessories
        products.add(new Product("A001", "Pearl Hair Clip Set",      "accessories", 14.99, "🤍"));
        products.add(new Product("A002", "Coquette Ribbon Bag",      "accessories", 24.99, "🎀"));
        products.add(new Product("A003", "Star Charm Necklace",      "accessories", 19.99, "⭐"));
        products.add(new Product("A004", "Cute Socks Bundle",        "accessories",  9.99, "🧦"));
    }

    public List<Product> getAll()                         { return Collections.unmodifiableList(products); }
    public List<Product> getByCategory(String category)  {
        return products.stream().filter(p -> p.getCategory().equals(category)).toList();
    }
    public Optional<Product> getById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }
}
