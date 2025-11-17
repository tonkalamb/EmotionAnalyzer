package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // PIN 확인 먼저
        showPinLockScreen(() -> {
            // PIN 인증 성공 후 메인 화면 표시
            showMainScreen(primaryStage);
        });
    }
    
    private void showPinLockScreen(Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/pinlock.fxml"));
            Parent root = loader.load();
            
            controller.PinLockController controller = loader.getController();
            controller.setOnSuccess(onSuccess);
            
            Stage pinStage = new Stage();
            pinStage.setTitle("🔐 PIN 입력");
            pinStage.setScene(new Scene(root, 450, 500));
            pinStage.setResizable(false);
            pinStage.initModality(Modality.APPLICATION_MODAL);
            pinStage.setOnCloseRequest(e -> {
                System.out.println("PIN 인증 없이 종료할 수 없습니다.");
                e.consume();
            });
            
            pinStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ PIN 화면 로드 실패:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void showMainScreen(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/main.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1000, 700);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/resources/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("스타일시트 로드 실패 (선택사항)");
            }
            
            primaryStage.setTitle("💬 감정 분석 & 답변 추천 시스템");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
            System.out.println("✅ 프로그램이 성공적으로 실행되었습니다!");
            
        } catch (Exception e) {
            System.err.println("❌ 프로그램 실행 중 오류 발생:");
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        System.out.println("프로그램을 종료합니다.");
    }

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  감정 분석 & 답변 추천 시스템 시작");
        System.out.println("===========================================");
        launch(args);
    }
}
