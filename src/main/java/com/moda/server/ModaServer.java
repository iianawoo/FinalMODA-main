package com.moda.server;

import com.moda.patterns.behavioral.DiscountService;
import com.moda.patterns.behavioral.ObserverFactory;
import com.moda.patterns.creational.OrderManager;
import com.moda.patterns.structural.ShopFacade;
import com.moda.model.Order;
import com.moda.model.Product;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

/**
 * Простой HTTP-сервер на встроенном com.sun.net.httpserver.
 * Никаких зависимостей — только JDK.
 *
 * Endpoints:
 * GET /api/products — список товаров
 * POST /api/order — оформить заказ или обновить статус
 * GET /api/orders — все заказы
 * POST /api/discount — применить промокод
 * GET / — фронтенд (index.html)
 */
public class ModaServer {

    private static final int PORT = 8083;
    private final ShopFacade facade = new ShopFacade();
    private final DiscountService discountService = new DiscountService();

    public void start() throws IOException {
        // Регистрируем Observer'ов (Behavioral Pattern #1)
        OrderManager om = OrderManager.getInstance();
        om.addObserver(ObserverFactory.consoleLogger());
        om.addObserver(ObserverFactory.emailNotifier());
        om.addObserver(ObserverFactory.smsNotifier());

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", this::serveIndex);
        server.createContext("/api/products", this::handleProducts);
        server.createContext("/api/order", this::handleOrder);
        server.createContext("/api/orders", this::handleOrders);
        server.createContext("/api/discount", this::handleDiscount);
        server.createContext("/images/",       this::serveImage);

        server.setExecutor(null);
        server.start();
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   MODA Shop Server started!      ║");
        System.out.println("║   http://localhost:" + PORT + "            ║");
        System.out.println("╚══════════════════════════════════╝");
    }

    // ─── Serve HTML ──────────────────────────────────────────────────────────

    private void serveIndex(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        InputStream is = getClass().getResourceAsStream("/index.html");
        if (is == null) {
            sendText(ex, 404, "index.html not found");
            return;
        }
        byte[] bytes = is.readAllBytes();
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    // ─── Serve Images ─────────────────────────────────────────────────────────

    private void serveImage(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        String path = "/images/" + ex.getRequestURI().getPath().replaceAll(".*/images/", "");
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) { ex.sendResponseHeaders(404, -1); return; }
        byte[] bytes = is.readAllBytes();
        String contentType = path.endsWith(".png") ? "image/png" : "image/jpeg";
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }


    // ─── GET /api/products ───────────────────────────────────────────────────

