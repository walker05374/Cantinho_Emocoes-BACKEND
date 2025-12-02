package com.cantinho_emocoes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class GmailEmailService {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Pega o email "de" a partir do application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetLink) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            // Use MimeMessageHelper para criar e-mails com HTML, anexos, etc.
            // O "true" indica que o conteúdo é HTML.
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Construa o corpo do email HTML
            String htmlContent = "Olá " + userName + ",<br><br>" +
                                 "Você solicitou a redefinição de sua senha. " +
                                 "Clique no link a seguir para redefinir:<br>" +
                                 "<a href=\"" + resetLink + "\">Redefinir Senha</a><br><br>" +
                                 "Se você não solicitou isso, por favor, ignore este e-mail.";

            helper.setText(htmlContent, true); // true = é HTML
            helper.setTo(toEmail);
            helper.setSubject("Redefina sua Senha");
            helper.setFrom(fromEmail); // O remetente é o mesmo da conta configurada

            mailSender.send(mimeMessage);
            logger.info("E-mail de redefinição de senha enviado para {} com sucesso!", toEmail);
            return true;

        } catch (MessagingException e) {
            logger.error("Falha ao enviar e-mail para {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}