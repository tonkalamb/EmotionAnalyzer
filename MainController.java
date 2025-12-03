package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Message;
import service.DataManager;
import service.GeminiService;

import java.util.*;

public class MainController {
    
    // 화면 전환용
    @FXML private ScrollPane analysisPane;
    @FXML private ScrollPane historyPane;
    @FXML private ScrollPane statsPane;
    @FXML private VBox historyBox;
    @FXML private VBox statsBox;
    
    // 햄버거 메뉴
    @FXML private Button menuButton;
    @FXML private VBox sideMenu;
    
    // 메인 UI
    @FXML private TextArea inputTextArea;
    @FXML private Button analyzeButton;
    @FXML private VBox resultBox;
    @FXML private Label emotionLabel;
    @FXML private Label intensityLabel;
    @FXML private TextArea responseTextArea;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ComboBox<String> contactComboBox;
    @FXML private Button addContactButton;
    @FXML private Label contactCountLabel;
    
    private GeminiService geminiService;
    private DataManager dataManager;
    private boolean isMenuOpen = false;
    
    @FXML
    public void initialize() {
        geminiService = new GeminiService();
        dataManager = new DataManager();
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        updateContactList();
        
        if (!GeminiService.isApiKeySet()) {
            Platform.runLater(() -> {
                showAlert("⚠️ API 키 설정 필요", 
                    "Gemini API 키가 설정되지 않았습니다.\n\n" +
                    "GeminiService.java 파일을 열어서\n" +
                    "API_KEY 변수에 발급받은 키를 입력해주세요.",
                    Alert.AlertType.WARNING);
            });
        }
        
        System.out.println("✅ UI 컨트롤러 초기화 완료");
    }
    
    // ========== 햄버거 메뉴 ==========
    
    @FXML
    private void handleMenuToggle() {
        isMenuOpen = !isMenuOpen;
        if (sideMenu != null) {
            sideMenu.setVisible(isMenuOpen);
            sideMenu.setManaged(isMenuOpen);
        }
        System.out.println(isMenuOpen ? "📂 메뉴 열림" : "📂 메뉴 닫힘");
    }
    
    @FXML
    private void handleShowAnalysis() {
        closeMenu();
        showPane("analysis");
        System.out.println("📱 메시지 분석 화면");
    }
    
    @FXML
    private void handleShowHistory() {
        closeMenu();
        showPane("history");
        loadHistory();
        System.out.println("📜 분석 기록 화면");
    }
    
    @FXML
    private void handleShowStats() {
        closeMenu();
        showPane("stats");
        loadStats();
        System.out.println("📈 통계 화면");
    }
    
    private void closeMenu() {
        isMenuOpen = false;
        if (sideMenu != null) {
            sideMenu.setVisible(false);
            sideMenu.setManaged(false);
        }
    }
    
    private void showPane(String paneName) {
        if (analysisPane != null) analysisPane.setVisible("analysis".equals(paneName));
        if (historyPane != null) historyPane.setVisible("history".equals(paneName));
        if (statsPane != null) statsPane.setVisible("stats".equals(paneName));
    }
    
    // ========== 기록 & 통계 ==========
    
