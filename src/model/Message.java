package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String content;
    private Emotion emotion;
    private double intensity;
    private String recommendedResponse;
    private LocalDateTime timestamp;
    private String contactName; // 🆕 상대방 이름 추가
    
    public Message(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.emotion = Emotion.NEUTRAL;
        this.intensity = 0.5;
        this.recommendedResponse = "";
        this.contactName = "알 수 없음";
    }
    
    public Message(String content, Emotion emotion, double intensity, String recommendedResponse) {
        this.content = content;
        this.emotion = emotion;
        this.intensity = intensity;
        this.recommendedResponse = recommendedResponse;
        this.timestamp = LocalDateTime.now();
        this.contactName = "알 수 없음";
    }
    
    // 🆕 상대방 이름 포함 생성자
    public Message(String content, Emotion emotion, double intensity, String recommendedResponse, String contactName) {
        this.content = content;
        this.emotion = emotion;
        this.intensity = intensity;
        this.recommendedResponse = recommendedResponse;
        this.timestamp = LocalDateTime.now();
        this.contactName = contactName != null && !contactName.trim().isEmpty() ? contactName : "알 수 없음";
    }
    
    public String getContent() { return content; }
    public Emotion getEmotion() { return emotion; }
    public double getIntensity() { return intensity; }
    public String getRecommendedResponse() { return recommendedResponse; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getContactName() { return contactName; } // 🆕
    
    public void setContent(String content) { this.content = content; }
    public void setEmotion(Emotion emotion) { this.emotion = emotion; }
    public void setRecommendedResponse(String recommendedResponse) { 
        this.recommendedResponse = recommendedResponse; 
    }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setContactName(String contactName) { // 🆕
        this.contactName = contactName != null && !contactName.trim().isEmpty() ? contactName : "알 수 없음";
    }
    
    public void setIntensity(double intensity) {
        if (intensity < 0.0) this.intensity = 0.0;
        else if (intensity > 1.0) this.intensity = 1.0;
        else this.intensity = intensity;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp.format(formatter);
    }
    
    public int getIntensityPercent() {
        return (int) (intensity * 100);
    }
    
    public String getIntensityLevel() {
        if (intensity < 0.33) return "약함";
        else if (intensity < 0.67) return "보통";
        else return "강함";
    }
    
    public String getSummary() {
        return String.format("%s %s (%d%%)", 
            emotion.getEmoji(), emotion.getKorean(), getIntensityPercent());
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (강도: %.0f%%)", 
            getFormattedTimestamp(), emotion.getKorean(), 
            content.length() > 20 ? content.substring(0, 20) + "..." : content, 
            intensity * 100);
    }
}
