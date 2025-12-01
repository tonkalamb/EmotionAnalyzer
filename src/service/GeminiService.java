package service;

import model.Emotion;
import model.Message;
import model.MBTI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GeminiService {
    
    private static final String API_KEY = "";
    
    private static final String API_URL = 
"https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    
    private static final int TIMEOUT = 30000;
    
    public Message analyzeEmotion(String text) throws Exception {
        return analyzeEmotion(text, null);
    }
    
    public Message analyzeEmotion(String text, MBTI mbti) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("분석할 텍스트가 비어있습니다.");
        }
        
        if (!isApiKeySet()) {
            throw new IllegalStateException(
                "API 키가 설정되지 않았습니다.\n" +
                "GeminiService.java 파일에서 API_KEY를 설정해주세요.");
        }
        
        System.out.println("📡 Gemini API 호출 중..." + 
            (mbti != null && mbti != MBTI.UNKNOWN ? " (MBTI: " + mbti.getCode() + ")" : ""));
        
        String prompt = createEmotionAnalysisPrompt(text, mbti);
        String response = callGeminiAPI(prompt);
        Message result = parseEmotionResponse(text, response);
        
        System.out.println("✅ 감정 분석 완료: " + result.getEmotion().getKorean());
        
        return result;
    }

    
    public Message analyzeEmotionWithContext(String currentMessage, String conversationContext, MBTI mbti) throws Exception {
        if (currentMessage == null || currentMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("분석할 텍스트가 비어있습니다.");
        }
        
        if (!isApiKeySet()) {
            throw new IllegalStateException(
                "API 키가 설정되지 않았습니다.\n" +
                "GeminiService.java 파일에서 API_KEY를 설정해주세요.");
        }
        
        System.out.println("📡 맥락 기반 감정 분석 중..." + 
            (mbti != null && mbti != MBTI.UNKNOWN ? " (MBTI: " + mbti.getCode() + ")" : ""));
        
        String prompt = createContextAnalysisPrompt(currentMessage, conversationContext, mbti);
        String response = callGeminiAPI(prompt);
        Message result = parseEmotionResponse(currentMessage, response);
        
        System.out.println("✅ 맥락 기반 감정 분석 완료: " + result.getEmotion().getKorean());
        
        return result;
    }
    
    private String createContextAnalysisPrompt(String currentMessage, String conversationContext, MBTI mbti) {
        boolean isKorean = isKoreanText(currentMessage);
        
        String mbtiContext = "";
        if (mbti != null && mbti != MBTI.UNKNOWN) {
            mbtiContext = "\n\n🧠 **상대방 MBTI: " + mbti.getCode() + " (" + mbti.getNickname() + ")**\n" +
                         "특성: " + mbti.getCharacteristic() + "\n" +
                         "감정 해석 가이드: " + mbti.getEmotionInterpretationGuideline() + "\n";
        }
        
        if (isKorean) {
            return "당신은 감정 분석 전문가입니다.\n\n" +
                   "📚 **이전 대화 맥락:**\n" +
                   conversationContext +
                   "\n\n" +
                   mbtiContext +
                   "\n\n🎯 **지금 막 받은 메시지 (분석 대상):**\n" +
                   "\"" + currentMessage + "\"\n\n" +
                   "⚠️ 중요: 위의 이전 대화 내용을 반드시 참고하여, 지금 받은 메시지의 감정을 분석하고 답변을 추천해주세요.\n" +
                   "상대방이 이전에 어떤 말을 했는지, 어떤 상황인지 맥락을 고려해서 분석하세요.\n\n" + 
                   "반드시 아래 형식을 정확히 지켜서 답변해주세요:\n\n" +
                   "감정: [기쁨/슬픔/분노/공포/혐오/놀람/중립 중 정확히 하나만]\n" +
                   "강도: [0.0에서 1.0 사이의 소수점 숫자]\n" +
                   "분석: [대화 맥락을 고려한 감정 분석 이유를 2-3문장으로 한국어로]\n" +
                   "추천답변: [대화 흐름과 상대방 감정을 고려한 공감적이고 적절한 답변 1-2문장을 한국어로]\n\n" +
                   "⚠️ 중요: 분석과 추천답변은 반드시 한국어로 작성하세요!";
        } else {
            return "You are an emotion analysis expert.\n\n" +
                   "📚 **Previous Conversation Context:**\n" +
                   conversationContext +
                   "\n\n" +
                   mbtiContext +
                   "\n\n🎯 **Current Message Just Received (Target for Analysis):**\n" +
                   "\"" + currentMessage + "\"\n\n" +
                   "⚠️ IMPORTANT: You must consider the previous conversation context above when analyzing this current message.\n" +
                   "Consider what the person said before and the current situation based on the context.\n\n" +
                   "Please follow this format exactly:\n\n" +
                   "감정: [Exactly one of: 기쁨/슬픔/분노/공포/혐오/놀람/중립]\n" +
                   "강도: [A decimal number between 0.0 and 1.0]\n" +
                   "분석: [Reason for emotion analysis considering context, 2-3 sentences IN ENGLISH]\n" +
                   "추천답변: [An empathetic and appropriate response considering conversation flow, 1-2 sentences IN ENGLISH]\n\n" +
                   "Please follow the format exactly.";
        }
    }
    
    private String createEmotionAnalysisPrompt(String text, MBTI mbti) {
        // 🔧 수정: 한글 비율로 판단
        boolean isKorean = isKoreanText(text);
        
        String mbtiContext = "";
        if (mbti != null && mbti != MBTI.UNKNOWN) {
            mbtiContext = "\n\n🧠 **상대방 MBTI: " + mbti.getCode() + " (" + mbti.getNickname() + ")**\n" +
                         "특성: " + mbti.getCharacteristic() + "\n" +
                         "감정 해석 가이드: " + mbti.getEmotionInterpretationGuideline() + "\n\n" +
                         "⚠️ 이 MBTI 특성을 고려하여 감정을 분석하고, 답변을 추천해주세요.\n" +
                         "예: INFP가 '괜찮아'라고 하면 실제로는 힘들 수 있음. ESTJ가 '괜찮아'라고 하면 정말 괜찮음.";
        }
        
        if (isKorean) {
            return "당신은 감정 분석 전문가입니다. 다음 문장의 감정을 정확하게 분석해주세요." +
                   mbtiContext +
                   "\n\n⚠️ 반드시 아래 형식을 정확히 지켜서 답변해주세요:\n\n" +
                   "감정: [기쁨/슬픔/분노/공포/혐오/놀람/중립 중 정확히 하나만]\n" +
                   "강도: [0.0에서 1.0 사이의 소수점 숫자]\n" +
                   "분석: [감정 분석 이유를 1-2문장으로 한국어로]\n" +
                   "추천답변: [상황에 맞는 공감하고 적절한 답변 1-2문장을 한국어로]\n\n" +
                   "분석할 문장: \"" + text + "\"\n\n" +
                   "⚠️ 중요: 분석과 추천답변은 반드시 한국어로 작성하세요!";
        } else {
            return "You are an emotion analysis expert. Please accurately analyze the emotion of the following sentence." +
                   mbtiContext +
                   "\n\n⚠️ Please follow this format exactly:\n\n" +
                   "감정: [Exactly one of: 기쁨/슬픔/분노/공포/혐오/놀람/중립]\n" +
                   "강도: [A decimal number between 0.0 and 1.0]\n" +
                   "분석: [Reason for emotion analysis in 1-2 sentences IN ENGLISH]\n" +
                   "추천답변: [An empathetic and appropriate response in 1-2 sentences IN ENGLISH]\n\n" +
                   "Sentence to analyze: \"" + text + "\"\n\n" +
                   "Please follow the format exactly.";
        }
    }
    
    // 🆕 한글 비율로 한국어 판단
    private boolean isKoreanText(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        
        int totalChars = 0;
        int koreanChars = 0;
        
        for (char c : text.toCharArray()) {
            // 공백, 숫자, 특수문자 제외
            if (Character.isWhitespace(c) || Character.isDigit(c) || !Character.isLetterOrDigit(c)) {
                continue;
            }
            
            totalChars++;
            
            // 한글 체크
            if ((c >= 0xAC00 && c <= 0xD7A3) ||  // 완성형 한글
                (c >= 0x1100 && c <= 0x11FF) ||  // 한글 자음
                (c >= 0x3130 && c <= 0x318F) ||  // 한글 호환 자모
                (c >= 0xA960 && c <= 0xA97F) ||  // 한글 자음 확장
                (c >= 0xD7B0 && c <= 0xD7FF)) {  // 한글 자음 확장-B
                koreanChars++;
            }
        }
        
        // 문자가 거의 없으면 한국어로 간주
        if (totalChars < 5) {
            return true;
        }
        
        // 한글 비율이 30% 이상이면 한국어
        double koreanRatio = (double) koreanChars / totalChars;
        System.out.println("📝 언어 판단: 총 " + totalChars + "자 중 한글 " + koreanChars + "자 (" + 
            String.format("%.1f%%", koreanRatio * 100) + ") → " + (koreanRatio >= 0.3 ? "한국어" : "영어"));
        return koreanRatio >= 0.3;
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        URL url = new URL(API_URL + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                
                String errorMsg = "API 호출 실패 (코드: " + responseCode + ")\n";
                if (responseCode == 403) {
                    errorMsg += "API 키가 올바르지 않거나 권한이 없습니다.";
                } else if (responseCode == 429) {
                    errorMsg += "API 호출 한도를 초과했습니다.";
                } else {
                    errorMsg += "오류 내용: " + errorResponse.toString();
                }
                
                throw new Exception(errorMsg);
            }
            
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();
            
            return response.toString();
            
        } finally {
            conn.disconnect();
        }
    }
    
    private Message parseEmotionResponse(String originalText, String apiResponse) {
        try {
            JSONObject jsonResponse = new JSONObject(apiResponse);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            
            if (candidates.length() == 0) {
                throw new Exception("API 응답에 결과가 없습니다.");
            }
            
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String text = parts.getJSONObject(0).getString("text");
            
            System.out.println("📄 AI 응답:\n" + text);
            
            Emotion emotion = Emotion.NEUTRAL;
            double intensity = 0.5;
            String recommendedResponse = "";
            
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("감정:") || line.startsWith("감정 :")) {
                    String emotionStr = line.substring(line.indexOf(":") + 1).trim();
                    emotionStr = emotionStr.replaceAll("[\\[\\]\\(\\)]", "").trim();
                    emotion = Emotion.fromKorean(emotionStr);
                    
                } else if (line.startsWith("강도:") || line.startsWith("강도 :")) {
                    String intensityStr = line.substring(line.indexOf(":") + 1).trim();
                    try {
                        intensityStr = intensityStr.replaceAll("[^0-9.]", "");
                        double parsedIntensity = Double.parseDouble(intensityStr);
                        
                        if (parsedIntensity > 1.0 && parsedIntensity <= 100) {
                            parsedIntensity = parsedIntensity / 100.0;
                        }
                        
                        intensity = Math.max(0.0, Math.min(1.0, parsedIntensity));
                    } catch (NumberFormatException e) {
                        intensity = 0.5;
                    }
                    
                } else if (line.startsWith("추천답변:") || line.startsWith("추천답변 :") ||
                          line.startsWith("추천 답변:") || line.startsWith("추천 답변 :")) {
                    recommendedResponse = line.substring(line.indexOf(":") + 1).trim();
                }
            }
            
            if (recommendedResponse.isEmpty()) {
                recommendedResponse = generateDefaultResponse(emotion);
            }
            
            return new Message(originalText, emotion, intensity, recommendedResponse);
            
        } catch (Exception e) {
            System.err.println("❌ 응답 파싱 실패: " + e.getMessage());
            e.printStackTrace();
            return new Message(originalText, Emotion.NEUTRAL, 0.5, 
                "응답 분석 중 오류가 발생했습니다.");
        }
    }
    
    private String generateDefaultResponse(Emotion emotion) {
        switch (emotion) {
            case JOY:
                return "정말 좋은 소식이네요! 함께 기뻐할게요 😊";
            case SADNESS:
                return "힘든 일이 있으신가 봐요. 괜찮으시길 바랄게요.";
            case ANGER:
                return "화가 많이 나셨나 봐요. 충분히 이해할 수 있어요.";
            case FEAR:
                return "걱정이 많으시겠어요. 함께 해결 방법을 찾아봐요.";
            case DISGUST:
                return "불편하셨겠어요. 그런 기분 충분히 이해해요.";
            case SURPRISE:
                return "정말 놀라셨겠어요! 어떤 일이 있었는지 궁금하네요.";
            case NEUTRAL:
            default:
                return "말씀 잘 들었어요. 어떻게 도와드릴까요?";
        }
    }
    
    // 🆕 대화 기록으로 상대방 프로필 자동 생성
    public String generateContactProfile(List<model.Message> messages, String contactName, MBTI mbti) throws Exception {
        if (messages == null || messages.isEmpty()) {
            return "아직 충분한 대화 데이터가 없습니다.";
        }
        
        if (messages.size() < 5) {
            return "프로필 생성에는 최소 5개 이상의 대화가 필요합니다. (현재: " + messages.size() + "개)";
        }
        
        System.out.println("🧠 상대방 프로필 생성 중... (" + messages.size() + "개 메시지 분석)");
        
        StringBuilder messageContext = new StringBuilder();
        messageContext.append("다음은 '").append(contactName).append("'님과의 대화 기록입니다:\n\n");
        
        for (int i = 0; i < Math.min(messages.size(), 20); i++) {
            model.Message msg = messages.get(i);
            messageContext.append(String.format("%d. [%s] %s님: \"%s\"\n",
                i + 1,
                msg.getFormattedTimestamp(),
                contactName,
                msg.getContent()));
            messageContext.append(String.format("   감정: %s (%d%%)\n\n",
                msg.getEmotion().getKorean(),
                msg.getIntensityPercent()));
        }
        
        String mbtiInfo = "";
        if (mbti != null && mbti != MBTI.UNKNOWN) {
            mbtiInfo = "\n\n참고: 이 사람의 MBTI는 " + mbti.getDisplayName() + "입니다.\n" +
                      "특성: " + mbti.getCharacteristic();
        }
        
        String prompt = "당신은 심리 분석 전문가입니다.\n\n" +
                       messageContext.toString() +
                       mbtiInfo +
                       "\n\n위 대화 기록을 분석하여, 이 사람의 성향을 요약해주세요.\n\n" +
                       "다음 항목을 포함해서 3-4문장으로 작성하세요:\n" +
                       "1. 평소 감정 표현 방식 (솔직한지, 절제적인지)\n" +
                       "2. 자주 나타나는 감정 패턴\n" +
                       "3. 스트레스나 힘들 때의 특징적인 반응\n" +
                       "4. 이 사람과 대화할 때 주의할 점\n\n" +
                       "⚠️ 반드시 한국어로, 존댓말로, 객관적이고 따뜻한 어조로 작성하세요.";
        
        String response = callGeminiAPI(prompt);
        
        try {
            JSONObject jsonResponse = new org.json.JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            
            if (candidates.length() == 0) {
                throw new Exception("API 응답에 결과가 없습니다.");
            }
            
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String profile = parts.getJSONObject(0).getString("text");
            
            System.out.println("✅ 프로필 생성 완료!");
            
            return profile.trim();
            
        } catch (Exception e) {
            System.err.println("❌ 프로필 생성 실패: " + e.getMessage());
            return "프로필 생성 중 오류가 발생했습니다.";
        }
    }

    // 🆕 이미지에서 텍스트 추출 (OCR) + 감정 분석
    public Message analyzeImageWithOCR(java.io.File imageFile, MBTI mbti) throws Exception {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("이미지 파일이 존재하지 않습니다.");
        }
        
        if (!isApiKeySet()) {
            throw new IllegalStateException("API 키가 설정되지 않았습니다.");
        }
        
        System.out.println("📷 이미지 OCR 분석 중: " + imageFile.getName());
        
        // 이미지를 Base64로 인코딩
        String base64Image = encodeImageToBase64(imageFile);
        
        // Gemini Vision API 호출
        String extractedText = extractTextFromImage(base64Image);
        
        System.out.println("📝 추출된 텍스트: " + extractedText);
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new Exception("이미지에서 텍스트를 추출할 수 없습니다.");
        }
        
        // 추출된 텍스트로 감정 분석
        return analyzeEmotion(extractedText.trim(), mbti);
    }
    
    // 이미지를 Base64로 인코딩
    public String encodeImageToBase64(java.io.File imageFile) throws Exception {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(imageFile)) {
            byte[] imageBytes = fis.readAllBytes();
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new Exception("이미지 인코딩 실패: " + e.getMessage());
        }
    }
    
    // Gemini Vision API로 이미지에서 텍스트 추출
    public String extractTextFromImage(String base64Image) throws Exception {
        // 이미지 타입 감지 (간단하게 png로 가정, 실제로는 확장자 체크 필요)
        String mimeType = "image/png";
        
        String prompt = "이 이미지는 메신저 대화 스크린샷입니다.\n\n" +
                       "⚠️ 다음 규칙에 따라 분석하세요:\n\n" +
                       "1. 말풍선의 색상과 위치를 보고 '나'와 '상대방'을 구분하세요\n" +
                       "   - 보통 오른쪽 정렬 = 나, 왼쪽 정렬 = 상대방\n" +
                       "   - 색상이 다른 말풍선 = 다른 발신자\n\n" +
                       "2. 대화 내용을 시간 순서대로 파싱하세요\n\n" +
                       "3. 각 메시지를 다음 형식으로 출력하세요:\n" +
                       "   [나] 메시지내용\n" +
                       "   [상대방] 메시지내용\n\n" +
                       "4. 만약 구분이 어려우면, 가장 최근(아래쪽)의 메시지만 출력하되\n" +
                       "   발신자 구분 없이 메시지 내용만 출력하세요\n\n" +
                       "5. 텍스트가 없으면 '텍스트 없음'이라고만 출력하세요\n\n" +
                       "⚠️ 출력 예시:\n" +
                       "[상대방] 안녕 오늘 어때?\n" +
                       "[나] 좋아! 너는?\n" +
                       "[상대방] 나도 좋아";
        
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        // 텍스트 파트
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);
        
        // 이미지 파트
        JSONObject imagePart = new JSONObject();
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);
        imagePart.put("inline_data", inlineData);
        parts.put(imagePart);
        
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);
        
        // API 호출
        URL url = new URL(API_URL + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                throw new Exception("OCR API 호출 실패 (코드: " + responseCode + ")");
            }
            
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();
            
            // 응답 파싱
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            
            if (candidates.length() == 0) {
                throw new Exception("OCR 응답에 결과가 없습니다.");
            }
            
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject contentObj = candidate.getJSONObject("content");
            JSONArray partsArray = contentObj.getJSONArray("parts");
            String extractedText = partsArray.getJSONObject(0).getString("text");
            
            return extractedText.trim();
            
        } finally {
            conn.disconnect();
        }
    }

    public static boolean isApiKeySet() {
        return !API_KEY.equals("YOUR_GEMINI_API_KEY_HERE") && 
               API_KEY != null && 
               !API_KEY.trim().isEmpty();
    }
}