    private void loadHistory() {
        if (historyBox == null) return;
        historyBox.getChildren().clear();
        
        List<Message> messages = dataManager.getRecentMessages(20);
        
        if (messages.isEmpty()) {
            // 빈 상태 카드
            VBox emptyCard = new VBox(15);
            emptyCard.setPadding(new Insets(40));
            emptyCard.setAlignment(javafx.geometry.Pos.CENTER);
            emptyCard.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
            );
            
            Label iconLabel = new Label("📭");
            iconLabel.setStyle("-fx-font-size: 60px;");
            
            Label titleLabel = new Label("아직 분석 기록이 없습니다");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-font-family: 'Apple SD Gothic Neo';");
            
            Label descLabel = new Label("메시지 분석 화면에서 감정 분석을 시작해보세요!");
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-family: 'Apple SD Gothic Neo';");
            
            emptyCard.getChildren().addAll(iconLabel, titleLabel, descLabel);
            historyBox.getChildren().add(emptyCard);
        } else {
            for (Message msg : messages) {
                historyBox.getChildren().add(createMessageCard(msg));
            }
        }
    }
    
    private VBox createMessageCard(Message message) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + message.getEmotion().getColorCode() + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        
        // 상단: 이름 + 감정
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label("👤 " + message.getContactName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-font-family: 'Apple SD Gothic Neo';");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label emotionBadge = new Label(message.getEmotion().getEmoji() + " " + message.getEmotion().getKorean());
        emotionBadge.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 5 12 5 12; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-font-family: 'Apple SD Gothic Neo';",
            message.getEmotion().getColorCode()
        ));
        
        header.getChildren().addAll(nameLabel, spacer, emotionBadge);
        
        // 메시지 내용
        Label contentLabel = new Label("💬 " + message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555; -fx-font-family: 'Apple SD Gothic Neo';");
        
        // 감정 강도
        Label intensityLabel = new Label("📊 감정 강도: " + message.getIntensityPercent() + "%");
        intensityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-family: 'Apple SD Gothic Neo';");
        
        card.getChildren().addAll(header, contentLabel, intensityLabel);
        return card;
    }
    
    private void loadStats() {
        if (statsBox == null) return;
        statsBox.getChildren().clear();
        
        int totalCount = dataManager.getTotalMessageCount();
        Map<String, Integer> emotionCounts = dataManager.getEmotionCounts();
        
        if (totalCount == 0) {
            // 빈 상태 카드
            VBox emptyCard = new VBox(15);
            emptyCard.setPadding(new Insets(40));
            emptyCard.setAlignment(javafx.geometry.Pos.CENTER);
            emptyCard.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
            );
            
            Label iconLabel = new Label("📊");
            iconLabel.setStyle("-fx-font-size: 60px;");
            
            Label titleLabel = new Label("아직 통계 데이터가 없습니다");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-font-family: 'Apple SD Gothic Neo';");
            
            Label descLabel = new Label("메시지를 분석하면 통계가 쌓입니다!");
            descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-family: 'Apple SD Gothic Neo';");
            
            emptyCard.getChildren().addAll(iconLabel, titleLabel, descLabel);
            statsBox.getChildren().add(emptyCard);
        } else {
            // 총 메시지 수 카드
            VBox totalCard = new VBox(10);
            totalCard.setPadding(new Insets(20));
            totalCard.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
            );
            
            Label totalLabel = new Label("📱 총 분석한 메시지");
            totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-family: 'Apple SD Gothic Neo';");
            
            Label totalCountLabel = new Label(totalCount + "개");
            totalCountLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-font-family: 'Apple SD Gothic Neo';");
            
            totalCard.getChildren().addAll(totalLabel, totalCountLabel);
            statsBox.getChildren().add(totalCard);
            
            // 감정별 통계 카드
            if (!emotionCounts.isEmpty()) {
                VBox emotionCard = new VBox(15);
                emotionCard.setPadding(new Insets(20));
                emotionCard.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
                );
                
                Label emotionTitle = new Label("😊 감정별 분포");
                emotionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-font-family: 'Apple SD Gothic Neo';");
                emotionCard.getChildren().add(emotionTitle);
                
                // 감정별 카운트를 내림차순으로 정렬
                emotionCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> {
                        HBox emotionRow = new HBox(10);
                        emotionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        
                        Label emotionLabel = new Label(entry.getKey());
                        emotionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-font-family: 'Apple SD Gothic Neo';");
                        emotionLabel.setPrefWidth(150);
                        
                        // 프로그레스 바
                        ProgressBar bar = new ProgressBar((double) entry.getValue() / totalCount);
                        bar.setPrefWidth(200);
                        bar.setStyle("-fx-accent: #667eea;");
                        
                        Label countLabel = new Label(entry.getValue() + "개");
                        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888; -fx-font-family: 'Apple SD Gothic Neo';");
                        
                        emotionRow.getChildren().addAll(emotionLabel, bar, countLabel);
                        emotionCard.getChildren().add(emotionRow);
                    });
                
                statsBox.getChildren().add(emotionCard);
            }
        }
    }
    
    // ========== 메시지 분석 ==========
    
    private void updateContactList() {
        if (contactComboBox == null) return;
        
        Set<String> contacts = dataManager.getAllContactNames();
        List<String> sortedContacts = new ArrayList<>(contacts);
        sortedContacts.remove("알 수 없음");
        Collections.sort(sortedContacts);
        
        contactComboBox.setItems(FXCollections.observableArrayList(sortedContacts));
        
        if (!sortedContacts.isEmpty() && contactComboBox.getSelectionModel().isEmpty()) {
            List<Message> recent = dataManager.getRecentMessages(1);
            if (!recent.isEmpty()) {
                contactComboBox.setValue(recent.get(0).getContactName());
            }
        }
        
        if (contactCountLabel != null) {
            contactCountLabel.setText(String.format("총 %d명", sortedContacts.size()));
        }
    }
    
    @FXML
    private void handleAddContact() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("새 상대 추가");
        dialog.setHeaderText("👤 새로운 대화 상대를 추가하세요");
        dialog.setContentText("이름 또는 별명:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty() && !trimmedName.equals("알 수 없음")) {
                if (!contactComboBox.getItems().contains(trimmedName)) {
                    contactComboBox.getItems().add(trimmedName);
                    Collections.sort(contactComboBox.getItems());
                }
                contactComboBox.setValue(trimmedName);
                showAlert("추가 완료", 
                    "'" + trimmedName + "'님이 목록에 추가되었습니다.", 
                    Alert.AlertType.INFORMATION);
            }
        });
    }
    
    @FXML
    private void handleAnalyze() {
        String text = inputTextArea.getText().trim();
        
        if (text.isEmpty()) {
            showAlert("입력 오류", "분석할 문장을 입력해주세요.", Alert.AlertType.WARNING);
            return;
        }
        
        if (text.length() > 2000) {
            showAlert("입력 오류", 
                "텍스트가 너무 깁니다. (최대 2000자)\n현재: " + text.length() + "자",
                Alert.AlertType.WARNING);
            return;
        }
        
        String contactName = contactComboBox.getValue();
        if (contactName == null || contactName.trim().isEmpty()) {
            showAlert("상대방 선택", 
                "대화 상대를 선택하거나 입력해주세요.", 
                Alert.AlertType.WARNING);
            contactComboBox.requestFocus();
            return;
        }
        contactName = contactName.trim();
        
        if (!GeminiService.isApiKeySet()) {
            showAlert("API 키 오류", 
                "Gemini API 키가 설정되지 않았습니다.",
                Alert.AlertType.ERROR);
            return;
        }
        
        setUIEnabled(false);
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        
        final String finalContactName = contactName;
        
        new Thread(() -> {
            try {
                System.out.println("🔍 감정 분석 시작... (상대: " + finalContactName + ")");
                Message message = geminiService.analyzeEmotion(text);
                message.setContactName(finalContactName);
                
                Platform.runLater(() -> {
                    displayResult(message);
                    dataManager.saveMessage(message);
                    updateContactList();
                    setUIEnabled(true);
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("분석 오류", 
                        "감정 분석 중 오류가 발생했습니다:\n\n" + e.getMessage(),
                        Alert.AlertType.ERROR);
                    setUIEnabled(true);
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }
        }).start();
    }
    
    private void displayResult(Message message) {
        if (message == null) return;
        
        if (emotionLabel != null) {
            emotionLabel.setText(message.getEmotion().getEmoji() + " " + message.getEmotion().getKorean());
            emotionLabel.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold;",
                message.getEmotion().getColorCode()));
        }
        
        if (intensityLabel != null) {
            intensityLabel.setText(String.format(
                "감정 강도: %d%% (%s)", 
                message.getIntensityPercent(),
                message.getIntensityLevel()));
        }
        
        if (responseTextArea != null) {
            responseTextArea.setText(message.getRecommendedResponse());
        }
        
        if (resultBox != null) {
            resultBox.setVisible(true);
        }
    }
    
    @FXML
    private void handleImportKakaoCSV() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("카카오톡 CSV 파일 선택");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV 파일", "*.csv")
        );
        
        java.io.File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        
        if (file != null) {
            new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(true);
                        setUIEnabled(false);
                    });
                    
                    // KakaoParser 사용
                    service.KakaoParser.ParseResult result = service.KakaoParser.parseCSV(file);
                    
                    // 상대방 메시지만 필터링 (내가 받은 메시지)
                    List<service.KakaoParser.KakaoMessage> receivedMessages = 
                        service.KakaoParser.filterReceivedMessages(result);
                    
                    Platform.runLater(() -> {
                        if (receivedMessages.isEmpty()) {
                            showAlert("CSV 파일 없음", 
                                "상대방의 메시지를 찾을 수 없습니다.",
                                Alert.AlertType.WARNING);
                            if (loadingIndicator != null) loadingIndicator.setVisible(false);
                            setUIEnabled(true);
                            return;
                        }
                        
                        // 결과 다이얼로그
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("CSV 불러오기 성공");
                        alert.setHeaderText(String.format(
                            "총 %d개의 메시지 중 %d개의 받은 메시지를 찾았습니다!",
                            result.getTotalMessageCount(),
                            receivedMessages.size()));
                        
                        StringBuilder content = new StringBuilder();
                        content.append("👤 나: ").append(result.getMainUser()).append("\n");
                        content.append("👤 상대방: ").append(result.getOtherUser()).append("\n\n");
                        content.append("가장 최근 메시지부터 분석을 시작합니다.");
                        
                        alert.setContentText(content.toString());
                        alert.showAndWait();
                        
                        // 가장 최근 메시지를 입력창에 표시
                        service.KakaoParser.KakaoMessage latestMsg = receivedMessages.get(receivedMessages.size() - 1);
                        contactComboBox.setValue(result.getOtherUser());
                        inputTextArea.setText(latestMsg.getMessage());
                        
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        setUIEnabled(true);
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("CSV 읽기 오류", 
                            "CSV 파일을 읽는 중 오류가 발생했습니다:\n\n" + e.getMessage(),
                            Alert.AlertType.ERROR);
                        e.printStackTrace();
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        setUIEnabled(true);
                    });
                }
            }).start();
        }
    }
    
    @FXML
