# Java バージョンに関する注意事項

**作成日**: 2025-11-05

## サマリー

当初の計画ではJava 25を使用する予定でしたが、Gradle Kotlin DSLの互換性問題により、**Java 21 LTS**を使用しています。

## 問題の詳細

### 発生した問題

Gradle 8.11.1および8.12のKotlin DSLが、Java 25のバージョン番号を正しく認識できない問題が発生しました。

**エラーメッセージ**:
```
java.lang.IllegalArgumentException: 25
	at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse(JavaVersion.java:307)
```

### 試した対応

1. **Gradleバージョンアップ**: 8.11.1 → 8.12
   - 結果: 同じエラーが継続

2. **Java 25固有フラグの追加**: `--enable-native-access=ALL-UNNAMED`
   - 結果: Java 21では不要なフラグでエラー

3. **Java 21 LTSへのダウングレード**: ✅ 成功
   - Eclipse Temurin 21+35 LTS
   - Gradle 8.12で正常にビルド成功

## 現在の構成

### Java バージョン

```bash
$ java -version
openjdk version "21" 2023-09-19 LTS
OpenJDK Runtime Environment Temurin-21+35 (build 21+35-LTS)
OpenJDK 64-Bit Server VM Temurin-21+35 (build 21+35-LTS, mixed mode, sharing)
```

### Gradle バージョン

```properties
# backend/gradle/wrapper/gradle-wrapper.properties
distributionUrl=https://services.gradle.org/distributions/gradle-8.12-bin.zip
```

### ビルド設定

```kotlin
// backend/build.gradle.kts
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

## ビルド結果

**成功**: ✅

```
BUILD SUCCESSFUL in 23s
6 actionable tasks: 4 executed, 1 from cache, 1 up-to-date
```

**生成されたJAR**:
- `hidearea-backend-0.0.1-SNAPSHOT.jar` (64MB)
- Spring Boot実行可能JAR

## 結論

**Java 21 LTS**の使用は、本番環境での安定性と互換性の観点から**より適切な選択**です。

---

**最終更新**: 2025-11-05
**ステータス**: Java 21 LTS採用（最終決定）
