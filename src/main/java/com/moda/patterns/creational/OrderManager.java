package com.moda.patterns.creational;

import com.moda.model.Order;
import com.moda.patterns.behavioral.OrderObserver;

import java.util.*;

/**
 * SINGLETON паттерн (Creational Pattern #1)
 * ──────────────────────────────────────────
 * Проблема: нам нужна одна точка управления всеми заказами.
 * Если создать несколько экземпляров OrderManager — заказы потеряются.
 *
 * Решение: Singleton гарантирует, что существует РОВНО ОДИН объект.
 *
 * COMPILE TIME: конструктор private — компилятор запрещает new OrderManager() извне.
 * RUNTIME: объект создаётся при первом вызове getInstance() (lazy initialization).
 */
public class OrderManager {

    // volatile гарантирует видимость между потоками
    private static volatile OrderManager instance;

    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final List<OrderObserver> observers = new ArrayList<>();

    // Приватный конструктор — никто снаружи не может создать объект
    private OrderManager() {}

    /**
     * Double-checked locking — безопасно для многопоточности.
     */
    public static OrderManager getInstance() {
        if (instance == null) {
            synchronized (OrderManager.class) {
                if (instance == null) {
                    instance = new OrderManager();
                }
            }
        }
        return instance;
    }

    // ─── Управление заказами ────────────────────────────────────────────────

    public Order placeOrder(Order order) {
        orders.put(order.getId(), order);
        notifyObservers(order, "NEW_ORDER");
        return order;
    }

    public void updateStatus(String orderId, Order.Status newStatus) {
        Order order = orders.get(orderId);
        if (order == null) throw new NoSuchElementException("Заказ " + orderId + " не найден");
        order.setStatus(newStatus);
        notifyObservers(order, "STATUS_CHANGED");
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrder(String id) {
        return orders.get(id);
    }

    // ─── Observer поддержка ─────────────────────────────────────────────────

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(Order order, String event) {
        for (OrderObserver observer : observers) {
            observer.onOrderEvent(order, event);
        }
    }
}
