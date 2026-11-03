package com.datn.pc_store.modules.auth;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import com.datn.pc_store.modules.auth.repository.AccountChallengeRepository;
import com.datn.pc_store.modules.auth.service.*;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.*;
import com.datn.pc_store.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:verification-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
    "app.admin.password=",
    "app.jwt.secret=0123456789012345678901234567890123456789012345678901234567890123"
})
@Transactional
class AccountVerificationIntegrationTest {
    @Autowired AccountVerificationService service;
    @Autowired UserRepository users;
    @Autowired AccountChallengeRepository challenges;
    @Autowired PasswordEncoder encoder;
    @Autowired RefreshTokenService refreshTokens;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired org.springframework.web.context.WebApplicationContext context;
    @Autowired com.datn.pc_store.modules.auth.security.JwtService jwt;
    @MockitoBean ChallengeDeliveryService delivery;
    User user;
    AtomicReference<String> code = new AtomicReference<>();

    @BeforeEach void setup() {
        user = users.save(User.builder().username("verifycustomer").email("verify@example.com")
            .phoneNumber("0912345678").passwordHash(encoder.encode("OldPassword123!"))
            .role(RoleType.CUSTOMER).status(UserStatus.ACTIVE).build());
        doAnswer(invocation -> { code.set(invocation.getArgument(1)); return null; })
            .when(delivery).send(anyString(), anyString(), any(AccountChallengePurpose.class));
    }

    @Test void verifiesEmailAndRejectsReuse() {
        service.requestEmailVerification(user.getId());
        String otp = code.get();
        assertThat(challenges.findByUserIdAndPurpose(user.getId(), AccountChallengePurpose.VERIFY_EMAIL).orElseThrow().getCodeHash()).doesNotContain(otp);
        service.verifyEmail(user.getId(), otp);
        assertThat(user.isEmailVerified()).isTrue();
        assertThatThrownBy(() -> service.verifyEmail(user.getId(), otp)).isInstanceOf(ResponseStatusException.class);
    }

    @Test void resetViaEmailRevokesSessionsAndInvalidatesCode() {
        String refresh = refreshTokens.issue(user.getId());
        service.requestPasswordReset("VERIFY@example.com");
        service.resetPassword(user.getEmail(), code.get(), "NewPassword123!");
        assertThat(encoder.matches("NewPassword123!", user.getPasswordHash())).isTrue();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        entityManager.flush();
        entityManager.clear();
        assertThat(refreshTokens.findByRaw(refresh).orElseThrow().isRevoked()).isTrue();
        assertThatThrownBy(() -> service.resetPassword(user.getEmail(), code.get(), "OtherPassword123!"))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test void apiRejectsPhoneNumbersForAllPasswordFlows() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        String token = jwt.generateToken(user);
        for (String phone : new String[]{"0912345678", "+84912345678"}) {
            for (String endpoint : new String[]{"forgot-password", "change-password/request", "reset-password", "change-password"}) {
                boolean confirm = endpoint.equals("reset-password") || endpoint.equals("change-password");
                String account = "{\"identifier\":\"" + phone + "\"}";
                String body = confirm ? "{\"account\":" + account
                        + ",\"code\":\"123456\",\"newPassword\":\"NewPassword123!\""
                        + (endpoint.equals("change-password") ? ",\"currentPassword\":\"OldPassword123!\"" : "") + "}" : account;
                mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/" + endpoint)
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
            }
        }
        verifyNoInteractions(delivery);
    }

    @Test void wrongAttemptsPersistAndLockCode() {
        service.requestEmailVerification(user.getId());
        String wrong = code.get().equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.verifyEmail(user.getId(), wrong)).isInstanceOf(ResponseStatusException.class);
        }
        assertThat(challenges.findByUserIdAndPurpose(user.getId(), AccountChallengePurpose.VERIFY_EMAIL).orElseThrow().getAttempts()).isEqualTo(5);
        assertThatThrownBy(() -> service.verifyEmail(user.getId(), code.get())).isInstanceOf(ResponseStatusException.class);
    }

    @Test void rejectsExpiredCodesAndThrottlesResend() {
        service.requestEmailVerification(user.getId());
        assertThatThrownBy(() -> service.requestEmailVerification(user.getId()))
            .isInstanceOf(ResponseStatusException.class);
        challenges.findByUserIdAndPurpose(user.getId(), AccountChallengePurpose.VERIFY_EMAIL).orElseThrow().setExpiresAt(Instant.now().minusSeconds(1));
        assertThatThrownBy(() -> service.verifyEmail(user.getId(), code.get())).isInstanceOf(ResponseStatusException.class);
    }

    @Test void changeRequiresOwnContactAndCurrentPassword() {
        assertThatThrownBy(() -> service.requestPasswordChange(user.getId(), "other@example.com"))
            .isInstanceOf(ResponseStatusException.class);
        service.requestPasswordChange(user.getId(), user.getEmail());
        service.changePassword(user.getId(), user.getEmail(), "OldPassword123!", code.get(), "NewPassword123!");
        assertThat(user.getTokenVersion()).isEqualTo(1);
    }

    @Test void unknownEmailNeverReceivesCode() {
        service.requestPasswordReset("unknown@example.com");
        verifyNoInteractions(delivery);
    }

    @Test void apiRequiresAuthenticationAndResetInvalidatesAccessToken() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/email-verification/request"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        String token = jwt.generateToken(user);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.emailVerified").value(false));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/forgot-password")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"verify@example.com\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isAccepted());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/reset-password")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"account\":{\"identifier\":\"verify@example.com\"},\"code\":\"" + code.get()
                        + "\",\"newPassword\":\"NewPassword123!\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
}
