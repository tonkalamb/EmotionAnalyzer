package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.Emotion;
import model.Message;
import service.DataManager;
import service.GeminiService;

import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    
    @FXML private TextArea inputTextArea;
    @FXML private Button analyzeButton;
    @FXML private VBox resultBox;
    @FXML private Label emotionLabel;
    @FXML private Label intensityLabel;
    @FXML private TextArea responseTextArea;
    @FXML private VBox historyBox;
    @FXML private VBox statsBox;
    @FXML private TabPane tabPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ComboBox<String> contactComboBox; // 🆕
    @FXML private Button addContactButton; // 🆕
    @FXML private Label contactCountLabel; // 🆕
    
    private GeminiService geminiService;
    private DataManager dataManager;
    
    @FXML
    public void initialize() {
        geminiService = new GeminiService();
        dataManager = new DataManager();
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        // 🆕 상대방 목록 초기화
        updateContactList();
        
        if (!GeminiService.isApiKeySet()) {
            Platform.runLater(() -> {
                showAlert("⚠️ API 키 설정 필요", 
                    "Gemini API 키가 설정되지 않았습니다.\n\n" +
                    "GeminiService.java 파일을 열어서\n" +
                    "API_KEY 변수에 발급받은 키를 입력해주세요.\n\n" +
                    "키 발급: https://makersuite.google.com/app/apikey",
                    Alert.AlertType.WARNING);
            });
        }
        
        loadHistory();
        loadStats();
        
        System.out.println("✅ UI 컨트롤러 초기화 완료");
    }
    
    // 🆕 상대방 목록 업데이트
    private void updateContactList() {
        if (contactComboBox == null) return;
        
        Set<String> contacts = dataManager.getAllContactNames();
        List<String> sortedContacts = new ArrayList<>(contacts);
        sortedContacts.remove("알 수 없음"); // 기본값 제외
        Collections.sort(sortedContacts);
        
        contactComboBox.setItems(FXCollections.observableArrayList(sortedContacts));
        
        // 가장 최근 사용한 상대방 자동 선택
        if (!sortedContacts.isEmpty() && contactComboBox.getSelectionModel().isEmpty()) {
            List<Message> recent = dataManager.getRecentMessages(1);
            if (!recent.isEmpty()) {
                contactComboBox.setValue(recent.get(0).getContactName());
            }
        }
        
        // 🆕 상대방 수 표시
        if (contactCountLabel != null) {
            contactCountLabel.setText(String.format("총 %d명", sortedContacts.size()));
        }
    }
    
    // 🆕 새 상대 추가 버튼
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
                // ComboBox에 추가
                if (!contactComboBox.getItems().contains(trimmedName)) {
                    contactComboBox.getItems().add(trimmedName);
                    Collections.sort(contactComboBox.getItems());
                }
                // 자동 선택
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
        
        // 🆕 상대방 선택 확인
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
                "Gemini API 키가 설정되지 않았습니다.\n" +
                "GeminiService.java 파일에서 API_KEY를 설정해주세요.",
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
                    updateContactList(); // 🆕 목록 업데이트
                    loadHistory();
                    loadStats();
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
        
        Emotion emotion = message.getEmotion();
        
        if (emotionLabel != null) {
            emotionLabel.setText(emotion.getEmoji() + " " + emotion.getKorean());
            emotionLabel.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold;",
                emotion.getColorCode()));
        }
        
        if (intensityLabel != null) {
            intensityLabel.setText(String.format(
                "감정 강도: %d%% (%s)", 
                message.getIntensityPercent(),
                message.getIntensityLevel()));
            intensityLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        }
        
        if (responseTextArea != null) {
            responseTextArea.setText(message.getRecommendedResponse());
            responseTextArea.setStyle(String.format(
                "-fx-border-color: %s; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 10;",
                emotion.getColorCode()));
        }
        
        if (resultBox != null) {
            resultBox.setVisible(true);
        }
    }
    
    private void loadHistory() {
        if (historyBox == null) return;
        
        historyBox.getChildren().clear();
        
        List<Message> messages = dataManager.getRecentMessages(20);
        
        if (messages.isEmpty()) {
            Label emptyLabel = new Label("📭 아직 분석 기록이 없습니다.\n\n" +
                "메시지를 입력하고 '감정 분석하기' 버튼을 눌러보세요!");
            emptyLabel.setStyle(
                "-fx-text-fill: gray; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 20; " +
                "-fx-text-alignment: center;");
            emptyLabel.setWrapText(true);
            historyBox.getChildren().add(emptyLabel);
            return;
        }
        
        for (Message msg : messages) {
            historyBox.getChildren().add(createMessageCard(msg));
        }
    }
    
    private VBox createMessageCard(Message message) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);",
            hexToRgba(message.getEmotion().getColorCode(), 0.08),
            message.getEmotion().getColorCode()));
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label contactLabel = new Label("👤 " + message.getContactName());
        contactLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-font-weight: bold;");
        
        Label timeLabel = new Label("🕐 " + message.getFormattedTimestamp());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        Label emotionTag = new Label(message.getEmotion().getEmoji() + " " + 
            message.getEmotion().getKorean() + " " + 
            message.getIntensityPercent() + "%");
        emotionTag.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 3 10 3 10; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold;",
            message.getEmotion().getColorCode()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(contactLabel, timeLabel, spacer, emotionTag);
        
        Label contentLabel = new Label("💬 " + message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #333; " +
            "-fx-padding: 5 0 5 0;");
        
        Separator separator = new Separator();
        
        Label responseLabel = new Label("💡 " + message.getRecommendedResponse());
        responseLabel.setWrapText(true);
        responseLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #555; " +
            "-fx-padding: 8; " +
            "-fx-background-color: rgba(255,255,255,0.5); " +
            "-fx-background-radius: 5;");
        
        card.getChildren().addAll(header, contentLabel, separator, responseLabel);
        
        return card;
    }
    
    private void loadStats() {
        if (statsBox == null) return;
        
        statsBox.getChildren().clear();
        
        int totalCount = dataManager.getTotalMessageCount();
        
        if (totalCount == 0) {
            Label emptyLabel = new Label("📊 아직 통계 데이터가 없습니다.");
            emptyLabel.setStyle(
                "-fx-text-fill: gray; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 20;");
            statsBox.getChildren().add(emptyLabel);
            return;
        }
        
        VBox overallStats = createOverallStatsBox();
        statsBox.getChildren().add(overallStats);
        
        List<Message> allMessages = dataManager.getAllMessages();
        Map<String, List<Message>> messagesByContact = allMessages.stream()
            .collect(Collectors.groupingBy(Message::getContactName));
        
        List<String> sortedContacts = new ArrayList<>(messagesByContact.keySet());
        sortedContacts.remove("알 수 없음");
        Collections.sort(sortedContacts);
        if (messagesByContact.containsKey("알 수 없음")) {
            sortedContacts.add("알 수 없음");
        }
        
        for (String contactName : sortedContacts) {
            List<Message> contactMessages = messagesByContact.get(contactName);
            VBox contactStatsBox = createContactStatsBox(contactName, contactMessages);
            statsBox.getChildren().add(contactStatsBox);
        }
    }
    
    private VBox createContactStatsBox(String contactName, List<Message> messages) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label titleLabel = new Label("👤 " + contactName + "님과의 대화");
        titleLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #667eea;");
        
        int count = messages.size();
        double avgIntensity = messages.stream()
            .mapToDouble(Message::getIntensity)
            .average()
            .orElse(0.0);
        
        Map<Emotion, Long> emotionCount = messages.stream()
            .collect(Collectors.groupingBy(Message::getEmotion, Collectors.counting()));
        
        Emotion mostFrequent = emotionCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Emotion.NEUTRAL);
        
        Label countLabel = new Label("📝 대화 횟수: " + count + "회");
        countLabel.setStyle("-fx-font-size: 13px;");
        
        Label avgLabel = new Label(String.format("📈 평균 감정 강도: %.0f%%", avgIntensity * 100));
        avgLabel.setStyle("-fx-font-size: 13px;");
        
        Label mostLabel = new Label("⭐ 가장 많은 감정: " + mostFrequent.getEmoji() + " " + mostFrequent.getKorean());
        mostLabel.setStyle("-fx-font-size: 13px;");
        
        HBox emotionBars = createMiniEmotionBars(emotionCount, count);
        
        box.getChildren().addAll(titleLabel, new Separator(), countLabel, avgLabel, mostLabel, emotionBars);
        
        return box;
    }
    
    private HBox createMiniEmotionBars(Map<Emotion, Long> emotionCount, int total) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        
        for (Emotion emotion : Emotion.values()) {
            long count = emotionCount.getOrDefault(emotion, 0L);
            if (count > 0) {
                double percentage = (count / (double) total) * 100;
                
                VBox bar = new VBox(3);
                bar.setAlignment(Pos.BOTTOM_CENTER);
                bar.setMinWidth(40);
                
                Label emojiLabel = new Label(emotion.getEmoji());
                emojiLabel.setStyle("-fx-font-size: 14px;");
                
                Region colorBar = new Region();
                colorBar.setPrefWidth(30);
                colorBar.setPrefHeight(percentage * 1.5);
                colorBar.setStyle(String.format(
                    "-fx-background-color: %s; " +
                    "-fx-background-radius: 5 5 0 0;",
                    emotion.getColorCode()));
                
                Label percentLabel = new Label(String.format("%.0f%%", percentage));
                percentLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                
                bar.getChildren().addAll(emojiLabel, colorBar, percentLabel);
                box.getChildren().add(bar);
            }
        }
        
        return box;
    }
    
    private VBox createOverallStatsBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);");
        
        Label titleLabel = new Label("📊 전체 감정 분석 통계");
        titleLabel.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: white;");
        
        int totalCount = dataManager.getTotalMessageCount();
        int todayCount = dataManager.getTodayMessageCount();
        double avgIntensity = dataManager.getAverageIntensity();
        Emotion mostFrequent = dataManager.getMostFrequentEmotion();
        
        Label totalLabel = new Label("📝 총 분석 횟수: " + totalCount + "회");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        
        Label todayLabel = new Label("📅 오늘의 분석: " + todayCount + "회");
        todayLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        
        Label avgLabel = new Label(String.format("📈 평균 감정 강도: %.0f%%", avgIntensity * 100));
        avgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        
        Label mostLabel = new Label("⭐ 가장 많은 감정: " + mostFrequent.getEmoji() + " " + mostFrequent.getKorean());
        mostLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        
        box.getChildren().addAll(titleLabel, totalLabel, todayLabel, avgLabel, mostLabel);
        
        return box;
    }
    
    @FXML
    private void handleClearData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("데이터 삭제 확인");
        alert.setHeaderText("모든 데이터를 삭제하시겠습니까?");
        alert.setContentText(
            "저장된 모든 메시지와 통계가 영구적으로 삭제됩니다.\n" +
            "이 작업은 되돌릴 수 없습니다!");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataManager.clearAllData();
                updateContactList(); // 🆕 목록 초기화
                loadHistory();
                loadStats();
                if (resultBox != null) {
                    resultBox.setVisible(false);
                }
                if (inputTextArea != null) {
                    inputTextArea.clear();
                }
                showAlert("삭제 완료", 
                    "모든 데이터가 삭제되었습니다.", 
                    Alert.AlertType.INFORMATION);
            }
        });
    }
    
    private void setUIEnabled(boolean enabled) {
        if (inputTextArea != null) {
            inputTextArea.setDisable(!enabled);
        }
        if (analyzeButton != null) {
            analyzeButton.setDisable(!enabled);
        }
        if (contactComboBox != null) {
            contactComboBox.setDisable(!enabled);
        }
        if (addContactButton != null) {
            addContactButton.setDisable(!enabled);
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.INFORMATION);
    }
    
    private String hexToRgba(String hex, double alpha) {
        try {
            Color color = Color.web(hex);
            return String.format("rgba(%d, %d, %d, %.2f)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                alpha);
        } catch (Exception e) {
            return "rgba(128, 128, 128, " + alpha + ")";
        }
    }
}
