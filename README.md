# 🌸 MODA — Korean Fashion Shop
### Design Patterns Project | Java + HTML/CSS/JS

---

## 📁 СТРУКТУРА ПРОЕКТА

```
moda/
├── pom.xml                                          ← Maven конфигурация
└── src/main/
    ├── java/com/moda/
    │   ├── Main.java                                ← Точка входа
    │   ├── model/
    │   │   ├── Product.java                         ← Модель товара
    │   │   ├── Order.java                           ← Модель заказа + Builder
    │   │   ├── CartItem.java                        ← Интерфейс (Composite)
    │   │   └── CartItemFactory.java                 ← Single/Bundle (Composite)
    │   ├── patterns/
    │   │   ├── creational/
    │   │   │   ├── OrderManager.java                ← Singleton #1
    │   │   │   └── ProductCatalog.java              ← Singleton #2
    │   │   ├── structural/
    │   │   │   ├── PricingCalculator.java           ← Decorator
    │   │   │   └── ShopFacade.java                  ← Facade
    │   │   └── behavioral/
    │   │       ├── ObserverFactory.java             ← Observer
    │   │       └── DiscountService.java             ← Strategy
    │   └── server/
    │       └── ModaServer.java                      ← HTTP сервер
    └── resources/
        └── index.html                               ← Фронтенд
```

---

## 🚀 КАК ЗАПУСТИТЬ В INTELLIJ IDEA (ПОШАГОВО)

### Шаг 1 — Открыть проект
1. Открой IntelliJ IDEA
2. **File → Open** → выбери папку `moda` (ту, где лежит `pom.xml`)
3. IntelliJ спросит "Trust this project?" → нажми **Trust Project**
4. Подожди пока IntelliJ загрузит Maven зависимости (внизу будет прогресс-бар)
   - Если не загружает автоматически: правой кнопкой на `pom.xml` → **Maven → Reload project**

### Шаг 2 — Проверить JDK
1. **File → Project Structure** (Ctrl+Alt+Shift+S)
2. В разделе **Project** убедись что SDK = **Java 17** или выше
3. Если нет — нажми **Add SDK → Download JDK** → выбери версию 17+

### Шаг 3 — Запустить
1. Найди файл `src/main/java/com/moda/Main.java`
2. Открой его
3. Нажми зелёный треугольник ▶ слева от `public static void main`
   — ИЛИ — нажми **Shift+F10**
4. В консоли IntelliJ появится:
   ```
   ╔══════════════════════════════════╗
   ║  🌸 MODA Shop Server started!    ║
   ║  http://localhost:8080            ║
   ╚══════════════════════════════════╝
   ```

### Шаг 4 — Открыть UI
1. Открой любой браузер (Chrome, Firefox, Edge)
2. Перейди по адресу: **http://localhost:8080**
3. Ты увидишь корейский магазин MODA 🌸

---

## 🛍 КАК ПОЛЬЗОВАТЬСЯ ПРИЛОЖЕНИЕМ

### Добавить товары в корзину
- На главной странице кликай на карточки товаров
- Или нажимай кнопку **+ Add to Cart**
- Иконка корзины вверху показывает количество товаров

### Оформить заказ
1. Нажми кнопку **Cart** вверху → откроется панель корзины
2. Нажми **Checkout →**
3. Заполни имя и адрес
4. Выбери доставку: Standard ($5.99) или Express ($14.99)
5. По желанию включи Gift Wrap 🎀 (+$3.99)
6. По желанию введи промокод (см. ниже)
7. Нажми **Place Order 🌸**

### Промокоды (Strategy паттерн в действии)
| Код | Скидка |
|-----|--------|
| `WELCOME10` | -10% (новый клиент) |
| `VIP20` | -20% (VIP) |
| `SALE30` | -30% (сезонная) |

### Управление заказами
- Перейди на страницу **Orders**
- Все оформленные заказы отображаются там
- Для каждого заказа можно изменить статус: PENDING → CONFIRMED → SHIPPED → DELIVERED
- При смене статуса в консоли IntelliJ появятся уведомления (Observer паттерн)

---

## 🎯 7 ПАТТЕРНОВ — ДЛЯ ЗАЩИТЫ

### 1. SINGLETON (Creational)
**Файлы:** `OrderManager.java`, `ProductCatalog.java`

**Что говорить на защите:**
> "Singleton гарантирует, что объект создаётся ровно один раз.
> У OrderManager приватный конструктор — это проверяется на compile time,
> компилятор не даст вызвать `new OrderManager()` снаружи класса.
> В runtime объект создаётся при первом вызове `getInstance()` —
> это называется lazy initialization. Double-checked locking обеспечивает
> безопасность при многопоточности."

**Compile time:** `private OrderManager()` — компилятор запрещает создание извне.
**Runtime:** объект создаётся один раз, все последующие вызовы возвращают тот же экземпляр.

---

### 2. BUILDER (Creational)
**Файл:** `Order.java` (внутренний класс `Order.Builder`)

**Что говорить:**
> "Builder нужен когда объект имеет много параметров — в нашем случае 6.
> Без Builder пришлось бы писать конструктор с 6 параметрами и легко перепутать
> их порядок. Builder позволяет создавать объект цепочкой вызовов:
> `new Order.Builder().customerName("Kim").address("Seoul")...build()`.
> Объект создаётся только при вызове `build()`, до этого идёт только накопление данных."

