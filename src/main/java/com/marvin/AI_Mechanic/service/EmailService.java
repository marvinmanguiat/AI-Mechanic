package com.marvin.AI_Mechanic.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;


@Service
public class EmailService {

	  @Autowired
    JavaMailSender mailSender;
  
    public void sendEmailOtp(String emailTo, String emailFrom, String subject, String body) {
        SimpleMailMessage simpleMail = new SimpleMailMessage(); 
        simpleMail.setFrom(emailFrom);
        simpleMail.setSubject(subject);
        simpleMail.setTo(emailTo);
        simpleMail.setText(body);
        mailSender.send(simpleMail);
    }
}
