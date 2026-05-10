package com.moda.patterns.behavioral;

import com.moda.model.Order;

/**
 * OBSERVER паттерн — интерфейс (Behavioral Pattern #1)
 *
 * COMPILE TIME: OrderManager зависит только от этого интерфейса, не от конкретных классов.
 * RUNTIME: ConcreteLogger, EmailNotifier, SmsNotifier регистрируются при старте.
 */
public interface OrderObserver {
    void onOrderEvent(Order order, String event);
}
