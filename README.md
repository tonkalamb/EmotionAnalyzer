# EmotionAnalyzer
Analyzing Emotions w/ Java

# 💬 감정 분석 & 답변 추천 시스템

JavaFX와 Google Gemini AI를 활용한 메시지 감정 분석 앱

## ✨ 기능
- 📱 메시지 감정 분석 (7가지 감정)
- 💡 AI 답변 추천
- 👥 상대방별 통계
- 📊 감정 데이터 시각화
- 🔐 PIN 잠금

## 🛠️ 설치 방법

### 1. Java 21 설치
```bash
brew install openjdk@21
```

### 2. JavaFX 다운로드
- https://gluonhq.com/products/javafx/ 접속
- JavaFX 21.0.9 Mac (aarch64) SDK 다운로드
- 압축 해제 후 `lib/` 폴더에 내용물 복사

### 3. Gemini API 키 발급
- https://aistudio.google.com/apikey 접속
- API 키 생성
- `src/service/GeminiService.java` 18번째 줄에 입력:
```java
private static final String API_KEY = "여기에_발급받은_키";
```

### 4. 컴파일 및 실행
```bash
mkdir bin
javac -d bin -encoding UTF-8 -cp "lib/*" src/**/*.java
cp -r src/resources bin/
java --module-path lib --add-modules javafx.controls,javafx.fxml -cp "bin:lib/*" main.MainApp
```

## 📁 프로젝트 구조
```
EmotionAnalyzer/
├── src/
│   ├── main/           # 메인 애플리케이션
│   ├── controller/     # UI 컨트롤러
│   ├── model/          # 데이터 모델
│   ├── service/        # 비즈니스 로직
│   └── resources/      # FXML, CSS
└── lib/                # JavaFX 라이브러리 (직접 다운로드 필요)
```

## 🔐 보안
- API 키는 절대 공유하지 마세요
- PIN은 SHA-256으로 암호화되어 저장됩니다

## 📄 라이선스
MIT License

## 👤 제작자
[@tonkalamb](https://github.com/tonkalamb)
