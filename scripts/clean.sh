#!/bin/bash

# HideArea 環境クリーンアップスクリプト

set -e

echo "========================================"
echo "HideArea 環境をクリーンアップします"
echo "========================================"

echo ""
read -p "すべてのDockerコンテナとボリュームを削除しますか？ (y/N): " confirm

if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    docker compose down -v
    echo ""
    echo "クリーンアップ完了！"
else
    echo ""
    echo "キャンセルしました。"
fi

echo ""
