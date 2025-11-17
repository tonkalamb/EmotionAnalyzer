package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.PinManager;

public class PinLockController {
    
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private PasswordField pinField1;
    @FXML private PasswordField pinField2;
    @FXML private PasswordField pinField3;
    @FXML private PasswordField pinField4;
    @FXML private Button submitButton;
    @FXML private Label hintLabel;
    
    private boolean isSettingPin = false;
    private String firstPin = null;
    private Runnable onSuccess;
    
    @FXML
    public void initialize() {
        // PIN 설정 여부에 따라 UI 변경
        isSettingPin = !PinManager.isPinSet();
        
        if (isSettingPin) {
            titleLabel.setText("🔐 PIN 설정");
            messageLabel.setText("4자리 PIN을 설정해주세요");
            submitButton.setText("설정");
            hintLabel.setText("앱을 보호하기 위한 PIN을 입력하세요");
        } else {
            titleLabel.setText("🔒 PIN 입력");
            messageLabel.setText("PIN을 입력하세요");
            submitButton.setText("확인");
            hintLabel.setText("설정된 PIN을 입력하세요");
        }
        
        // 각 필드에 자동 포커스 이동 설정
        setupAutoFocus();
        
        // 첫 번째 필드에 포커스
        pinField1.requestFocus();
    }
    
    private void setupAutoFocus() {
        pinField1.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() == 1) pinField2.requestFocus();
        });
        pinField2.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() == 1) pinField3.requestFocus();
        });
        pinField3.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() == 1) pinField4.requestFocus();
        });
        pinField4.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() == 1) handleSubmit();
        });
    }
    
    @FXML
    private void handleSubmit() {
        String pin = pinField1.getText() + pinField2.getText() + 
                     pinField3.getText() + pinField4.getText();
        
        if (pin.length() != 4) {
            showError("4자리 숫자를 모두 입력해주세요");
            return;
        }
        
        if (!pin.matches("\\d{4}")) {
            showError("숫자만 입력 가능합니다");
            clearFields();
            return;
        }
        
        if (isSettingPin) {
            handlePinSetting(pin);
        } else {
            handlePinVerification(pin);
        }
    }
    
    private void handlePinSetting(String pin) {
        if (firstPin == null) {
            // 첫 번째 입력
            firstPin = pin;
            messageLabel.setText("다시 한 번 입력해주세요");
            messageLabel.setStyle("-fx-text-fill: #666;");
            clearFields();
        } else {
            // 두 번째 입력 - 확인
            if (firstPin.equals(pin)) {
                if (PinManager.setPin(pin)) {
                    showSuccess("PIN이 설정되었습니다!");
                    closeWindow();
                } else {
                    showError("PIN 설정에 실패했습니다");
                    firstPin = null;
                    clearFields();
                }
            } else {
                showError("PIN이 일치하지 않습니다. 다시 시도하세요");
                firstPin = null;
                messageLabel.setText("4자리 PIN을 설정해주세요");
                clearFields();
            }
        }
    }
    
    private void handlePinVerification(String pin) {
        if (PinManager.verifyPin(pin)) {
            System.out.println("✅ PIN 인증 성공!");
            closeWindow();
        } else {
            showError("PIN이 틀렸습니다");
            clearFields();
        }
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #51cf66; -fx-font-weight: bold;");
    }
    
    private void clearFields() {
        pinField1.clear();
        pinField2.clear();
        pinField3.clear();
        pinField4.clear();
        pinField1.requestFocus();
    }
    
    private void closeWindow() {
        if (onSuccess != null) {
            onSuccess.run();
        }
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }
}
