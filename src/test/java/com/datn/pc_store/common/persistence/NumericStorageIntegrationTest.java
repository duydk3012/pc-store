package com.datn.pc_store.common.persistence;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import com.datn.pc_store.modules.auth.entity.AccountChallenge;
import com.datn.pc_store.modules.auth.entity.RefreshToken;
import com.datn.pc_store.modules.auth.repository.AccountChallengeRepository;
import com.datn.pc_store.modules.auth.repository.RefreshTokenRepository;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.RoleType;
import com.datn.pc_store.modules.user.enums.UserStatus;
import com.datn.pc_store.modules.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:numeric-storage-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "app.admin.password=",
        "app.jwt.secret=0123456789012345678901234567890123456789012345678901234567890123"
})
@Transactional
class NumericStorageIntegrationTest {
    @Autowired EntityManager entityManager;
    @Autowired UserRepository users;
    @Autowired AccountChallengeRepository challenges;
    @Autowired RefreshTokenRepository tokens;

    @Test
    void storesNumericRoleStatusAndFlagsAndRestoresDomainTypes() {
        for (RoleType role : RoleType.values()) {
            User user = users.saveAndFlush(User.builder()
                    .username("numeric_" + role.name()).email(role.name() + "@numeric.test")
                    .fullName("Numeric storage").passwordHash("unused")
                    .role(role).status(UserStatus.ACTIVE).emailVerified(true).build());
            Object[] row = (Object[]) entityManager.createNativeQuery(
                    "SELECT role, status, email_verified, is_deleted FROM users WHERE id = :id")
                    .setParameter("id", user.getId()).getSingleResult();
            assertThat(((Number) row[0]).intValue()).isEqualTo(role.getValue());
            assertThat(((Number) row[1]).intValue()).isEqualTo(1);
            assertThat(((Number) row[2]).intValue()).isEqualTo(1);
            assertThat(((Number) row[3]).intValue()).isZero();
            entityManager.clear();
            User loaded = users.findById(user.getId()).orElseThrow();
            assertThat(loaded.getRole()).isEqualTo(role);
            assertThat(loaded.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(loaded.isEmailVerified()).isTrue();
            assertThat(users.searchUsers(null, role, UserStatus.ACTIVE,
                    org.springframework.data.domain.Pageable.unpaged()).getContent())
                    .extracting(User::getId).contains(user.getId());
        }
    }

    @Test
    void convertsPurposeQueryParametersAndBooleanBulkUpdates() {
        User user = users.saveAndFlush(User.builder().username("numeric_otp")
                .email("otp@numeric.test").fullName("OTP").passwordHash("unused")
                .role(RoleType.CUSTOMER).status(UserStatus.ACTIVE).build());
        AccountChallengePurpose[] purposes = {AccountChallengePurpose.VERIFY_EMAIL, AccountChallengePurpose.RESET_PASSWORD, AccountChallengePurpose.CHANGE_PASSWORD};
        for (int code = 0; code < purposes.length; code++) {
            AccountChallenge challenge = new AccountChallenge();
            challenge.setUserId(user.getId());
            challenge.setPurpose(purposes[code]);
            challenge.setDestination(user.getEmail());
            challenge.setCodeHash("unused");
            challenge.setCreatedAt(Instant.now());
            challenge.setExpiresAt(Instant.now().plusSeconds(300));
            challenges.saveAndFlush(challenge);
            Object[] row = (Object[]) entityManager.createNativeQuery(
                    "SELECT purpose, consumed FROM account_challenges WHERE id = :id")
                    .setParameter("id", challenge.getId()).getSingleResult();
            assertThat(((Number) row[0]).intValue()).isEqualTo(code);
            assertThat(((Number) row[1]).intValue()).isZero();
            entityManager.clear();
            assertThat(challenges.findByUserIdAndPurpose(user.getId(), purposes[code]))
                    .isPresent().get().extracting(AccountChallenge::getPurpose).isEqualTo(purposes[code]);
        }
        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setTokenHash("numeric-refresh-token");
        token.setExpiresAt(Instant.now().plusSeconds(300));
        tokens.saveAndFlush(token);
        assertThat(tokens.revokeAllByUserId(user.getId())).isEqualTo(1);
        entityManager.clear();
        assertThat(tokens.findById(token.getId()).orElseThrow().isRevoked()).isTrue();
        assertThat(tokens.revokeAllByUserId(user.getId())).isZero();
    }

    @Test
    void rejectsInvalidFlagCodesInsteadOfTreatingEveryNonzeroValueAsTrue() {
        assertThatThrownBy(() -> new TinyIntBooleanConverter().convertToEntityAttribute((byte) 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
