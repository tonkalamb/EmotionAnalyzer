package service;

import model.Message;
import model.MBTI;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KakaoParser {
    
    // 카카오톡 CSV 파싱 결과
    public static class ParseResult {
        private List<KakaoMessage> messages;
        private Map<String, Integer> userMessageCount;
        private String mainUser; // 가장 많이 말한 사람 (나)
        private String otherUser; // 상대방
        
        public ParseResult() {
            this.messages = new ArrayList<>();
            this.userMessageCount = new HashMap<>();
        }
        
        public void addMessage(KakaoMessage msg) {
            messages.add(msg);
            userMessageCount.put(msg.getUser(), 
                userMessageCount.getOrDefault(msg.getUser(), 0) + 1);
        }
        
        public void calculateMainUsers() {
            if (userMessageCount.isEmpty()) return;
            
            // 메시지 수로 정렬
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(userMessageCount.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            if (sorted.size() >= 2) {
                mainUser = sorted.get(0).getKey();
                otherUser = sorted.get(1).getKey();
            } else if (sorted.size() == 1) {
                mainUser = sorted.get(0).getKey();
                otherUser = mainUser;
            }
        }
        
        public List<KakaoMessage> getMessages() { return messages; }
        public String getMainUser() { return mainUser; }
        public String getOtherUser() { return otherUser; }
        public Map<String, Integer> getUserMessageCount() { return userMessageCount; }
        public int getTotalMessageCount() { return messages.size(); }
    }
    
    // 카카오톡 메시지
    public static class KakaoMessage {
        private LocalDateTime dateTime;
        private String user;
        private String message;
        
        public KakaoMessage(LocalDateTime dateTime, String user, String message) {
            this.dateTime = dateTime;
            this.user = user;
            this.message = message;
        }
        
        public LocalDateTime getDateTime() { return dateTime; }
        public String getUser() { return user; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", 
                dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                user, message);
        }
    }
    
    /**
     * 카카오톡 CSV 파일 파싱
     * 형식: Date,User,Message
     * 예: 2025-04-04 17:48:56,"윤정우","메시지 내용"
     */
    public static ParseResult parseCSV(File file) throws Exception {
        ParseResult result = new ParseResult();
        
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 첫 줄 (헤더) 스킵
                if (isFirstLine) {
                    isFirstLine = false;
                    // BOM 제거
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    continue;
                }
                
                try {
                    KakaoMessage msg = parseLine(line);
                    if (msg != null) {
                        result.addMessage(msg);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ " + lineNumber + "번째 줄 파싱 실패: " + e.getMessage());
                    // 에러 나도 계속 진행
                }
            }
            
            result.calculateMainUsers();
            
            System.out.println("✅ CSV 파싱 완료:");
            System.out.println("  - 총 메시지: " + result.getTotalMessageCount() + "개");
            System.out.println("  - 사용자: " + result.getUserMessageCount().keySet());
            if (result.getMainUser() != null) {
                System.out.println("  - 주 사용자 (나): " + result.getMainUser());
                System.out.println("  - 상대방: " + result.getOtherUser());
            }
            
        }
        
        return result;
    }
    
    /**
     * CSV 한 줄 파싱
     * 예: 2025-04-04 17:48:56,"윤정우","메시지 내용"
     */
    private static KakaoMessage parseLine(String line) throws Exception {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        // CSV 파싱 (큰따옴표 안의 쉼표는 무시)
        List<String> fields = parseCSVLine(line);
        
        if (fields.size() < 3) {
            throw new Exception("필드 부족: " + fields.size());
        }
        
        // Date 파싱
        String dateStr = fields.get(0).trim();
        LocalDateTime dateTime = parseDateTime(dateStr);
        
        // User 파싱
        String user = fields.get(1).trim();
        
        // Message 파싱 (나머지 전부)
        StringBuilder message = new StringBuilder();
        for (int i = 2; i < fields.size(); i++) {
            if (i > 2) message.append(",");
            message.append(fields.get(i));
        }
        
        String messageText = message.toString().trim();
        
        // 빈 메시지 필터링
        if (messageText.isEmpty() || messageText.equals("삭제된 메시지입니다.")) {
            return null;
        }
        
        return new KakaoMessage(dateTime, user, messageText);
    }
    
    /**
     * CSV 라인 파싱 (큰따옴표 처리)
     */
    private static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        fields.add(current.toString());
        return fields;
    }
    
    /**
     * 날짜 파싱
     * 지원 형식:
     * - 2025-04-04 17:48:56
     * - 2025-04-04 오후 5:48:56
     */
    private static LocalDateTime parseDateTime(String dateStr) {
        try {
            // 기본 형식
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            // 오전/오후 형식 시도
            try {
                dateStr = dateStr.replace("오전", "AM").replace("오후", "PM");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd a h:mm:ss", Locale.ENGLISH);
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e2) {
                throw new RuntimeException("날짜 파싱 실패: " + dateStr);
            }
        }
    }
    
    /**
     * 상대방 메시지만 필터링 (내가 받은 메시지)
     */
    public static List<KakaoMessage> filterReceivedMessages(ParseResult result) {
        if (result.getOtherUser() == null) {
            return result.getMessages();
        }
        
        return result.getMessages().stream()
            .filter(msg -> msg.getUser().equals(result.getOtherUser()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 모든 메시지를 대화 형식으로 변환 (맥락 분석용)
     * 🆕 나와 상대방을 명확히 구분
     */
    public static String toConversationContext(List<KakaoMessage> messages, int maxCount, String mainUser, String otherUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("최근 대화 내용 (분석 대상은 '상대방'입니다):\n\n");
        
        int count = Math.min(messages.size(), maxCount);
        int startIndex = Math.max(0, messages.size() - count);
        
        for (int i = startIndex; i < messages.size(); i++) {
            KakaoMessage msg = messages.get(i);
            
            // 🆕 나와 상대방 명확히 구분
            String sender;
            if (msg.getUser().equals(mainUser)) {
                sender = "나";
            } else if (msg.getUser().equals(otherUser)) {
                sender = "상대방";
            } else {
                sender = msg.getUser(); // 혹시 다른 사람이 있으면 이름 표시
            }
            
            sb.append(String.format("[%s] %s: %s\n", 
                msg.getDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                sender,
                msg.getMessage()));
        }
        
        return sb.toString();
    }
}
