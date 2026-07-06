package com.marvin.AI_Mechanic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class GeminiService {

    private static final String GEMINI_API_URL_TEMPLATE =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiService(@Value("${gemini.api.key:}") String apiKey,
                         @Value("${gemini.api.model:gemini-1.5-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get a response from Gemini for a given prompt.
     */
    public String getAiResponse(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Gemini API key is missing. Set GEMINI_API_KEY environment variable.";
        }

        try {
            String endpoint = String.format(
                GEMINI_API_URL_TEMPLATE,
                URLEncoder.encode(model, StandardCharsets.UTF_8),
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
            );

            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "Gemini API error (" + response.statusCode() + "): " + response.body();
            }

            return extractTextFromResponse(response.body());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Generate maintenance advice for a car.
     */
    public String generateMaintenanceAdvice(String carMake, String carModel) {
        String prompt = String.format(
            "Provide 3 key maintenance tips for a %s %s. Format as a numbered list with brief explanations.",
            carMake, carModel
        );
        return getAiResponse(prompt);
    }

    /**
     * Generate troubleshooting steps for a car issue.
     */
    public String generateTroubleshootingSteps(String carMake, String carModel, String issue) {
        String prompt = String.format(
            "Provide step-by-step troubleshooting for a %s %s with the following issue: %s. " +
            "Format as numbered steps with clear instructions.",
            carMake, carModel, issue
        );
        return getAiResponse(prompt);
    }

    /**
     * Generate repair estimates summary for a car.
     */
    public String generateRepairSummary(String carMake, String carModel, String repairType) {
        String prompt = String.format(
            "Provide a brief summary of typical costs and time for %s repairs on a %s %s.",
            repairType, carMake, carModel
        );
        return getAiResponse(prompt);
    }

    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");

        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        return objectMapper.writeValueAsString(root);
    }

    private String extractTextFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");

        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                String text = parts.get(0).path("text").asText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }

        JsonNode errorMessage = root.path("error").path("message");
        if (!errorMessage.isMissingNode() && !errorMessage.asText().isBlank()) {
            return "Gemini API error: " + errorMessage.asText();
        }

        return "No response generated";
    }
}
