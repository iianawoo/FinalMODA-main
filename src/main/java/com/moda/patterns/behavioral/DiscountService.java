package com.moda.patterns.behavioral;

/**
 * STRATEGY паттерн — реализации и контекст (Behavioral Pattern #2)
 * ─────────────────────────────────────────────────────────────────
 * Интерфейс DiscountStrategy находится в DiscountStrategy.java.
 *
 * Промокоды:
 * WELCOME10 → -10% (новый клиент)
 * VIP20 → -20% (VIP)
 * SALE30 → -30% (сезонная)
 *
 * COMPILE TIME: DiscountService зависит от интерфейса DiscountStrategy.
 * RUNTIME: нужная стратегия выбирается по промокоду через switch.
 */

// ─── Strategy #1: Нет скидки ─────────────────────────────────────────────────
class NoDiscount implements DiscountStrategy {
    @Override
    public double apply(double price) {
        return price;
    }

    @Override
    public String getDescription() {
        return "No discount";
    }
}

// ─── Strategy #2: Скидка для новых клиентов (10%) ────────────────────────────
class NewCustomerDiscount implements DiscountStrategy {
    @Override
    public double apply(double price) {
        return Math.round(price * 0.90 * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return "New Customer -10%";
    }
}

// ─── Strategy #3: VIP скидка (20%) ───────────────────────────────────────────
class VipDiscount implements DiscountStrategy {
    @Override
    public double apply(double price) {
        return Math.round(price * 0.80 * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return "VIP -20%";
    }
}

// ─── Strategy #4: Сезонная распродажа (30%) ──────────────────────────────────
class SeasonSaleDiscount implements DiscountStrategy {
    @Override
    public double apply(double price) {
        return Math.round(price * 0.70 * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return "Season Sale -30%";
    }
}

// ─── Context ─────────────────────────────────────────────────────────────────

public class DiscountService {

    /**
     * Вспомогательный record — передаёт информацию о стратегии в HTTP-ответ.
     * valid=false если промокод не распознан.
     */
    public record StrategyInfo(String description, boolean valid) {
    }

    /**
     * Возвращает стратегию по коду + флаг valid.
     * Используется сервером чтобы сообщить фронту — код сработал или нет.
     */
    public static StrategyInfo fromCodeWithInfo(String code) {
        if (code == null || code.isBlank())
            return new StrategyInfo("No discount", false);
        return switch (code.trim().toUpperCase()) {
            case "WELCOME10" -> new StrategyInfo("New Customer -10%", true);
            case "VIP20" -> new StrategyInfo("VIP -20%", true);
            case "SALE30" -> new StrategyInfo("Season Sale -30%", true);
            default -> new StrategyInfo("Invalid promo code", false);
        };
    }

    /**
     * Выбирает и возвращает стратегию (используется внутри applyDiscount).
     */
    public static DiscountStrategy fromCode(String promoCode) {
        if (promoCode == null || promoCode.isBlank())
            return new NoDiscount();
        return switch (promoCode.trim().toUpperCase()) {
            case "WELCOME10" -> new NewCustomerDiscount();
            case "VIP20" -> new VipDiscount();
            case "SALE30" -> new SeasonSaleDiscount();
            default -> new NoDiscount();
        };
    }

    /**
     * Применяет скидку и логирует в консоль (видно в IntelliJ).
     */
    public double applyDiscount(double price, String promoCode) {
        DiscountStrategy strategy = fromCode(promoCode);
        double discounted = strategy.apply(price);
        System.out.printf("[DISCOUNT] Strategy: %-20s | Before: $%.2f | After: $%.2f%n",
                strategy.getDescription(), price, discounted);
        return discounted;
    }
}