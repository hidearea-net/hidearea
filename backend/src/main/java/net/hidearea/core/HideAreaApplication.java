package net.hidearea.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * HideArea アプリケーションのエントリーポイント
 * マイクロカーネルアーキテクチャ マルチデバイス対応Webサービスプラットフォーム
 */
@SpringBootApplication
@EnableJpaAuditing
public class HideAreaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HideAreaApplication.class, args);
    }

}
