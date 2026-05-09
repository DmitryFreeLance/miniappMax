#!/usr/bin/env bash
set -euo pipefail

docker run -d \
  --name miniapp-max \
  -p 8080:8080 \
  -v "$(pwd)/data:/opt/app/data" \
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
