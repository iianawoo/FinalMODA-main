package com.moda.patterns.structural;

/**
 * DECORATOR паттерн — общий интерфейс компонента (Structural Pattern #1)
 *
 * COMPILE TIME: все декораторы обязаны реализовать getPrice() и getDescription().
 * RUNTIME: цепочка вызовов проходит через все слои декораторов.
 */
public interface PricingComponent {
    double getPrice();
    String getDescription();
}
