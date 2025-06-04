package korn03.tradeguardserver.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@AllArgsConstructor
@EnableConfigurationProperties(EmailConfigProperties.class)
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailConfigProperties emailConfigProperties;
    private final SpringTemplateEngine templateEngine;
    //private final JavaMailSenderImpl mailSender;

    @Async
    public void sendEmail(String toMail, String subject, String messageBody) {
        final var simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(emailConfigProperties.getUsername());
        simpleMailMessage.setTo(toMail);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(messageBody);
        javaMailSender.send(simpleMailMessage);
    }

    //@Async
    public void sendOtpTemplateEmail(String to, String name, int otp, int expirationMinutes, String deepLinkUrl) {
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("name", name);
        context.setVariable("expirationMinutes", expirationMinutes);
        context.setVariable("deepLink", deepLinkUrl);

        String html = templateEngine.process("email/otp-email", context);

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailConfigProperties.getUsername());
            helper.setTo(to);
            helper.setSubject("🔐 Your TradeGuard Verification Code");
            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }


}