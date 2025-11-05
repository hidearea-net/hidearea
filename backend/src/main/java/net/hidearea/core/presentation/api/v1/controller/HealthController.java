package net.hidearea.core.presentation.api.v1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ヘルスチェック用コントローラー
 * アプリケーションの動作確認用の簡単なエンドポイントを提供します。
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    /**
     * ヘルスチェックエンドポイント
     *
     * @return ステータス情報
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "HideArea Backend");
        response.put("version", "0.0.1-SNAPSHOT");

        return ResponseEntity.ok(response);
    }
}