private void handleImageOCR() {
    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
    fileChooser.setTitle("스크린샷 이미지 선택");
    fileChooser.getExtensionFilters().addAll(
        new javafx.stage.FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg"),
        new javafx.stage.FileChooser.ExtensionFilter("모든 파일", "*.*")
    );
    
    java.io.File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
    
    if (file != null) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    if (loadingIndicator != null) loadingIndicator.setVisible(true);
                    setUIEnabled(false);
                });
                
                // 🔥 이름 + 텍스트 추출
                GeminiService.OCRResult ocrResult = geminiService.extractTextAndNameFromImage(file);
                
                // 🔥 감정 분석
                Message result = geminiService.analyzeEmotion(ocrResult.getExtractedText(), null);
                result.setContactName(ocrResult.getContactName());
                
                Platform.runLater(() -> {
                    // 상대방 이름 자동 설정
                    String contactName = ocrResult.getContactName();
                    if (!contactName.equals("알 수 없음")) {
                        // 목록에 없으면 추가
                        if (!contactComboBox.getItems().contains(contactName)) {
                            contactComboBox.getItems().add(contactName);
                            Collections.sort(contactComboBox.getItems());
                        }
                        contactComboBox.setValue(contactName);
                    }
                    
                    // 추출된 텍스트를 입력창에 표시
                    inputTextArea.setText(ocrResult.getExtractedText());
                    
                    // 분석 결과 표시 및 저장
                    displayResult(result);
                    dataManager.saveMessage(result);
                    updateContactList();
                    
                    showAlert("이미지 분석 완료", 
                        "상대방: " + contactName + "\n\n" +
                        "이미지에서 텍스트를 추출하고 감정 분석을 완료했습니다!",
                        Alert.AlertType.INFORMATION);
                    
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    setUIEnabled(true);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("이미지 분석 오류", 
                        "이미지 분석 중 오류가 발생했습니다:\n\n" + e.getMessage(),
                        Alert.AlertType.ERROR);
                    e.printStackTrace();
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    setUIEnabled(true);
                });
            }
        }).start();
    }
}
    
    @FXML
    private void handleClearData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("데이터 삭제 확인");
        alert.setHeaderText("모든 데이터를 삭제하시겠습니까?");
        alert.setContentText("저장된 모든 메시지와 통계가 영구적으로 삭제됩니다.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataManager.clearAllData();
                updateContactList();
                if (resultBox != null) {
                    resultBox.setVisible(false);
                }
                if (inputTextArea != null) {
                    inputTextArea.clear();
                }
                showAlert("삭제 완료", "모든 데이터가 삭제되었습니다.", Alert.AlertType.INFORMATION);
            }
        });
    }
    
    private void setUIEnabled(boolean enabled) {
        if (inputTextArea != null) inputTextArea.setDisable(!enabled);
        if (analyzeButton != null) analyzeButton.setDisable(!enabled);
        if (contactComboBox != null) contactComboBox.setDisable(!enabled);
        if (addContactButton != null) addContactButton.setDisable(!enabled);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}