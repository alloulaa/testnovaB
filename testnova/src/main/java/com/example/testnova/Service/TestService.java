package com.example.testnova.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class TestService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public TestService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public Object generateTest(String cvAnalysisJson) {
        System.out.println("[TestService] Début génération test personnalisé");

        String prompt = """
            Tu es un expert technique. Génère un test personnalisé basé sur cette analyse CV (JSON):
            
            %s
            
            Réponds UNIQUEMENT avec le JSON suivant, sans texte supplémentaire, sans bloc de code, sans backticks. JSON pur et valide.
            
            {
              "questions": [
                {
                  "id": 1,
                  "text": "Question technique courte",
                  "type": "multiple|open",
                  "options": ["Option 1", "Option 2"]  // Si multiple
                }
              ],  // Exactement 10 questions techniques basées sur skills (ex. : Java, Angular, etc.)
              "problem": {
                "description": "Problème à résoudre (scénario réaliste)"
              }
            }
            """.formatted(cvAnalysisJson);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[TestService] Génération terminée");
            System.out.println("[TestService] JSON brut: " + jsonString.substring(0, Math.min(200, jsonString.length())) + "...");

            // Nettoyage (réutilise méthode de CVservice si possible, ou copie)
            String cleanJson = extractJsonFromResponse(jsonString);

            Object parsedJson = objectMapper.readValue(cleanJson, Object.class);
            System.out.println("[TestService] JSON parsé avec succès");
            return parsedJson;

        } catch (Exception e) {
            System.err.println("[TestService] Erreur génération test: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du test: " + e.getMessage(), e);
        }
    }

    // Méthode de nettoyage (copiée de CVservice pour indépendance)
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```\\s*", Pattern.DOTALL);
        var matcher = codeBlockPattern.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}') + 1;
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end).trim();
        }

        cleaned = cleaned.replaceAll("/\\*.*?\\*/", "").replaceAll("//.*", "").trim();

        return cleaned.isEmpty() ? "{}" : cleaned;
    }
}