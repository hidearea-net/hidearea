#!/bin/bash

# HideArea 開発環境起動スクリプト

set -e

echo "========================================"
echo "HideArea 開発環境を起動します"
echo "========================================"

# .envファイルの存在確認
if [ ! -f .env ]; then
    echo ".envファイルが存在しません。.env.exampleをコピーします..."
    cp .env.example .env
    echo ".envファイルを作成しました。必要に応じて設定を変更してください。"
fi

# Docker Composeで起動
echo ""
echo "Docker Composeでサービスを起動しています..."
docker compose up -d postgres

echo ""
echo "データベースの起動を待っています..."
sleep 10

echo ""
echo "起動しました！"
echo ""
echo "サービスURL:"
echo "  - PostgreSQL: localhost:5432"
echo "  - Backend API: http://localhost:8080"
echo "  - Frontend: http://localhost:5173"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "停止するには: docker compose down"
echo "ログを見るには: docker compose logs -f"
echo ""
