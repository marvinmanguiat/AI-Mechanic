package com.marvin.AI_Mechanic.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String defaultSenderEmail;
    private final String defaultSenderName;

    public EmailService(JavaMailSender mailSender,
                        @Value("${mail.from.email:}") String defaultSenderEmail,
                        @Value("${mail.from.name:AI Mechanic}") String defaultSenderName) {
        this.mailSender = mailSender;
        this.defaultSenderEmail = defaultSenderEmail;
        this.defaultSenderName = defaultSenderName;
    }

    public void sendEmailOtp(String emailTo, String emailFrom, String subject, String body) {
        try {
            sendViaSmtp(emailTo, emailFrom, subject, body);
        } catch (Exception ex) {
            log.error("SMTP email send failed for recipient {}", emailTo, ex);
            throw new IllegalArgumentException("Email delivery failed via SMTP.");
        }
    }

    private void sendViaSmtp(String emailTo, String emailFrom, String subject, String body) {
        SimpleMailMessage simpleMail = new SimpleMailMessage();
        String resolvedSenderEmail = (emailFrom == null || emailFrom.isBlank()) ? defaultSenderEmail : emailFrom;
        if (resolvedSenderEmail == null || resolvedSenderEmail.isBlank()) {
            throw new IllegalStateException("Sender email is missing. Set MAIL_FROM_EMAIL or pass emailFrom.");
        }
        simpleMail.setFrom(resolvedSenderEmail);
        simpleMail.setSubject(subject);
        simpleMail.setTo(emailTo);
        simpleMail.setText(body);
        mailSender.send(simpleMail);
        log.info("Email sent via SMTP to {} with subject '{}' from sender '{}'", emailTo, subject, defaultSenderName);
    }
}
