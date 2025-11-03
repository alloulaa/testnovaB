package com.example.testnova.Controller;

import com.example.testnova.Service.TestService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping(value = "/generateTest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateTest(@RequestBody Map<String, Object> analysis) {
        try {
            System.out.println("[TestController] Requête reçue pour génération test");
            if (analysis == null || !analysis.containsKey("skills")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Analyse CV manquante"));
            }

            String analysisJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(analysis);
            Object testResult = testService.generateTest(analysisJson);

            System.out.println("[TestController] Test généré avec succès");
            return ResponseEntity.ok(testResult);

        } catch (Exception e) {
            System.err.println("[TestController] Erreur génération test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}