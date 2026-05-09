# MAX mini app: каталог стройматериалов

Готовый MVP на Java + SQLite + Docker:
- мини-апп с вкладками `Каталог`, `Fix Price 🔥`, `Общая информация`, `Админка`;
- карточка товара, расчет суммы заказа по количеству (шт/куб.м);
- при заказе клиент указывает количество только в доступной для товара единице измерения;
- сбор ФИО, телефона, адреса;
- интеграция оплаты через ЮKassa (`payment_url` и webhook обработки статусов);
- заказ попадает в админку;
- админ может добавлять товары, посты, админов, смотреть пользователей и заказы;
- для товара в админке выбирается единица измерения и соответствующий остаток;
- товары со скидкой добавляются в отдельный раздел `Fix Price 🔥`.

## Стек
- Java 21
- Spring Boot 3
- Spring Data JPA
- SQLite
- Vanilla JS mini app

## Пошаговый запуск (локально, с нуля)

### Шаг 1. Откройте терминал

```bash
cd /Users/dmitry/Desktop/miniappMax
```

### Шаг 2. Проверьте Java и Maven

```bash
java -version
mvn -version
```

### Шаг 3. Соберите проект

```bash
mvn -DskipTests clean package
```

### Шаг 4. Запустите приложение

```bash
mvn spring-boot:run
```

### Шаг 5. Откройте mini app

Откройте в браузере:
- [http://localhost:8080](http://localhost:8080)

### Шаг 6. Задайте админский MAX ID

По умолчанию bootstrap-админ:
- `1`

Если нужен другой ID, перезапустите так:

```bash
APP_BOOTSTRAP_ADMIN_ID=123456 mvn spring-boot:run
```

### Шаг 7. Проверка сценария в UI

1. Введите свой MAX ID в поле сверху и нажмите `Сохранить`.
2. В `Админка` добавьте товар:
   - фото,
   - название,
   - описание,
   - цену,
   - раздел (`Каталог` или `Fix Price 🔥`),
   - формат количества (`Только шт` / `Только кубы` / `И шт, и кубы`),
   - остаток в соответствующей единице.
3. Перейдите в `Каталог` или `Fix Price 🔥`, откройте товар.
4. Нажмите `Заказать`, введите количество (в доступной единице), ФИО, телефон, адрес.
5. Перейдите по ссылке оплаты.

### Шаг 8. Остановка приложения

В окне с `spring-boot:run` нажмите:

```text
Ctrl + C
```

## Пошаговый запуск в Docker (локально)

### Шаг 1. Сборка образа

```bash
cd /Users/dmitry/Desktop/miniappMax
docker build -t miniapp-max:latest .
```

### Шаг 2. Создайте папку для данных

```bash
mkdir -p /Users/dmitry/Desktop/miniappMax/data
```

### Шаг 3. Запустите контейнер

```bash
docker run -d \
  --name miniapp-max \
  -p 8080:8080 \
  -v "/Users/dmitry/Desktop/miniappMax/data:/opt/app/data" \
  -e APP_PUBLIC_BASE_URL="http://localhost:8080" \
  -e APP_BOOTSTRAP_ADMIN_ID="1" \
  -e APP_PAYMENT_MOCK_ENABLED="true" \
  -e MAX_BOT_TOKEN="" \
  -e MAX_WEBHOOK_SECRET="" \
  -e MAX_MINIAPP_URL="http://localhost:8080" \
  -e YOOKASSA_SHOP_ID="" \
  -e YOOKASSA_SECRET_KEY="" \
  -e YOOKASSA_RETURN_URL="http://localhost:8080/?payment=done" \
  miniapp-max:latest
```

### Шаг 4. Проверьте, что контейнер запущен

```bash
docker ps --filter name=miniapp-max
```

### Шаг 5. Откройте mini app

- [http://localhost:8080](http://localhost:8080)

### Шаг 6. Просмотр логов

```bash
docker logs -f miniapp-max
```

### Шаг 7. Остановить контейнер

```bash
docker stop miniapp-max
```

### Шаг 8. Удалить контейнер

```bash
docker rm miniapp-max
```

## Пошаговый запуск в проде (MAX + ЮKassa)

### Шаг 1. Поднимите приложение на HTTPS-домене

Пример:
- `https://your-domain.ru`

### Шаг 2. Запустите контейнер с прод-переменными

```bash
docker run -d \
  --name miniapp-max \
  -p 8080:8080 \
  -v "$(pwd)/data:/opt/app/data" \
  -e APP_PUBLIC_BASE_URL="https://your-domain.ru" \
  -e APP_BOOTSTRAP_ADMIN_ID="123456" \
  -e APP_PAYMENT_MOCK_ENABLED="false" \
  -e MAX_BOT_TOKEN="your_max_token" \
  -e MAX_WEBHOOK_SECRET="your_secret" \
  -e MAX_MINIAPP_URL="https://your-domain.ru" \
  -e YOOKASSA_SHOP_ID="shop_id" \
  -e YOOKASSA_SECRET_KEY="secret_key" \
  -e YOOKASSA_RETURN_URL="https://your-domain.ru/?payment=done" \
  miniapp-max:latest
```

### Шаг 3. Настройте webhook ЮKassa

В кабинете ЮKassa укажите URL:
- `https://your-domain.ru/api/payments/yookassa/webhook`

### Шаг 4. Настройте webhook MAX

Приложение отправляет подписку автоматически при старте, если:
1. указан `MAX_BOT_TOKEN`;
2. `APP_PUBLIC_BASE_URL` начинается с `https://`.

## Основные API

- `GET /api/catalog` — список товаров каталога
- `GET /api/fix-price` — список товаров раздела `Fix Price 🔥`
- `GET /api/catalog/{id}` — карточка товара
- `GET /api/info` — посты раздела `Общая информация`
- `POST /api/orders` — оформить заказ
- `POST /api/payments/yookassa/webhook` — webhook от ЮKassa
- `POST /api/max/webhook` — webhook от MAX

Админские (нужен заголовок `X-User-Id` с админским MAX ID):
- `GET /api/admin/users`
- `GET /api/admin/orders`
- `GET /api/admin/admins`
- `POST /api/admin/admins`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `POST /api/admin/uploads` (multipart `file`)
- `POST /api/admin/info-posts`

Пример `POST /api/admin/products`:

```json
{
  "name": "Пеноблок D600",
  "description": "Блок стеновой",
  "imageUrl": "https://...",
  "price": 120.0,
  "oldPrice": 150.0,
  "unitMode": "PCS_ONLY",
  "stockPcs": 5000,
  "stockCubicMeters": null,
  "fixPrice": true,
  "active": true
}
```

## Важно по интеграции MAX

Согласно официальной документации MAX API:
- отправка сообщения: `POST https://platform-api.max.ru/messages`;
- подписка webhook: `POST https://platform-api.max.ru/subscriptions`;
- токен передается в заголовке `Authorization`.

## Важно по ЮKassa

В проде отключите mock-режим:
- `APP_PAYMENT_MOCK_ENABLED=false`

И укажите:
- `YOOKASSA_SHOP_ID`
- `YOOKASSA_SECRET_KEY`
- `YOOKASSA_RETURN_URL`
