package com.datn.pc_store.modules.auth.service;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ChallengeDeliveryService {
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    public ChallengeDeliveryService(ObjectProvider<JavaMailSender> mail,
            @Value("${app.verification.mail-from:}") String from) {
        this.mail = mail;
        this.from = from;
    }

    public void send(String destination, String code, AccountChallengePurpose purpose) {
        String message = "PC Store - " + purpose + ": " + code + ". Ma co hieu luc 5 phut. Khong chia se ma nay.";
        try {
            JavaMailSender sender = mail.getIfAvailable();
            if (sender == null || from.isBlank()) throw new IllegalStateException();
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(from);
            email.setTo(destination);
            email.setSubject("PC Store - Ma xac thuc");
            email.setText(message);
            sender.send(email);
        } catch (Exception ex) {
            // Do not expose provider errors or OTP values in API responses/logs.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi mã xác thực, vui lòng thử lại sau");
        }
    }
}
