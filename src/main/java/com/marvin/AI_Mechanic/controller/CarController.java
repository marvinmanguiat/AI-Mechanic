package com.marvin.AI_Mechanic.controller;

import com.marvin.AI_Mechanic.dto.DiagnosisRequest;
import com.marvin.AI_Mechanic.model.AiInquiry;
import com.marvin.AI_Mechanic.model.CarMake;
import com.marvin.AI_Mechanic.model.CarModel;
import com.marvin.AI_Mechanic.service.AiInquiryService;
import com.marvin.AI_Mechanic.service.CarService;
import com.marvin.AI_Mechanic.service.GeminiService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private AiInquiryService aiInquiryService;

    /**
     * Get all car makes
     * GET /api/cars/makes
     */
    @GetMapping("/makes")
    public ResponseEntity<List<CarMake>> getAllMakes() {
        List<CarMake> makes = carService.getAllMakes();
        return ResponseEntity.ok(makes);
    }

    /**
     * Get a specific make by ID
     * GET /api/cars/makes/{id}
     */
    @GetMapping("/makes/{id}")
    public ResponseEntity<?> getMakeById(@PathVariable Long id) {
        Optional<CarMake> make = carService.getMakeById(id);
        if (make.isPresent()) {
            return ResponseEntity.ok(make.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get models for a specific make
     * GET /api/cars/makes/{id}/models
     */
    @GetMapping("/makes/{id}/models")
    public ResponseEntity<List<CarModel>> getModelsByMakeId(@PathVariable Long id) {
        // Verify that the make exists
        Optional<CarMake> make = carService.getMakeById(id);
        if (make.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<CarModel> models = carService.getModelsByMakeId(id);
        return ResponseEntity.ok(models);
    }

    /**
     * Get all models for all makes (with nested structure)
     * GET /api/cars/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<CarMake>> getAllMakesWithModels() {
        List<CarMake> makes = carService.getAllMakes();
        return ResponseEntity.ok(makes);
    }

    /**
     * Add a new car make
     * POST /api/cars/makes
     */
    @PostMapping("/makes")
    public ResponseEntity<CarMake> addMake(@RequestParam String name) {
        CarMake make = carService.addMake(name);
        return ResponseEntity.status(201).body(make);
    }

    /**
     * Add a model to a specific make
     * POST /api/cars/makes/{id}/models
     */
    @PostMapping("/makes/{id}/models")
    public ResponseEntity<?> addModelToMake(@PathVariable Long id, @RequestParam String name) {
        CarModel model = carService.addModelToMake(id, name);
        if (model != null) {
            return ResponseEntity.status(201).body(model);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update a car make
     * PUT /api/cars/makes/{id}
     */
    @PutMapping("/makes/{id}")
    public ResponseEntity<?> updateMake(@PathVariable Long id, @RequestParam String name) {
        return carService.updateMake(id, name)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a car make
     * DELETE /api/cars/makes/{id}
     */
    @DeleteMapping("/makes/{id}")
    public ResponseEntity<Void> deleteMake(@PathVariable Long id) {
        boolean deleted = carService.deleteMake(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a car model
     * PUT /api/cars/models/{id}
     */
    @PutMapping("/models/{id}")
    public ResponseEntity<?> updateModel(@PathVariable Long id, @RequestParam String name) {
        return carService.updateModel(id, name)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a car model
     * DELETE /api/cars/models/{id}
     */
    @DeleteMapping("/models/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        boolean deleted = carService.deleteModel(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

        /**
         * Get maintenance advice for a car using Gemini
     * GET /api/cars/{make}/{model}/maintenance
     */
    @GetMapping("/{make}/{model}/maintenance")
    public ResponseEntity<String> getMaintenanceAdvice(@PathVariable String make, 
                                                       @PathVariable String model) {
                String advice = geminiService.generateMaintenanceAdvice(make, model);
        return ResponseEntity.ok(advice);
    }

    /**
         * Get troubleshooting steps for a car issue using Gemini
     * GET /api/cars/{make}/{model}/troubleshoot
     */
    @GetMapping("/{make}/{model}/troubleshoot")
    public ResponseEntity<String> getTroubleshootingSteps(@PathVariable String make,
                                                          @PathVariable String model,
                                                          @RequestParam String issue) {
                String steps = geminiService.generateTroubleshootingSteps(make, model, issue);
        return ResponseEntity.ok(steps);
    }

    /**
         * Get repair summary for a car using Gemini
     * GET /api/cars/{make}/{model}/repair-summary
     */
    @GetMapping("/{make}/{model}/repair-summary")
    public ResponseEntity<String> getRepairSummary(@PathVariable String make,
                                                    @PathVariable String model,
                                                    @RequestParam String repairType) {
                String summary = geminiService.generateRepairSummary(make, model, repairType);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get general AI response for a custom prompt
     * POST /api/cars/ai/ask
     */
    @PostMapping("/ai/ask")
    public ResponseEntity<String> askAI(@RequestBody DiagnosisRequest request, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication is required");
        }

        String username = authentication.getName();
        if (aiInquiryService.hasReachedDailyLimit(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Daily AI inquiry limit reached (5 per day). Please try again tomorrow.");
        }

        String symptoms = String.join("\n- ", request.getSymptoms());
        symptoms = "- " + symptoms;

        String prompt = """
                You are an ASE-certified automotive technician.

                Analyze the following vehicle carefully.

                Estimated Repair Cost shoud be in Philippine Pesos (PHP).

                Vehicle Information:
                Make: %s
                Model: %s
                Year: %d
                Engine: %s
                Transmission: %s

                Symptoms:
                %s

                Please provide:

                ## 1. Most Likely Causes
                List the possible causes from most likely to least likely.

                ## 2. Probability
                Assign an estimated probability (0-100%%) for each possible cause.

                ## 3. Recommended Inspections
                Explain what should be inspected first.

                ## 4. Repair Difficulty
                Rate each repair:
                - Easy
                - Moderate
                - Difficult

                ## 5. Estimated Repair Cost
                Give a rough estimate.

                ## 6. Is it safe to drive?
                Explain whether the vehicle should still be driven.

                ## 7. Summary
                Give a short conclusion.

                Respond using Markdown.
                """
                .formatted(
                        request.getMake(),
                        request.getModel(),
                        request.getYear(),
                        request.getEngine(),
                        request.getTransmission(),
                        symptoms
                );

        String response = geminiService.getAiResponse(prompt);

        // Save only successful AI inquiries so users can retrieve them later.
        aiInquiryService.saveInquiry(username, request, response);

        return ResponseEntity.ok(response);
    }

    /**
     * Get inquiry history for the currently logged-in user.
     * GET /api/cars/ai/inquiries
     */
    @GetMapping("/ai/inquiries")
    public ResponseEntity<List<AiInquiryResponse>> getAiInquiryHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AiInquiryResponse> history = aiInquiryService.getInquiryHistory(authentication.getName())
            .stream()
            .map(AiInquiryResponse::from)
            .toList();

        return ResponseEntity.ok(history);
    }

    /**
     * Lightweight AI health check.
     * GET /api/cars/ai/health
     */
    @GetMapping("/ai/health")
    public ResponseEntity<AiHealthResponse> aiHealth() {
        long startedAt = System.currentTimeMillis();

        if (!geminiService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AiHealthResponse(
                    "gemini",
                    geminiService.getModel(),
                    "DOWN",
                    System.currentTimeMillis() - startedAt,
                    "Gemini API key is not configured"
                ));
        }

        String probe = geminiService.checkHealth();
        long latencyMs = System.currentTimeMillis() - startedAt;
        boolean up = !(probe.startsWith("Error:") || probe.startsWith("Gemini API error"));

        HttpStatus status = up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        String detail = up ? "Gemini is reachable" : probe;

        return ResponseEntity.status(status)
            .body(new AiHealthResponse("gemini", geminiService.getModel(), up ? "UP" : "DOWN", latencyMs, detail));
    }

    /**
     * Simple DTO for AI prompt requests
     */
    public static class AiPromptRequest {
        private String prompt;

        public AiPromptRequest() {}

        public AiPromptRequest(String prompt) {
            this.prompt = prompt;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    /**
     * Simple DTO for AI health responses.
     */
    public static class AiHealthResponse {
        private String provider;
        private String model;
        private String status;
        private long latencyMs;
        private String detail;

        public AiHealthResponse() {}

        public AiHealthResponse(String provider, String model, String status, long latencyMs, String detail) {
            this.provider = provider;
            this.model = model;
            this.status = status;
            this.latencyMs = latencyMs;
            this.detail = detail;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public void setLatencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    /**
     * DTO for returning AI inquiry history.
     */
    public static class AiInquiryResponse {
        private Long id;
        private String make;
        private String model;
        private int year;
        private String engine;
        private String transmission;
        private String symptoms;
        private String aiResponse;
        private Instant createdAt;

        public static AiInquiryResponse from(AiInquiry inquiry) {
            AiInquiryResponse response = new AiInquiryResponse();
            response.setId(inquiry.getId());
            response.setMake(inquiry.getMake());
            response.setModel(inquiry.getModel());
            response.setYear(inquiry.getYear());
            response.setEngine(inquiry.getEngine());
            response.setTransmission(inquiry.getTransmission());
            response.setSymptoms(inquiry.getSymptoms());
            response.setAiResponse(inquiry.getAiResponse());
            response.setCreatedAt(inquiry.getCreatedAt());
            return response;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getTransmission() {
            return transmission;
        }

        public void setTransmission(String transmission) {
            this.transmission = transmission;
        }

        public String getSymptoms() {
            return symptoms;
        }

        public void setSymptoms(String symptoms) {
            this.symptoms = symptoms;
        }

        public String getAiResponse() {
            return aiResponse;
        }

        public void setAiResponse(String aiResponse) {
            this.aiResponse = aiResponse;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}
