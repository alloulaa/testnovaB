package com.example.testnova.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class CVservice {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public CVservice(ChatClient.Builder chatClientbuilder) {
        this.chatClient = chatClientbuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public Object analysecv(String cvtext, String providedOwnerName) {
        System.out.println("[CVService] Début de l'analyse IA");

        String cvOwnerName = providedOwnerName != null && !providedOwnerName.trim().isEmpty()
                ? providedOwnerName
                : extractNameFromCV(cvtext);
        System.out.println("[CVService] ownerName utilisé: " + cvOwnerName);

        String prompt = """
            Tu es un expert RH. Analyse le CV suivant (format texte) :
            
            %s
            
            Réponds UNIQUEMENT avec le JSON suivant, sans aucun texte supplémentaire, sans bloc de code, sans backticks, sans markdown. JSON pur et valide.
            Lis bien le texte avant de répondre. Si tu ne trouves pas une information, laisse-la vide.
            
            {
              "resume": "Résumé intelligent du profil en 2 à 3 phrases",
              "skills": [
                {
                  "name": "Compétence",
                  "level": "beginner|intermediate|advanced|expert"
                }
              ],
              "experience": [
                {
                  "company": "Nom de l'entreprise",
                  "role": "Poste occupé",
                  "year": "Année ou période",
                  "duration": "Durée en mois ou années",
                  "competences": ["Compétence utilisée 1", "Compétence utilisée 2"]
                }
              ],
              "ownerName": "%s"
            }
            """.formatted(cvtext, cvOwnerName);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[CVService] Analyse IA terminée");
            System.out.println("[CVService] JSON brut: " + jsonString.substring(0, Math.min(200, jsonString.length())) + "...");

            String cleanJson = extractJsonFromResponse(jsonString);
            System.out.println("[CVService] JSON nettoyé: " + cleanJson.substring(0, Math.min(100, cleanJson.length())) + "...");

            try {
                Object parsedJson = objectMapper.readValue(cleanJson, Object.class);
                System.out.println("[CVService] JSON parsé avec succès");
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = (Map<String, Object>) parsedJson;
                if (!jsonMap.containsKey("ownerName")) {
                    jsonMap.put("ownerName", cvOwnerName);
                    System.out.println("[CVService] ownerName ajouté manuellement au JSON");
                }
                return parsedJson;
            } catch (Exception parseException) {
                System.err.println("[CVService] Erreur parsing JSON nettoyé: " + parseException.getMessage());
                System.err.println("[CVService] JSON nettoyé: " + cleanJson);
                throw new RuntimeException("Le format JSON retourné par l'IA est invalide après nettoyage", parseException);
            }

        } catch (Exception e) {
            System.err.println("[CVService] Erreur IA: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'analyse IA: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";  // Fallback JSON vide
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

    private String extractNameFromCV(String cvText) {
        String[] lines = cvText.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 2 && trimmed.length() < 50) {
                System.out.println("[CVService] Nom extrait du CV: " + trimmed);
                return trimmed;
            }
        }
        System.out.println("[CVService] Nom par défaut: Candidat");
        return "Candidat";
    }
}