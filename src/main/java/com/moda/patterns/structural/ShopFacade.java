package com.moda.patterns.structural;

import com.moda.model.*;
import com.moda.patterns.creational.OrderManager;
import com.moda.patterns.creational.ProductCatalog;

import java.util.List;

/**
 * FACADE паттерн (Structural Pattern #3)
 * ───────────────────────────────────────
 * Проблема: чтобы оформить заказ, клиентский код должен знать о:
 * ProductCatalog, CartItemFactory, PricingCalculator, Order.Builder, OrderManager...
 * Это слишком много зависимостей для HTTP-обработчика.
 *
 * Решение: Facade предоставляет один простой метод placeOrder(),
 * который внутри координирует всю сложную логику.
 *
 * COMPILE TIME: HttpHandler зависит только от ShopFacade, не от 5 классов.
 * RUNTIME: Facade вызывает нужные компоненты в правильном порядке.
 */
public class ShopFacade {

    private final ProductCatalog catalog;
    private final OrderManager orderManager;

    public ShopFacade() {
        // Получаем синглтоны
        this.catalog      = ProductCatalog.getInstance();
        this.orderManager = OrderManager.getInstance();
    }

    /**
     * Главный метод — оформить заказ.
     * Клиент передаёт данные формы, Facade делает всё остальное.
     */
    public Order placeOrder(String customerName,
                            String address,
                            List<String> productIds,
                            String deliveryType,
                            boolean giftWrap) {

        // Шаг 1: Получаем товары из каталога и оборачиваем в CartItem (Composite)
        List<CartItem> cartItems = productIds.stream()
                .map(id -> catalog.getById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id)))
                .map(CartItemFactory::single)
                .toList();

        // Шаг 2: Считаем стоимость через Decorator
        double itemsTotal = cartItems.stream().mapToDouble(CartItem::getPrice).sum();
        PricingComponent pricing = PricingCalculator.calculate(itemsTotal, deliveryType, giftWrap);
        double finalPrice = pricing.getPrice();

        // Шаг 3: Создаём заказ через Builder
        Order order = new Order.Builder()
                .customerName(customerName)
                .address(address)
                .items(cartItems)
                .deliveryType(deliveryType)
                .giftWrap(giftWrap)
                .totalPrice(Math.round(finalPrice * 100.0) / 100.0)
                .build();

        // Шаг 4: Сохраняем через Singleton OrderManager (он уведомит Observer'ов)
        return orderManager.placeOrder(order);
    }

    public List<Product> getProducts()     { return catalog.getAll(); }
    public List<Order>   getOrders()       { return orderManager.getAllOrders(); }

    public Order updateOrderStatus(String orderId, String status) {
        Order.Status newStatus = Order.Status.valueOf(status.toUpperCase());
        orderManager.updateStatus(orderId, newStatus);
        return orderManager.getOrder(orderId);
    }
}
