package com.marvin.AI_Mechanic.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestTemplate restTemplate;
    private final JavaMailSender mailSender;
    private final String brevoApiKey;
    private final String brevoEmailUrl;
    private final String defaultSenderEmail;
    private final String defaultSenderName;
    private final boolean smtpFallback;

    

    public EmailService(RestTemplate restTemplate,
                        JavaMailSender mailSender,
                        @Value("${brevo.api.key:}") String brevoApiKey,
                        @Value("${brevo.email.url:https://api.brevo.com/v3/smtp/email}") String brevoEmailUrl,
                        @Value("${brevo.email.sender:}") String defaultSenderEmail,
                        @Value("${brevo.email.sender-name:AI Mechanic}") String defaultSenderName,
                        @Value("${brevo.email.smtp-fallback:false}") boolean smtpFallback) {


                            log.info("Brevo Api Key: {}", (brevoApiKey == null || brevoApiKey.isBlank()) ? "(empty)" : "********");
    
        this.restTemplate = restTemplate;
        this.mailSender = mailSender;
        this.brevoApiKey = brevoApiKey;
        this.brevoEmailUrl = brevoEmailUrl;
        this.defaultSenderEmail = defaultSenderEmail;
        this.defaultSenderName = defaultSenderName;
        this.smtpFallback = smtpFallback;
    }

    public void sendEmailOtp(String emailTo, String emailFrom, String subject, String body) {
        try {
            sendViaBrevoApi(emailTo, emailFrom, subject, body);
            return;
        } catch (RestClientResponseException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.error(
                "Brevo API error for recipient {} -> status={}, responseBody='{}', keyMeta='{}'",
                emailTo,
                ex.getStatusCode().value(),
                (responseBody == null || responseBody.isBlank()) ? "(empty)" : responseBody,
                apiKeyMeta(normalizeApiKey(brevoApiKey))
            );
            if (!smtpFallback) {
                throw ex;
            }
            log.warn("Falling back to SMTP because brevo.email.smtp-fallback=true");
        } catch (RestClientException ex) {
            log.error("Brevo email API call failed for recipient {}", emailTo, ex);
            if (!smtpFallback) {
                throw ex;
            }
            log.warn("Falling back to SMTP because brevo.email.smtp-fallback=true");
        }

        sendViaSmtp(emailTo, emailFrom, subject, body);
    }

    private void sendViaBrevoApi(String emailTo, String emailFrom, String subject, String body) {
        log.info("Sending email via Brevo API to {} with subject '{}'", emailTo, subject);
        String normalizedApiKey = normalizeApiKey(brevoApiKey);
        if (normalizedApiKey == null || normalizedApiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY is missing");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", normalizedApiKey);

        Map<String, Object> sender = new HashMap<>();
        String resolvedSenderEmail = (emailFrom == null || emailFrom.isBlank()) ? defaultSenderEmail : emailFrom;
        if (resolvedSenderEmail == null || resolvedSenderEmail.isBlank()) {
            throw new IllegalStateException("Sender email is missing. Set BREVO_EMAIL_SENDER or pass emailFrom.");
        }
        sender.put("email", resolvedSenderEmail);
        sender.put("name", defaultSenderName);

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", emailTo);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", sender);
        payload.put("to", List.of(recipient));
        payload.put("subject", subject);
        payload.put("textContent", body);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(brevoEmailUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Brevo email API returned non-success status: " + response.getStatusCode());
        }

        log.info("Email sent via Brevo API to {} with status {}", emailTo, response.getStatusCode().value());
    }

    private String normalizeApiKey(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = raw.trim();
        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            normalized = normalized.substring(7).trim();
        }
        return normalized;
    }

    private String apiKeyMeta(String key) {
        if (key == null || key.isBlank()) {
            return "empty";
        }
        int length = key.length();
        int previewLen = Math.min(6, length);
        String prefix = key.substring(0, previewLen);
        return "len=" + length + ",prefix=" + prefix + "***";
    }

    private void sendViaSmtp(String emailTo, String emailFrom, String subject, String body) {
        SimpleMailMessage simpleMail = new SimpleMailMessage();
        String resolvedSenderEmail = (emailFrom == null || emailFrom.isBlank()) ? defaultSenderEmail : emailFrom;
        simpleMail.setFrom(resolvedSenderEmail);
        simpleMail.setSubject(subject);
        simpleMail.setTo(emailTo);
        simpleMail.setText(body);
        mailSender.send(simpleMail);
    }
}