    private void handleProducts(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        if (!ex.getRequestMethod().equals("GET")) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        List<Product> products = facade.getProducts();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            json.append(String.format(Locale.US,
                    "{\"id\":\"%s\",\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"emoji\":\"%s\"}",
                    p.getId(), escape(p.getName()), p.getCategory(), p.getPrice(), p.getEmoji()));
            if (i < products.size() - 1)
                json.append(",");
        }
        json.append("]");
        sendJson(ex, 200, json.toString());
    }
    // ─── POST /api/order (также GET → список всех заказов) ──────────────────

    private void handleOrder(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);

        // --- НОВОЕ: Логируем каждый пришедший запрос ---
        System.out.println("[DEBUG] Incoming Request: " + ex.getRequestMethod() + " " + ex.getRequestURI());

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            ex.getResponseBody().close(); // Важно закрыть поток для OPTIONS
            return;
        }

        if (ex.getRequestMethod().equals("GET")) {
            handleOrders(ex);
            return;
        }
        if (!ex.getRequestMethod().equals("POST")) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("[DEBUG] Received body: " + body);


        Map<String, String> params = parseJson(body);

        // Обновление статуса заказа
        if (params.containsKey("orderId") && params.containsKey("status")) {
            try {
                Order order = facade.updateOrderStatus(params.get("orderId"), params.get("status"));
                sendJson(ex, 200, orderToJson(order));
            } catch (Exception e) {
                System.err.println("[ERROR] updateStatus: " + e.getMessage());
                sendJson(ex, 400, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
            return;
        }

        // Новый заказ
        try {
            String customerName = params.getOrDefault("customerName", "").trim();
            String address = params.getOrDefault("address", "").trim();
            String productIdsStr = params.getOrDefault("productIds", "").trim();
            String deliveryType = params.getOrDefault("deliveryType", "standard").trim();
            // giftWrap может прийти как строка "true" или булево true — обрабатываем оба
            // случая
            String giftWrapRaw = params.getOrDefault("giftWrap", "false").trim();
            boolean giftWrap = "true".equalsIgnoreCase(giftWrapRaw);
            String promoCode = params.getOrDefault("promoCode", "").trim();

            if (customerName.isEmpty())
                throw new IllegalArgumentException("Укажите имя");
            if (address.isEmpty())
                throw new IllegalArgumentException("Укажите адрес");
            if (productIdsStr.isEmpty())
                throw new IllegalArgumentException("Корзина пуста");

            List<String> productIds = Arrays.stream(productIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // ShopFacade координирует: Builder + Decorator + Composite + Singleton
            Order order = facade.placeOrder(customerName, address, productIds, deliveryType, giftWrap);

            // Strategy паттерн: применяем скидку если есть промокод
            if (!promoCode.isEmpty()) {
                double discounted = discountService.applyDiscount(order.getTotalPrice(), promoCode);
                order.setTotalPrice(discounted);
            }

            sendJson(ex, 200, orderToJson(order));

        } catch (Exception e) {
            System.err.println("[ERROR] placeOrder: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 400, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ─── GET /api/orders ─────────────────────────────────────────────────────

    private void handleOrders(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        List<Order> orders = facade.getOrders();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            json.append(orderToJson(orders.get(i)));
            if (i < orders.size() - 1)
                json.append(",");
        }
        json.append("]");
        sendJson(ex, 200, json.toString());
    }

    // ─── POST /api/discount ──────────────────────────────────────────────────

    private void handleDiscount(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        if (!ex.getRequestMethod().equals("POST")) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseJson(body);

        String code = params.getOrDefault("code", "").trim();
        double price = 0;
        try {
            price = Double.parseDouble(params.getOrDefault("price", "0"));
        } catch (NumberFormatException ignore) {
        }

        double discounted = discountService.applyDiscount(price, code);
        DiscountService.StrategyInfo info = DiscountService.fromCodeWithInfo(code);
        sendJson(ex, 200, String.format(
                "{\"original\":%.2f,\"discounted\":%.2f,\"description\":\"%s\",\"valid\":%b}",
                price, discounted, info.description(), info.valid()));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String orderToJson(Order o) {
        StringBuilder items = new StringBuilder("[");
        for (int i = 0; i < o.getItems().size(); i++) {
            var item = o.getItems().get(i);
            // Используем Locale.US для точки вместо запятой
            items.append(String.format(Locale.US, "{\"name\":\"%s\",\"price\":%.2f}",
                    escape(item.getName()), item.getPrice()));
            if (i < o.getItems().size() - 1)
                items.append(",");
        }
        items.append("]");
        return String.format(Locale.US,
                "{\"id\":\"%s\",\"customerName\":\"%s\",\"address\":\"%s\"," +
                        "\"deliveryType\":\"%s\",\"giftWrap\":%b,\"totalPrice\":%.2f," +
                        "\"status\":\"%s\",\"items\":%s}",
                o.getId(), escape(o.getCustomerName()), escape(o.getAddress()),
                o.getDeliveryType(), o.isGiftWrap(), o.getTotalPrice(),
                o.getStatus(), items);
    }


    private void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        // Разрешаем браузеру запомнить CORS-настройки на час
        ex.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }


    private void sendJson(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private void sendText(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    /**
     * Надёжный парсер JSON-объекта.
     * Корректно обрабатывает строки, булевы значения и числа.
     * Пример: {"name":"Alice","giftWrap":true,"price":29.99}
     */
    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank())
            return map;

        // Убираем внешние фигурные скобки
        String s = json.trim();
        if (s.startsWith("{"))
            s = s.substring(1);
        if (s.endsWith("}"))
            s = s.substring(0, s.length() - 1);

        // Разбираем пары key:value вручную, уважая строки в кавычках
        int i = 0;
        while (i < s.length()) {
            // Пропускаем пробелы и запятые
            while (i < s.length() && (s.charAt(i) == ',' || Character.isWhitespace(s.charAt(i))))
                i++;
            if (i >= s.length())
                break;

            // Читаем ключ
            String key = readToken(s, i);
            i += computeTokenLength(s, i, key);

            // Пропускаем ":"
            while (i < s.length() && (s.charAt(i) == ':' || Character.isWhitespace(s.charAt(i))))
                i++;

            // Читаем значение
            String value = readToken(s, i);
            i += computeTokenLength(s, i, value);

            map.put(key, value);
        }
        return map;
    }

    private String readToken(String s, int start) {
        if (start >= s.length())
            return "";
        if (s.charAt(start) == '"') {
            // Строка в кавычках
            int end = start + 1;
            while (end < s.length()) {
                if (s.charAt(end) == '\\') {
                    end += 2;
                    continue;
                }
                if (s.charAt(end) == '"') {
                    end++;
                    break;
                }
                end++;
            }
            return s.substring(start + 1, end - 1); // без кавычек
        } else {
            // Булево, число или null
            int end = start;
            while (end < s.length() && s.charAt(end) != ',' && s.charAt(end) != '}')
                end++;
            return s.substring(start, end).trim();
        }
    }

    private int computeTokenLength(String s, int start, String value) {
        if (start >= s.length())
            return 0;
        if (s.charAt(start) == '"')
            return value.length() + 2; // +2 за кавычки
        return value.length();
    }
}