package com.example.pfebtk.auth.service.email;

import com.example.pfebtk.echeancier.entity.Echeancier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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



    public void sendRetardEcheancierGrouped(String toEmail, List<Echeancier> echeances) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Notification de retard de paiement");

        StringBuilder body = new StringBuilder();

        body.append("Bonjour,\n\n")
                .append("Nous vous informons que vous avez des échéances en retard :\n\n");

        for (Echeancier e : echeances) {
            body.append("- Date d’échéance : ")
                    .append(e.getDateEcheance())
                    .append("\n");
        }

        body.append("\nMerci de procéder à la régularisation dans les plus brefs délais.\n\n")
                .append("Cordialement,\n")
                .append("Service Amicale BTK");

        message.setText(body.toString());

        mailSender.send(message);
    }

}

