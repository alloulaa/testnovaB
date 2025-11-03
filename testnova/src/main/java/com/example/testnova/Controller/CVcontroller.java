package com.example.testnova.Controller;

import com.example.testnova.Service.CVservice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CVcontroller {

    private final CVservice cvservice;

    public CVcontroller(CVservice cvservice) {
        this.cvservice = cvservice;
    }

    @PostMapping(value = "/analysecv", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analyze(@RequestBody Map<String, String> body) {
        try {
            System.out.println("[Backend] Requête reçue pour analyse CV");
            String textcv = body.get("textcv");
            String ownerName = body.get("ownerName");

            if (textcv == null || textcv.trim().isEmpty()) {
                System.out.println("[Backend] Erreur: texte CV vide");
                return ResponseEntity.badRequest().body(Map.of("error", "Le texte du CV est vide"));
            }

            System.out.println("[Backend] Texte CV reçu: " + textcv.substring(0, Math.min(100, textcv.length())) + "...");
            if (ownerName != null) {
                System.out.println("[Backend] ownerName fourni: " + ownerName);
            }

            Object result = cvservice.analysecv(textcv, ownerName);

            System.out.println("[Backend] Analyse terminée avec succès");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("[Backend] Erreur lors de l'analyse: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}