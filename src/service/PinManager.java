package service;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;

public class PinManager {
    private static final String PIN_FILE = "data/pin.dat";
    private static final String SALT = "EmotionAnalyzer2024";
    
    // PIN이 설정되어 있는지 확인
    public static boolean isPinSet() {
        return new File(PIN_FILE).exists();
    }
    
    // PIN 설정 (최초 1회)
    public static boolean setPin(String pin) {
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            return false;
        }
        
        try {
            String hashedPin = hashPin(pin);
            Files.createDirectories(Paths.get("data"));
            Files.write(Paths.get(PIN_FILE), hashedPin.getBytes());
            System.out.println("✅ PIN이 설정되었습니다.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ PIN 설정 실패: " + e.getMessage());
            return false;
        }
    }
    
    // PIN 확인
    public static boolean verifyPin(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        
        try {
            String savedHash = new String(Files.readAllBytes(Paths.get(PIN_FILE)));
            String inputHash = hashPin(pin);
            return savedHash.equals(inputHash);
        } catch (Exception e) {
            System.err.println("❌ PIN 확인 실패: " + e.getMessage());
            return false;
        }
    }
    
    // PIN 초기화 (삭제)
    public static void resetPin() {
        try {
            Files.deleteIfExists(Paths.get(PIN_FILE));
            System.out.println("✅ PIN이 초기화되었습니다.");
        } catch (Exception e) {
            System.err.println("❌ PIN 초기화 실패: " + e.getMessage());
        }
    }
    
    // PIN 해싱 (SHA-256)
    private static String hashPin(String pin) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest((pin + SALT).getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
