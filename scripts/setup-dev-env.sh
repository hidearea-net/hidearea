#!/bin/bash

#######################################
# HideArea 開発環境セットアップスクリプト
#
# このスクリプトは以下をインストール・設定します:
# - Java 25 (SDKMAN使用)
# - Node.js 20.x LTS (nvm使用)
# - Docker & Docker Compose
# - 推奨開発ツール
#######################################

set -e  # エラーが発生したら即座に終了

# カラー定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ログ出力関数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# OS検出
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if [ -f /etc/debian_version ]; then
            OS="debian"
        elif [ -f /etc/redhat-release ]; then
            OS="redhat"
        elif [ -f /etc/arch-release ]; then
            OS="arch"
        else
            OS="linux"
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="mac"
    else
        log_error "サポートされていないOSです: $OSTYPE"
        exit 1
    fi
    log_info "検出されたOS: $OS"
}

# 既存インストールの確認
check_installed() {
    if command -v $1 &> /dev/null; then
        log_success "$1 は既にインストールされています (バージョン: $($1 --version 2>&1 | head -n1))"
        return 0
    else
        log_warning "$1 はインストールされていません"
        return 1
    fi
}

# Gitのインストール確認・インストール
install_git() {
    log_info "=== Gitのセットアップ ==="

    if check_installed git; then
        return 0
    fi

    case $OS in
        debian)
            sudo apt-get update
            sudo apt-get install -y git
            ;;
        redhat)
            sudo yum install -y git
            ;;
        arch)
            sudo pacman -S --noconfirm git
            ;;
        mac)
            brew install git
            ;;
    esac

    log_success "Gitのインストールが完了しました"
}

# SDKMAN & Java 25のインストール
install_java() {
    log_info "=== Java 25のセットアップ ==="

    # SDKMANのインストール確認
    if [ ! -d "$HOME/.sdkman" ]; then
        log_info "SDKMANをインストールしています..."
        curl -s "https://get.sdkman.io" | bash
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        log_success "SDKMANのインストールが完了しました"
    else
        log_success "SDKMANは既にインストールされています"
        source "$HOME/.sdkman/bin/sdkman-init.sh"
    fi

    # Java 25のインストール確認
    if sdk list java | grep -q "25.*installed"; then
        log_success "Java 25は既にインストールされています"
    else
        log_info "Java 25をインストールしています..."
        sdk install java 25-open
        sdk default java 25-open
        log_success "Java 25のインストールが完了しました"
    fi

    java -version
}

# nvm & Node.js 20.xのインストール
install_nodejs() {
    log_info "=== Node.js 20.x LTSのセットアップ ==="

    # nvmのインストール確認
    if [ ! -d "$HOME/.nvm" ]; then
        log_info "nvmをインストールしています..."
        curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash

        # nvmの環境変数を読み込み
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

        log_success "nvmのインストールが完了しました"
    else
        log_success "nvmは既にインストールされています"
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    fi

    # Node.js 20.xのインストール
    log_info "Node.js 20.x LTSをインストールしています..."
    nvm install 20
    nvm use 20
    nvm alias default 20

    log_success "Node.js $(node --version) のインストールが完了しました"
    log_success "npm $(npm --version) のインストールが完了しました"
}

# Dockerのインストール
install_docker() {
    log_info "=== Dockerのセットアップ ==="

    if check_installed docker; then
        log_info "Dockerサービスの状態を確認しています..."
        if systemctl is-active --quiet docker; then
            log_success "Dockerサービスは実行中です"
        else
            log_warning "Dockerサービスを起動しています..."
            sudo systemctl start docker
            sudo systemctl enable docker
        fi
        return 0
    fi

    case $OS in
        debian)
            log_info "Docker (Debian/Ubuntu)をインストールしています..."

            # 古いバージョンの削除
            sudo apt-get remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true

            # 必要なパッケージのインストール
            sudo apt-get update
            sudo apt-get install -y \
                ca-certificates \
                curl \
                gnupg \
                lsb-release

            # DockerのGPGキー追加
            sudo install -m 0755 -d /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            sudo chmod a+r /etc/apt/keyrings/docker.gpg

            # Dockerリポジトリ追加
            echo \
              "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
              $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

            # Dockerのインストール
            sudo apt-get update
            sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
            ;;

        arch)
            log_info "Docker (Arch Linux)をインストールしています..."
            sudo pacman -S --noconfirm docker docker-compose
            ;;

        mac)
            log_warning "Mac OSの場合は Docker Desktop を手動でインストールしてください"
            log_info "https://www.docker.com/products/docker-desktop"
            return 0
            ;;

        *)
            log_error "このOSでのDockerの自動インストールはサポートされていません"
            log_info "公式サイトからインストールしてください: https://docs.docker.com/get-docker/"
            return 1
            ;;
    esac

    # 現在のユーザーをdockerグループに追加
    sudo usermod -aG docker $USER

    # Dockerサービス起動
    sudo systemctl start docker
    sudo systemctl enable docker

    log_success "Dockerのインストールが完了しました"
    log_warning "Dockerグループの変更を有効にするには、ログアウト後に再ログインしてください"
}

