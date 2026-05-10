package com.moda.patterns.structural;

/**
 * DECORATOR паттерн — реализации и фабрика (Structural Pattern #1)
 * ─────────────────────────────────────────────────────────────────
 * Интерфейс PricingComponent вынесен в PricingComponent.java
 */

// ─── Base: базовая стоимость товаров ────────────────────────────────────────
class BasePrice implements PricingComponent {
    private final double itemsTotal;

    public BasePrice(double itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    @Override public double getPrice()       { return itemsTotal; }
    @Override public String getDescription() { return "Items: $" + String.format("%.2f", itemsTotal); }
}

// ─── Abstract Decorator ──────────────────────────────────────────────────────
abstract class PricingDecorator implements PricingComponent {
    protected final PricingComponent wrapped;

    protected PricingDecorator(PricingComponent wrapped) {
        this.wrapped = wrapped;
    }
}

// ─── Concrete Decorator #1: Стандартная доставка ────────────────────────────
class StandardDeliveryDecorator extends PricingDecorator {
    private static final double COST = 5.99;

    public StandardDeliveryDecorator(PricingComponent wrapped) {
        super(wrapped);
    }

    @Override public double getPrice()       { return wrapped.getPrice() + COST; }
    @Override public String getDescription() {
        return wrapped.getDescription() + "\n+ Standard Delivery: $" + COST;
    }
}

// ─── Concrete Decorator #2: Экспресс доставка ───────────────────────────────
class ExpressDeliveryDecorator extends PricingDecorator {
    private static final double COST = 14.99;

    public ExpressDeliveryDecorator(PricingComponent wrapped) {
        super(wrapped);
    }

    @Override public double getPrice()       { return wrapped.getPrice() + COST; }
    @Override public String getDescription() {
        return wrapped.getDescription() + "\n+ Express Delivery: $" + COST;
    }
}

// ─── Concrete Decorator #3: Подарочная упаковка ─────────────────────────────
class GiftWrapDecorator extends PricingDecorator {
    private static final double COST = 3.99;

    public GiftWrapDecorator(PricingComponent wrapped) {
        super(wrapped);
    }

    @Override public double getPrice()       { return wrapped.getPrice() + COST; }
    @Override public String getDescription() {
        return wrapped.getDescription() + "\n+ Gift Wrap 🎀: $" + COST;
    }
}

// ─── Фабрика для удобного создания цепочки ──────────────────────────────────
/**
 * PricingCalculator скрывает детали создания декораторов.
 * Клиентский код просто передаёт опции, не зная о декораторах.
 */
public class PricingCalculator {
    public static PricingComponent calculate(double itemsTotal, String deliveryType, boolean giftWrap) {
        PricingComponent pricing = new BasePrice(itemsTotal);

        // Добавляем слои в runtime
        if ("express".equalsIgnoreCase(deliveryType)) {
            pricing = new ExpressDeliveryDecorator(pricing);
        } else {
            pricing = new StandardDeliveryDecorator(pricing);
        }

        if (giftWrap) {
            pricing = new GiftWrapDecorator(pricing);
        }

        return pricing;
    }
}
