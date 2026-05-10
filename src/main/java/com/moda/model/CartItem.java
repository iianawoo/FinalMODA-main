package com.moda.model;

/**
 * Интерфейс для COMPOSITE паттерна (Structural #2).
 *
 * И одиночный товар (SingleItem), и набор товаров (Bundle)
 * реализуют этот интерфейс — их можно использовать одинаково.
 *
 * COMPILE TIME: компилятор гарантирует, что все CartItem имеют getName() и getPrice().
 */
public interface CartItem {
    String getName();
    double getPrice();
    String getDescription();   // для отображения в UI
    boolean isBundle();
}
