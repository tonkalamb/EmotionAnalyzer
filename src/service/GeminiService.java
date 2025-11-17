package service;

import model.Emotion;
import model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiService {
    
    // ⚠️ 여기에 발급받은 Gemini API 키를 입력하세요!
    private static final String API_KEY = "";
    
    private static final String API_URL = 
"https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    
    private static final int TIMEOUT = 30000;
    
    public Message analyzeEmotion(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("분석할 텍스트가 비어있습니다.");
        }
        
        if (!isApiKeySet()) {
            throw new IllegalStateException(
                "API 키가 설정되지 않았습니다.\n" +
                "GeminiService.java 파일에서 API_KEY를 설정해주세요.");
        }
        
        System.out.println("📡 Gemini API 호출 중...");
        
        String prompt = createEmotionAnalysisPrompt(text);
        String response = callGeminiAPI(prompt);
        Message result = parseEmotionResponse(text, response);
        
        System.out.println("✅ 감정 분석 완료: " + result.getEmotion().getKorean());
        
        return result;
    }
    
    private String createEmotionAnalysisPrompt(String text) {
        // 입력 텍스트의 언어 감지 (한글 포함 여부)
        boolean isKorean = text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        
        if (isKorean) {
            // 한국어 프롬프트
            return "당신은 감정 분석 전문가입니다. 다음 문장의 감정을 정확하게 분석해주세요.\n\n" +
                   "⚠️ 반드시 아래 형식을 정확히 지켜서 답변해주세요:\n\n" +
                   "감정: [기쁨/슬픔/분노/공포/혐오/놀람/중립 중 정확히 하나만]\n" +
                   "강도: [0.0에서 1.0 사이의 소수점 숫자]\n" +
                   "분석: [감정 분석 이유를 1-2문장으로]\n" +
                   "추천답변: [상황에 맞는 공감하고 적절한 답변 1-2문장]\n\n" +
                   "분석할 문장: \"" + text + "\"\n\n" +
                   "위 형식을 정확히 지켜서 답변해주세요.";
        } else {
            // 영어 프롬프트
            return "You are an emotion analysis expert. Please accurately analyze the emotion of the following sentence.\n\n" +
                   "⚠️ Please follow this format exactly:\n\n" +
                   "감정: [Exactly one of: 기쁨/슬픔/분노/공포/혐오/놀람/중립]\n" +
                   "강도: [A decimal number between 0.0 and 1.0]\n" +
                   "분석: [Reason for emotion analysis in 1-2 sentences IN ENGLISH]\n" +
                   "추천답변: [An empathetic and appropriate response in 1-2 sentences IN ENGLISH]\n\n" +
                   "Sentence to analyze: \"" + text + "\"\n\n" +
                   "Please follow the format exactly. Write your analysis and recommended response in English, but keep the field labels (감정:, 강도:, 분석:, 추천답변:) in Korean.";
        }
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
    
    public static boolean isApiKeySet() {
        return !API_KEY.equals("YOUR_GEMINI_API_KEY_HERE") && 
               API_KEY != null && 
               !API_KEY.trim().isEmpty();
    }
}