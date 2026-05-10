package com.moda.patterns.behavioral;

import com.moda.model.Order;

/**
 * OBSERVER паттерн — конкретные наблюдатели и фабрика (Behavioral Pattern #1)
 * ────────────────────────────────────────────────────────────────────────────
 * Интерфейс OrderObserver вынесен в отдельный файл OrderObserver.java
 * (правило Java: один public тип = один файл).
 */

// ─── Concrete Observer #1: Лог в консоль ────────────────────────────────────
class ConsoleLogger implements OrderObserver {
    @Override
    public void onOrderEvent(Order order, String event) {
        System.out.printf("[LOG] %s | Order #%s | Customer: %s | Status: %s | Total: $%.2f%n",
                event, order.getId(), order.getCustomerName(),
                order.getStatus(), order.getTotalPrice());
    }
}

// ─── Concrete Observer #2: Имитация Email ───────────────────────────────────
class EmailNotifier implements OrderObserver {
    @Override
    public void onOrderEvent(Order order, String event) {
        if ("NEW_ORDER".equals(event)) {
            System.out.printf("[EMAIL] → %s: Your order #%s has been placed! Total: $%.2f%n",
                    order.getCustomerName(), order.getId(), order.getTotalPrice());
        } else if ("STATUS_CHANGED".equals(event)) {
            System.out.printf("[EMAIL] → %s: Order #%s status updated to: %s%n",
                    order.getCustomerName(), order.getId(), order.getStatus());
        }
    }
}

// ─── Concrete Observer #3: Имитация SMS ─────────────────────────────────────
class SmsNotifier implements OrderObserver {
    @Override
    public void onOrderEvent(Order order, String event) {
        if ("STATUS_CHANGED".equals(event) && order.getStatus() == Order.Status.SHIPPED) {
            System.out.printf("[SMS] → %s: Your order #%s is on the way! 🚚%n",
                    order.getCustomerName(), order.getId());
        }
    }
}

// ─── Фабрика наблюдателей ────────────────────────────────────────────────────
/**
 * Создаёт всех наблюдателей для регистрации в OrderManager.
 */
public class ObserverFactory {
    public static OrderObserver consoleLogger() { return new ConsoleLogger(); }
    public static OrderObserver emailNotifier() { return new EmailNotifier(); }
    public static OrderObserver smsNotifier()   { return new SmsNotifier(); }
}
