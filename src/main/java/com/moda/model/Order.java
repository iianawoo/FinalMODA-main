package com.moda.model;

import java.util.List;
import java.util.UUID;

/**
 * Объект заказа. Создаётся через Builder (паттерн #2).
 * Статус меняется через OrderManager — Observer уведомляет подписчиков.
 */
public class Order {
    public enum Status { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }

    private final String id;
    private final String customerName;
    private final String address;
    private final List<CartItem> items;
    private final String deliveryType;   // "standard" / "express"
    private final boolean giftWrap;
    private double totalPrice;
    private Status status;

    // Приватный конструктор — создаём только через Builder
    private Order(Builder builder) {
        this.id           = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerName = builder.customerName;
        this.address      = builder.address;
        this.items        = builder.items;
        this.deliveryType = builder.deliveryType;
        this.giftWrap     = builder.giftWrap;
        this.totalPrice   = builder.totalPrice;
        this.status       = Status.PENDING;
    }

    // ─── Геттеры ────────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getCustomerName() { return customerName; }
    public String getAddress()      { return address; }
    public List<CartItem> getItems(){ return items; }
    public String getDeliveryType() { return deliveryType; }
    public boolean isGiftWrap()     { return giftWrap; }
    public double getTotalPrice()   { return totalPrice; }
    public Status getStatus()       { return status; }

    public void setStatus(Status status) { this.status = status; }
    public void setTotalPrice(double p)  { this.totalPrice = p; }

    // ─── BUILDER (Creational Pattern #2) ────────────────────────────────────
    /**
     * Builder позволяет собирать заказ шаг за шагом.
     * Без Builder пришлось бы передавать 6+ параметров в конструктор —
     * легко перепутать порядок, тяжело читать код.
     *
     * COMPILE TIME: компилятор знает все поля Builder'а и их типы.
     * RUNTIME: объект Order создаётся только при вызове build().
     */
    public static class Builder {
        private String customerName;
        private String address;
        private List<CartItem> items;
        private String deliveryType = "standard";
        private boolean giftWrap    = false;
        private double totalPrice;

        public Builder customerName(String name)    { this.customerName = name;  return this; }
        public Builder address(String address)      { this.address = address;    return this; }
        public Builder items(List<CartItem> items)  { this.items = items;        return this; }
        public Builder deliveryType(String type)    { this.deliveryType = type;  return this; }
        public Builder giftWrap(boolean wrap)       { this.giftWrap = wrap;      return this; }
        public Builder totalPrice(double price)     { this.totalPrice = price;   return this; }

        public Order build() {
            if (customerName == null || address == null || items == null || items.isEmpty()) {
                throw new IllegalStateException("Заказ не может быть создан: заполните имя, адрес и корзину");
            }
            return new Order(this);
        }
    }
}
