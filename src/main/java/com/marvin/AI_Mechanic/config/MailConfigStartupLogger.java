package com.marvin.AI_Mechanic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MailConfigStartupLogger implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MailConfigStartupLogger.class);

    private final Environment environment;

    @Value("${app.debug.log-mail-password-plain:false}")
    private boolean logPlainPassword;

    public MailConfigStartupLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String host = environment.getProperty("spring.mail.host", "");
        String port = environment.getProperty("spring.mail.port", "");
        String username = environment.getProperty("spring.mail.username", "");
        String password = environment.getProperty("spring.mail.password", "");

        String passwordForLog = logPlainPassword ? password : maskPassword(password);

        logger.info(
                "Resolved mail config -> spring.mail.host='{}', spring.mail.port='{}', spring.mail.username='{}', spring.mail.password='{}'",
                blankToLabel(host),
                blankToLabel(port),
                blankToLabel(username),
                blankToLabel(passwordForLog)
        );
    }

    private String maskPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return "(empty)";
        }
        if (rawPassword.length() <= 2) {
            return "**";
        }
        return "*".repeat(rawPassword.length() - 2) + rawPassword.substring(rawPassword.length() - 2);
    }

    private String blankToLabel(String value) {
        return (value == null || value.isBlank()) ? "(empty)" : value;
    }
}
