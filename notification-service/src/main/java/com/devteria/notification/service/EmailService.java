package com.devteria.notification.service;

import com.devteria.notification.dto.request.EmailRequest;
import com.devteria.notification.dto.request.Recipient;
import com.devteria.notification.dto.request.SendEmailRequest;
import com.devteria.notification.dto.request.Sender;
import com.devteria.notification.dto.response.EmailResponse;
import com.devteria.notification.exception.AppException;
import com.devteria.notification.exception.ErrorCode;
import com.devteria.notification.repository.http_client.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    EmailClient emailClient;

    @NonFinal
    @Value("${brevo.api-key}")
    String apiKey;

    @NonFinal
    @Value("${brevo.sender}")
    String sender;

    public EmailResponse sendEmail(SendEmailRequest sendEmailRequest) {
        EmailRequest emailRequest = EmailRequest.builder()
                .subject(sendEmailRequest.getSubject())
                .sender(
                        Sender.builder()
                                .name("Marco Pham")
                                .email(sender)
                                .build()
                )
                .to(List.of(sendEmailRequest.getTo()))
                .htmlContent(sendEmailRequest.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException ex) {
            log.error("exception when send mail", ex);
            throw new AppException(ErrorCode.CANNOT_SEND_MAIL);
        }
    }

}
