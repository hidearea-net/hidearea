#!/bin/bash

# HideArea 開発環境停止スクリプト

set -e

echo "========================================"
echo "HideArea 開発環境を停止します"
echo "========================================"

docker compose down

echo ""
echo "停止しました！"
echo ""
