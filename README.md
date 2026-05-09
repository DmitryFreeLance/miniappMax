# MAX mini app: каталог стройматериалов

MVP на Java + SQLite + Docker для MAX:
- каталог + Fix Price;
- карточка товара;
- заказ с выбором единицы (`шт` / `куб.м`), доставки и способа оплаты;
- варианты оплаты:
  - `Сейчас (карта)` — клиент видит реквизиты, переводит и нажимает `Готово`;
  - `При получении`;
- заказы попадают в админку;
- админка: товары, посты, пользователи, админы, заказы, реквизиты оплаты;
- при новом заказе отправляется уведомление админам через MAX Bot API.

## Стек
- Java 21
- Spring Boot 3
- Spring Data JPA
- SQLite
- Vanilla JS

## Запуск локально

```bash
cd /Users/dmitry/Desktop/miniappMax
mvn -DskipTests clean package
mvn spring-boot:run
```

Открыть:
- [http://localhost:8080](http://localhost:8080)

## Запуск в Docker (локально)

```bash
cd /Users/dmitry/Desktop/miniappMax
docker build -t miniapp-max:latest .

mkdir -p /Users/dmitry/Desktop/miniappMax/data

docker rm -f miniapp-max 2>/dev/null || true
docker run -d \
  --name miniapp-max \
  --restart unless-stopped \
  -p 8080:8080 \
  -v "/Users/dmitry/Desktop/miniappMax/data:/opt/app/data" \
  -e APP_PUBLIC_BASE_URL="http://localhost:8080" \
  -e MAX_MINIAPP_URL="http://localhost:8080" \
  -e APP_BOOTSTRAP_ADMIN_ID="188421258" \
  -e APP_CITY_DELIVERY_FEE="1000.00" \
  -e APP_DEFAULT_PAYMENT_DETAILS="Карта: 0000 0000 0000 0000\\nТелефон: +7XXXXXXXXXX" \
  -e MAX_BOT_TOKEN="" \
  -e MAX_WEBHOOK_SECRET="" \
  miniapp-max:latest
```

## Запуск на сервере (через `docker run`)

```bash
cd /root/miniapp

docker build -t miniapp-max:latest .

docker rm -f miniapp-max 2>/dev/null || true
docker run -d \
  --name miniapp-max \
  --restart unless-stopped \
  -p 127.0.0.1:18081:8080 \
  -v /opt/miniapp-max/data:/opt/app/data \
  -e APP_PUBLIC_BASE_URL="https://profishina.moscow" \
  -e MAX_MINIAPP_URL="https://profishina.moscow" \
  -e APP_BOOTSTRAP_ADMIN_ID="188421258" \
  -e APP_CITY_DELIVERY_FEE="1000.00" \
  -e APP_DEFAULT_PAYMENT_DETAILS="Карта: 0000 0000 0000 0000\\nТелефон: +7XXXXXXXXXX" \
  -e MAX_BOT_TOKEN="YOUR_MAX_BOT_TOKEN" \
  -e MAX_WEBHOOK_SECRET="YOUR_MAX_WEBHOOK_SECRET" \
  miniapp-max:latest
```

## Переменные окружения

Обязательные:
- `APP_PUBLIC_BASE_URL`
- `MAX_MINIAPP_URL`
- `APP_BOOTSTRAP_ADMIN_ID`

Для уведомлений и кнопки в боте:
- `MAX_BOT_TOKEN`
- `MAX_WEBHOOK_SECRET`

Опционально:
- `APP_CITY_DELIVERY_FEE` (по умолчанию `1000.00`)
- `APP_DEFAULT_PAYMENT_DETAILS` (стартовые реквизиты; потом можно поменять в админке)

## API

Публичные:
- `GET /api/catalog`
- `GET /api/fix-price`
- `GET /api/catalog/{id}`
- `GET /api/info`
- `GET /api/orders/payment-details`
- `POST /api/orders`

Админские (`X-User-Id` с MAX ID админа):
- `GET /api/admin/users`
- `GET /api/admin/orders`
- `GET /api/admin/admins`
- `POST /api/admin/admins`
- `GET /api/admin/products`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`
- `POST /api/admin/uploads`
- `GET /api/admin/info-posts`
- `POST /api/admin/info-posts`
- `DELETE /api/admin/info-posts/{id}`
- `GET /api/admin/settings/payment-details`
- `PUT /api/admin/settings/payment-details`

Webhook MAX:
- `POST /api/max/webhook`
