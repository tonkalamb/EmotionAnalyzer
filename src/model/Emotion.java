package model;

public enum Emotion {
    JOY("기쁨", "#FFD700", "😊"),
    SADNESS("슬픔", "#4169E1", "😢"),
    ANGER("분노", "#FF4444", "😠"),
    FEAR("공포", "#800080", "😨"),
    DISGUST("혐오", "#32CD32", "🤢"),
    SURPRISE("놀람", "#FF69B4", "😲"),
    NEUTRAL("중립", "#808080", "😐");
    
    private final String korean;
    private final String colorCode;
    private final String emoji;
    
    Emotion(String korean, String colorCode, String emoji) {
        this.korean = korean;
        this.colorCode = colorCode;
        this.emoji = emoji;
    }
    
    public String getKorean() {
        return korean;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public static Emotion fromKorean(String korean) {
        if (korean == null) return NEUTRAL;
        korean = korean.trim();
        for (Emotion emotion : values()) {
            if (emotion.korean.equals(korean) || 
                emotion.korean.contains(korean) ||
                korean.contains(emotion.korean)) {
                return emotion;
            }
        }
        return NEUTRAL;
    }
    
    public String getDescription() {
        switch (this) {
            case JOY: return "긍정적이고 행복한 감정";
            case SADNESS: return "우울하고 슬픈 감정";
            case ANGER: return "화나고 분노하는 감정";
            case FEAR: return "두렵고 불안한 감정";
            case DISGUST: return "혐오스럽고 거부감이 드는 감정";
            case SURPRISE: return "놀랍고 예상치 못한 감정";
            case NEUTRAL:
            default: return "중립적이고 평온한 감정";
        }
    }
    
    @Override
    public String toString() {
        return emoji + " " + korean;
    }
}