**Compile time:** компилятор знает типы всех полей Builder'а.
**Runtime:** `build()` вызывается в ShopFacade, объект Order создаётся только тогда.

---

### 3. DECORATOR (Structural)
**Файл:** `PricingCalculator.java`

**Что говорить:**
> "Decorator добавляет поведение объекту не меняя его класс.
> У нас базовая цена (BasePrice) оборачивается в декораторы доставки и упаковки.
> Каждый декоратор реализует интерфейс PricingComponent и хранит ссылку на предыдущий.
> При вызове `getPrice()` каждый слой добавляет свою стоимость.
> Цепочка формируется в runtime зависимости от выбора пользователя."

**Compile time:** все декораторы реализуют `PricingComponent` — типобезопасность.
**Runtime:** `calculate(total, "express", true)` создаёт цепочку: Base → Express → GiftWrap.

---

### 4. COMPOSITE (Structural)
**Файлы:** `CartItem.java`, `CartItemFactory.java`

**Что говорить:**
> "Composite позволяет работать с одиночными объектами и коллекциями одинаково.
> `CartItem` — общий интерфейс. `SingleItem` — один товар, `Bundle` — набор товаров.
> Код, который считает сумму корзины, не знает — это один товар или набор.
> Он просто вызывает `getPrice()` у каждого CartItem."

**Compile time:** `interface CartItem` с методами `getName()`, `getPrice()` — всё статически типизировано.
**Runtime:** Bundle рекурсивно считает цену всех вложенных элементов.

---

### 5. FACADE (Structural)
**Файл:** `ShopFacade.java`

**Что говорить:**
> "Facade скрывает сложность системы за простым интерфейсом.
> HTTP-сервер не знает о Builder, Decorator, Composite, Singleton.
> Он просто вызывает `facade.placeOrder(name, address, ids, delivery, giftWrap)`.
> Facade сам координирует все компоненты в правильном порядке."

**Compile time:** `ModaServer` импортирует только `ShopFacade`, не 5 разных классов.
**Runtime:** Facade вызывает компоненты: Catalog → Builder → PricingCalculator → OrderManager.

---

### 6. OBSERVER (Behavioral)
**Файл:** `ObserverFactory.java`, используется в `OrderManager.java`

**Что говорить:**
> "Observer (он же publish-subscribe) позволяет объекту уведомлять подписчиков
> без знания кто они. `OrderManager` — это Subject. Когда заказ создаётся или
> меняется статус, он вызывает `notifyObservers()`. Три наблюдателя —
> ConsoleLogger, EmailNotifier, SmsNotifier — реагируют по-своему.
> Можно добавить нового наблюдателя не меняя OrderManager."

**Compile time:** `OrderManager` зависит только от интерфейса `OrderObserver`.
**Runtime:** наблюдатели регистрируются при старте сервера, уведомления идут автоматически.

---

### 7. STRATEGY (Behavioral)
**Файл:** `DiscountService.java`

**Что говорить:**
> "Strategy позволяет менять алгоритм в runtime. У нас 4 стратегии скидки.
> Без Strategy в `DiscountService` был бы большой if/else: `if code == WELCOME10...
> else if code == VIP20...`. Strategy выносит каждый алгоритм в отдельный класс.
> Можно добавить новую скидку не меняя `DiscountService` — это Open/Closed Principle."

**Compile time:** все стратегии реализуют `DiscountStrategy` — типобезопасность.
**Runtime:** `fromCode("VIP20")` возвращает `new VipDiscount()` — выбор происходит динамически.

---

## 🔄 COMPILE TIME vs RUNTIME — КРАТКАЯ ШПАРГАЛКА

| | Compile Time | Runtime |
|---|---|---|
| Singleton | `private` конструктор запрещён компилятором | Экземпляр создаётся при первом `getInstance()` |
| Builder | Типы полей известны, цепочка методов проверена | `build()` создаёт объект |
| Decorator | Интерфейс `PricingComponent` проверен | Цепочка декораторов собирается по опциям |
| Composite | Интерфейс `CartItem` проверен | Bundle рекурсивно считает цену |
| Facade | Сервер зависит только от Facade | Facade координирует компоненты |
| Observer | `OrderManager` зависит от интерфейса | Наблюдатели уведомляются при событии |
| Strategy | Все стратегии реализуют интерфейс | Нужная стратегия выбирается по промокоду |

---

## ⚠️ ЧАСТЫЕ ПРОБЛЕМЫ

**"Port already in use"**
→ В IntelliJ нажми красный квадрат ⏹ чтобы остановить предыдущий запуск, потом запусти снова.

**"index.html not found"**
→ Убедись что `src/main/resources/index.html` существует.
→ В IntelliJ: правой кнопкой на `pom.xml` → Maven → Reload project.
→ Build → Rebuild Project.

**Сайт открывается но товары не загружаются**
→ Проверь что сервер запущен (есть сообщение в консоли).
→ Попробуй открыть `http://localhost:8080/api/products` в браузере напрямую.

**Не работает Java 17 (метод .toList())**
→ File → Project Structure → Project SDK → убедись что выбрана Java 17+.
