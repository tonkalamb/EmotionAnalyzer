package model;

public enum MBTI {
    // Analysts (분석가형)
    INTJ("INTJ", "전략가", "독립적이고 전략적. 감정 표현 절제적"),
    INTP("INTP", "논리술사", "논리적이고 분석적. 감정보다 이성 우선"),
    ENTJ("ENTJ", "통솔자", "리더십 강함. 직설적이고 효율적"),
    ENTP("ENTP", "변론가", "창의적이고 도전적. 감정보다 논리"),
    
    // Diplomats (외교관형)
    INFJ("INFJ", "옹호자", "이상주의적. 깊은 공감 능력"),
    INFP("INFP", "중재자", "감수성 예민. '괜찮아'가 진짜 힘든 신호"),
    ENFJ("ENFJ", "선도자", "타인 감정 민감. 배려심 많음"),
    ENFP("ENFP", "활동가", "열정적이고 긍정적. 감정 솔직"),
    
    // Sentinels (관리자형)
    ISTJ("ISTJ", "현실주의자", "책임감 강함. 감정 표현 절제"),
    ISFJ("ISFJ", "수호자", "헌신적이고 온화. 타인 배려"),
    ESTJ("ESTJ", "경영자", "실용적이고 직설적. '괜찮아'는 정말 괜찮음"),
    ESFJ("ESFJ", "집정관", "사교적이고 배려심 많음. 조화 중시"),
    
    // Explorers (탐험가형)
    ISTP("ISTP", "장인", "현실적이고 독립적. 감정 표현 최소화"),
    ISFP("ISFP", "모험가", "온화하고 유연. 감정 내면화"),
    ESTP("ESTP", "사업가", "활동적이고 실용적. 직설적"),
    ESFP("ESFP", "연예인", "사교적이고 낙관적. 감정 표현 풍부"),
    
    UNKNOWN("알 수 없음", "미설정", "MBTI를 설정해주세요");
    
    private final String code;
    private final String nickname;
    private final String characteristic;
    
    MBTI(String code, String nickname, String characteristic) {
        this.code = code;
        this.nickname = nickname;
        this.characteristic = characteristic;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getCharacteristic() {
        return characteristic;
    }
    
    public String getDisplayName() {
        return code + " (" + nickname + ")";
    }
    
    public boolean isEmotionallyExpressive() {
        return (code.contains("F") && code.startsWith("E"));
    }
    
    public boolean isEmotionallyReserved() {
        return code.contains("T") || code.startsWith("I");
    }
    
    public boolean tendsToMinimizeProblems() {
        return this == INFP || this == ISFP || this == INFJ;
    }
    
    public boolean tendsToBeHonest() {
        return this == ESTJ || this == ENTJ || this == ESTP || this == ISTJ;
    }
    
    public String getEmotionInterpretationGuideline() {
        switch (this) {
            case INFP:
            case INFJ:
                return "매우 감수성이 예민하며, '괜찮아'라고 말해도 실제로는 깊이 상처받았을 가능성이 높음. 간접적 표현 뒤에 숨겨진 진짜 감정을 파악해야 함.";
            
            case ENFP:
            case ENFJ:
                return "감정 표현이 솔직하고 풍부함. 말 그대로 받아들여도 됨. 기쁠 때 정말 기쁘고, 슬플 때 정말 슬픔.";
            
            case INTJ:
            case INTP:
                return "논리적이고 이성적. 감정 표현을 최소화하지만 내면에는 감정이 있음. '괜찮아'는 정말 괜찮거나, 혼자 해결하겠다는 의미.";
            
            case ESTJ:
            case ENTJ:
                return "직설적이고 솔직함. '괜찮아'는 정말 괜찮다는 의미. 문제가 있으면 바로 말함. 돌려 말하지 않음.";
            
            case ISFJ:
            case ESFJ:
                return "타인 배려가 강함. 본인 감정보다 상대방 배려를 우선시할 수 있음. '괜찮아'가 상대를 편하게 하려는 말일 수 있음.";
            
            case ISTP:
            case ESTP:
                return "현실적이고 실용적. 감정 표현 최소화. '괜찮아'는 대부분 정말 괜찮거나 관심 없다는 의미.";
            
            case ISFP:
            case ESFP:
                return "감정을 내면화하거나(ISFP) 표출함(ESFP). ISFP는 '괜찮아'가 힘든 신호일 수 있음. ESFP는 솔직함.";
            
            case UNKNOWN:
            default:
                return "MBTI 정보가 없어 일반적인 감정 해석을 적용함.";
        }
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    public static MBTI fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        str = str.trim().toUpperCase();
        for (MBTI mbti : values()) {
            if (mbti.code.equals(str) || mbti.code.equalsIgnoreCase(str)) {
                return mbti;
            }
        }
        return UNKNOWN;
    }
}
