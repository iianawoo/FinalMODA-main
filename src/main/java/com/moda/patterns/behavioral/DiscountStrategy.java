package com.moda.patterns.behavioral;

/**
 * STRATEGY паттерн — интерфейс (Behavioral Pattern #2)
 *
 * COMPILE TIME: все стратегии обязаны реализовать эти два метода.
 * RUNTIME: нужная стратегия выбирается по промокоду в DiscountService.
 */
public interface DiscountStrategy {
    double apply(double price);
    String getDescription();
}