# プロジェクト固有の設定
setup_project() {
    log_info "=== プロジェクト固有の設定 ==="

    # プロジェクトルートディレクトリの確認
    if [ ! -f "PROJECT_PLAN.md" ]; then
        log_error "プロジェクトルートディレクトリで実行してください"
        exit 1
    fi

    # .envファイルの作成
    if [ ! -f ".env" ]; then
        log_info ".envファイルを作成しています..."
        cat > .env << 'EOF'
# データベース設定
DB_USER=hidearea_user
DB_PASSWORD=hidearea_pass
DB_NAME=hidearea_dev

# JWT設定
JWT_SECRET=your-super-secret-key-change-this-in-production-min-32-chars-long

# フロントエンド設定
VITE_API_BASE_URL=http://localhost:8080/api/v1
EOF
        log_success ".envファイルを作成しました"
        log_warning "本番環境では .env ファイルの JWT_SECRET を必ず変更してください"
    else
        log_success ".envファイルは既に存在します"
    fi

    # backend/.envの作成（シンボリックリンク）
    if [ -d "backend" ] && [ ! -f "backend/.env" ]; then
        log_info "backend/.envのシンボリックリンクを作成しています..."
        ln -s ../.env backend/.env
        log_success "backend/.envを作成しました"
    fi

    # frontend/.envの作成
    if [ -d "frontend" ] && [ ! -f "frontend/.env" ]; then
        log_info "frontend/.envを作成しています..."
        cat > frontend/.env << 'EOF'
VITE_API_BASE_URL=http://localhost:8080/api/v1
EOF
        log_success "frontend/.envを作成しました"
    fi

    # Gradleラッパーの実行権限付与
    if [ -d "backend" ] && [ -f "backend/gradlew" ]; then
        chmod +x backend/gradlew
        log_success "Gradleラッパーに実行権限を付与しました"
    fi

    # スクリプトディレクトリの実行権限付与
    if [ -d "scripts" ]; then
        chmod +x scripts/*.sh 2>/dev/null || true
        log_success "スクリプトに実行権限を付与しました"
    fi
}

# バックエンド依存関係のダウンロード
setup_backend_dependencies() {
    log_info "=== バックエンド依存関係のセットアップ ==="

    if [ -d "backend" ]; then
        cd backend
        log_info "Gradle依存関係をダウンロードしています（初回は時間がかかります）..."
        ./gradlew build -x test --no-daemon
        cd ..
        log_success "バックエンド依存関係のダウンロードが完了しました"
    else
        log_warning "backendディレクトリが見つかりません"
    fi
}

# フロントエンド依存関係のインストール
setup_frontend_dependencies() {
    log_info "=== フロントエンド依存関係のセットアップ ==="

    if [ -d "frontend" ]; then
        cd frontend
        log_info "npm依存関係をインストールしています..."
        npm install
        cd ..
        log_success "フロントエンド依存関係のインストールが完了しました"
    else
        log_warning "frontendディレクトリが見つかりません"
    fi
}

# 推奨ツールのインストール提案
suggest_recommended_tools() {
    log_info "=== 推奨ツールのインストール提案 ==="

    echo ""
    log_info "以下のツールのインストールも推奨されます:"
    echo ""
    echo "  1. IntelliJ IDEA Ultimate (バックエンド開発)"
    echo "     https://www.jetbrains.com/idea/download/"
    echo ""
    echo "  2. Visual Studio Code (フロントエンド開発)"
    echo "     https://code.visualstudio.com/"
    echo ""
    echo "  3. DBeaver (PostgreSQLクライアント)"
    echo "     https://dbeaver.io/"
    echo ""
    echo "  4. Postman または Bruno (APIテストツール)"
    echo "     https://www.postman.com/"
    echo "     https://www.usebruno.com/"
    echo ""
}

# セットアップ完了メッセージ
print_completion_message() {
    echo ""
    echo "=========================================="
    log_success "開発環境のセットアップが完了しました！"
    echo "=========================================="
    echo ""
    log_info "次のステップ:"
    echo ""
    echo "  1. シェルを再起動するか、以下を実行してください:"
    echo "     source ~/.bashrc  # または source ~/.zshrc"
    echo ""
    echo "  2. Dockerグループの変更を有効にするため、ログアウトして再ログインしてください"
    echo ""
    echo "  3. 開発サーバーを起動:"
    echo "     ./scripts/start-dev.sh"
    echo ""
    echo "  4. アクセス先:"
    echo "     - フロントエンド: http://localhost:5173"
    echo "     - バックエンドAPI: http://localhost:8080/api/v1"
    echo "     - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "     - H2 Console: http://localhost:8080/h2-console"
    echo ""
    log_info "詳細は docs/development/ENVIRONMENT_SETUP.md を参照してください"
    echo ""
}

# メイン処理
main() {
    echo "=========================================="
    echo "  HideArea 開発環境セットアップ"
    echo "=========================================="
    echo ""

    detect_os

    log_info "以下をインストール・設定します:"
    echo "  - Git"
    echo "  - Java 25 (SDKMAN経由)"
    echo "  - Node.js 20.x LTS (nvm経由)"
    echo "  - Docker & Docker Compose"
    echo "  - プロジェクト依存関係"
    echo ""

    read -p "続行しますか? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warning "セットアップを中止しました"
        exit 0
    fi

    echo ""

    # 各コンポーネントのインストール
    install_git
    echo ""

    install_java
    echo ""

    install_nodejs
    echo ""

    install_docker
    echo ""

    setup_project
    echo ""

    # 依存関係のセットアップ（オプション）
    read -p "プロジェクトの依存関係もダウンロードしますか? (Y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
        echo ""
        setup_backend_dependencies
        echo ""
        setup_frontend_dependencies
    fi

    echo ""
    suggest_recommended_tools

    print_completion_message
}

# スクリプト実行
main "$@"
