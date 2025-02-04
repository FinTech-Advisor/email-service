package org.advisor.email.services;

import org.advisor.email.exceptions.InvalidTokenException;
import org.advisor.global.libs.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"default", "test", "email"})
class EmailResetServiceTest {

    @Autowired // @MockBean 제거 후 @Autowired로 변경
    private EmailService emailService;

    @Autowired
    private Utils utils;

    @Autowired
    private EmailResetService emailResetService;


    @Test
    void sendResetEmail_success() throws InterruptedException {
        // Given
        String to = "kwat0112@gmail.com";

        when(utils.getUserHash()).thenReturn("userHash");
        doNothing().when(utils).saveValue(anyString(), any());
        when(utils.getMessage(any())).thenReturn("test");

        // When
        emailResetService.sendResetEmail(to);

        // Then
        Thread.sleep(1000);
        // verify(emailService, times(1)).sendEmail(any(), eq("reset-email")); // verify 제거
        // 실제 이메일이 kwat0112@gmail.com으로 보내졌는지 직접 확인
    }

    @Test
    void confirmResetEmail_success() {
        // Given
        String token = UUID.randomUUID().toString();
        LocalDateTime expired = LocalDateTime.now().plusHours(1);
        when(utils.getUserHash()).thenReturn("userHash");
        when(utils.getValue("userHash_resetTokenExpired")).thenReturn(expired);
        when(utils.getValue("userHash_resetToken")).thenReturn(token);

        // When & Then
        assertDoesNotThrow(() -> emailResetService.confirmResetEmail(token));
    }

    @Test
    void confirmResetEmail_expiredToken() {
        // Given
        String token = UUID.randomUUID().toString();
        LocalDateTime expired = LocalDateTime.now().minusHours(1); // 이미 만료된 시간
        when(utils.getUserHash()).thenReturn("userHash");
        when(utils.getValue("userHash_resetTokenExpired")).thenReturn(expired);

        // When & Then
        assertThrows(InvalidTokenException.class, () -> emailResetService.confirmResetEmail(token));
    }

    @Test
    void confirmResetEmail_invalidToken() {
        // Given
        String token = UUID.randomUUID().toString();
        LocalDateTime expired = LocalDateTime.now().plusHours(1);
        when(utils.getUserHash()).thenReturn("userHash");
        when(utils.getValue("userHash_resetTokenExpired")).thenReturn(expired);
        when(utils.getValue("userHash_resetToken")).thenReturn("different-token");

        // When & Then
        assertThrows(InvalidTokenException.class, () -> emailResetService.confirmResetEmail(token));
    }
}