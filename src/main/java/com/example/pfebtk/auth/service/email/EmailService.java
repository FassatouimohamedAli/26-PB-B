package com.example.pfebtk.auth.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendCredentials(String toEmail, String unix, String password) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Votre compte a été créé");

        message.setText(
                "Bonjour,\n\n" +
                        "Votre compte a été créé avec succès.\n\n" +
                        "Username : " + unix + "\n" +
                        "Mot de passe : " + password + "\n\n" +
                        "Cordialement."
        );

        mailSender.send(message);
    }


    public void sendCredentials_2(String toEmail, String password) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Votre compte a été créé");

        message.setText(
                "Bonjour,\n\n" +
                        "Votre password a été regenere avec succès.\n\n" +
                        "Mot de passe : " + password + "\n\n" +
                        "Cordialement."
        );

        mailSender.send(message);
    }
}

