# スクリプトディレクトリ

このディレクトリには、HideAreaプロジェクトの開発・運用に役立つスクリプトが含まれています。

## セットアップスクリプト

### setup-dev-env.sh

開発環境を自動的にセットアップするスクリプトです。

**インストールされるもの:**
- Git
- Java 25 (SDKMAN経由)
- Node.js 20.x LTS (nvm経由)
- Docker & Docker Compose
- プロジェクト依存関係

**使い方:**

```bash
# プロジェクトルートで実行
./scripts/setup-dev-env.sh
```

**対応OS:**
- Ubuntu/Debian
- Arch Linux
- macOS (一部手動インストールが必要)

**注意事項:**
- スクリプト実行後、シェルの再起動またはログアウト/ログインが必要です
- Dockerグループへの追加を有効にするため、再ログインしてください
- macOSの場合、Docker Desktopは手動でインストールが必要です

**初回セットアップ後の手順:**

1. シェルを再起動:
   ```bash
   source ~/.bashrc  # または source ~/.zshrc
   ```

2. ログアウト/ログイン（Dockerグループ変更を反映）

3. 開発サーバー起動:
   ```bash
   ./scripts/start-dev.sh
   ```

## その他のスクリプト（今後追加予定）

### start-dev.sh
開発環境を起動するスクリプト（Docker Compose使用）

### start-test.sh
テスト環境を起動するスクリプト

### start-prod.sh
本番環境を起動するスクリプト

### clean.sh
ビルド成果物やキャッシュをクリーンアップするスクリプト

## トラブルシューティング

### Dockerの権限エラー

```
Got permission denied while trying to connect to the Docker daemon socket
```

**解決策:**
ログアウトして再ログインし、Dockerグループへの追加を有効化してください。

### SDKMANが見つからない

**解決策:**
シェルを再起動するか、以下を実行してください:
```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### nvmが見つからない

**解決策:**
シェルを再起動するか、以下を実行してください:
```bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
```

## 参考ドキュメント

詳細な手順については、以下のドキュメントを参照してください:
- [開発環境セットアップガイド](../docs/development/ENVIRONMENT_SETUP.md)
- [開発ガイドライン](../docs/development/DEVELOPMENT_GUIDE.md)
