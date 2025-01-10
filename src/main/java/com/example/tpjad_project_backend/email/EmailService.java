package com.example.tpjad_project_backend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendReservationNotification(String to, EmailDto emailDto) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());


            Context context = new Context();
            context.setVariable("emailDto", emailDto);

            String emailContent = templateEngine.process("exam-template", context);

            helper.setTo(to);
            helper.setSubject("Reservation confirmation");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException ignored) {
        }
    }

    @Async
    public void sendReservationReminder(String to, EmailDto emailDto) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());


            Context context = new Context();
            context.setVariable("emailDto", emailDto);

            String emailContent = templateEngine.process("reminder-template", context);

            helper.setTo(to);
            helper.setSubject("Reservation reminder");
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException ignored) {
        }
    }
}
