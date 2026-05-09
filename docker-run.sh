#!/usr/bin/env bash
set -euo pipefail

docker run -d \
  --name miniapp-max \
  -p 8080:8080 \
  -v "$(pwd)/data:/opt/app/data" \
  -e APP_PUBLIC_BASE_URL="http://localhost:8080" \
  -e APP_BOOTSTRAP_ADMIN_ID="1" \
  -e APP_DEFAULT_PAYMENT_DETAILS="Карта: 0000 0000 0000 0000 | Телефон: +7XXXXXXXXXX" \
  -e APP_CITY_DELIVERY_FEE="1000.00" \
  -e MAX_BOT_TOKEN="" \
  -e MAX_WEBHOOK_SECRET="" \
  -e MAX_MINIAPP_URL="http://localhost:8080" \
  miniapp-max:latest
