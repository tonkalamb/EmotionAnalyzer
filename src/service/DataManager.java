package service;

import model.Emotion;
import model.Message;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private static final String DATA_FILE = "emotion_data.txt";
    private List<Message> messageHistory;
    
    public DataManager() {
        this.messageHistory = new ArrayList<>();
        loadData();
        System.out.println("📁 데이터 매니저 초기화 완료 (메시지 " + messageHistory.size() + "개)");
    }
    
    public void saveMessage(Message message) {
        if (message == null) {
            return;
        }
        
        messageHistory.add(message);
        saveData();
        System.out.println("💾 메시지 저장 완료: " + message.getSummary() + " (상대: " + message.getContactName() + ")");
    }
    
    public List<Message> getAllMessages() {
        return new ArrayList<>(messageHistory);
    }
    
    public List<Message> getMessagesByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        return messageHistory.stream()
            .filter(msg -> msg.getTimestamp().toLocalDate().equals(date))
            .collect(Collectors.toList());
    }
    
    public List<Message> getMessagesByEmotion(Emotion emotion) {
        if (emotion == null) {
            return new ArrayList<>();
        }
        
        return messageHistory.stream()
            .filter(msg -> msg.getEmotion() == emotion)
            .collect(Collectors.toList());
    }
    
    // 🆕 상대방별 메시지 조회
    public List<Message> getMessagesByContact(String contactName) {
        if (contactName == null || contactName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return messageHistory.stream()
            .filter(msg -> msg.getContactName().equals(contactName))
            .collect(Collectors.toList());
    }
    
    // 🆕 상대방별 메시지 조회 (최근 N개만)
    public List<Message> getMessagesByContact(String contactName, int limit) {
        if (contactName == null || contactName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Message> allMessages = messageHistory.stream()
            .filter(msg -> msg.getContactName().equals(contactName))
            .collect(Collectors.toList());
        
        // 최근 limit개만 반환
        int size = allMessages.size();
        int startIndex = Math.max(0, size - limit);
        
        return new ArrayList<>(allMessages.subList(startIndex, size));
    }
    
    // 🆕 모든 상대방 이름 목록
    public Set<String> getAllContactNames() {
        return messageHistory.stream()
            .map(Message::getContactName)
            .collect(Collectors.toSet());
    }
    
    public List<Message> getRecentMessages(int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }
        
        int size = messageHistory.size();
        int startIndex = Math.max(0, size - count);
        
        List<Message> recent = new ArrayList<>(messageHistory.subList(startIndex, size));
        Collections.reverse(recent);
        return recent;
    }
    
    public Map<LocalDate, Map<Emotion, Integer>> getDailyEmotionStats(int days) {
        Map<LocalDate, Map<Emotion, Integer>> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<Emotion, Integer> emotionCount = new HashMap<>();
            
            for (Emotion emotion : Emotion.values()) {
                emotionCount.put(emotion, 0);
            }
            
            List<Message> dailyMessages = getMessagesByDate(date);
            for (Message msg : dailyMessages) {
                emotionCount.put(msg.getEmotion(), 
                    emotionCount.get(msg.getEmotion()) + 1);
            }
            
            stats.put(date, emotionCount);
        }
        
        return stats;
    }
    
    public Map<Emotion, Integer> getEmotionDistribution() {
        Map<Emotion, Integer> distribution = new HashMap<>();
        
        for (Emotion emotion : Emotion.values()) {
            distribution.put(emotion, 0);
        }
        
        for (Message msg : messageHistory) {
            Emotion emotion = msg.getEmotion();
            distribution.put(emotion, distribution.get(emotion) + 1);
        }
        
        return distribution;
    }
    
    public Emotion getMostFrequentEmotion() {
        Map<Emotion, Integer> distribution = getEmotionDistribution();
        
        return distribution.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Emotion.NEUTRAL);
    }
    
    public double getAverageIntensity() {
        if (messageHistory.isEmpty()) {
            return 0.0;
        }
        
        double sum = messageHistory.stream()
            .mapToDouble(Message::getIntensity)
            .sum();
        
        return sum / messageHistory.size();
    }
    
    public int getTotalMessageCount() {
        return messageHistory.size();
    }
    
    public int getTodayMessageCount() {
        return getMessagesByDate(LocalDate.now()).size();
    }
    
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(DATA_FILE), "UTF-8"))) {
            
            for (Message msg : messageHistory) {
                writer.println(messageToString(msg));
            }
            
        } catch (IOException e) {
            System.err.println("❌ 데이터 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadData() {
        File file = new File(DATA_FILE);
        
        if (!file.exists()) {
            System.out.println("📄 데이터 파일이 없습니다. 새로 시작합니다.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            
            String line;
            int loadCount = 0;
            
            while ((line = reader.readLine()) != null) {
                Message msg = stringToMessage(line);
                if (msg != null) {
                    messageHistory.add(msg);
                    loadCount++;
                }
            }
            
            System.out.println("✅ " + loadCount + "개의 메시지를 불러왔습니다.");
            
        } catch (IOException e) {
            System.err.println("❌ 데이터 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 🆕 contactName 포함하여 저장
    private String messageToString(Message msg) {
        String content = msg.getContent().replace("|", "｜");
        String response = msg.getRecommendedResponse().replace("|", "｜");
        String contactName = msg.getContactName().replace("|", "｜");
        
        return String.format("%s|%s|%.3f|%s|%s|%s",
            msg.getTimestamp().toString(),
            msg.getEmotion().name(),
            msg.getIntensity(),
            content,
            response,
            contactName);
    }
    
    // 🆕 contactName 포함하여 로드 (하위 호환성 유지)
    private Message stringToMessage(String str) {
        try {
            String[] parts = str.split("\\|");
            
            if (parts.length < 5) {
                System.err.println("⚠️ 잘못된 데이터 형식: " + str);
                return null;
            }
            
            LocalDateTime timestamp = LocalDateTime.parse(parts[0]);
            Emotion emotion = Emotion.valueOf(parts[1]);
            double intensity = Double.parseDouble(parts[2]);
            String content = parts[3].replace("｜", "|");
            String response = parts[4].replace("｜", "|");
            String contactName = parts.length > 5 ? parts[5].replace("｜", "|") : "알 수 없음";
            
            Message msg = new Message(content, emotion, intensity, response, contactName);
            msg.setTimestamp(timestamp);
            return msg;
            
        } catch (Exception e) {
            System.err.println("⚠️ 메시지 파싱 실패: " + e.getMessage());
            return null;
        }
    }
    
    public void clearAllData() {
        messageHistory.clear();
        saveData();
        System.out.println("🗑️ 모든 데이터가 삭제되었습니다.");
    }
}
