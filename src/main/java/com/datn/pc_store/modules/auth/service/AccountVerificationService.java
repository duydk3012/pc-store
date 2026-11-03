package com.datn.pc_store.modules.auth.service;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import com.datn.pc_store.modules.auth.entity.AccountChallenge;
import com.datn.pc_store.modules.auth.repository.AccountChallengeRepository;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.UserStatus;
import com.datn.pc_store.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository users;
    private final AccountChallengeRepository challenges;
    private final PasswordEncoder encoder;
    private final ChallengeDeliveryService delivery;
    private final RefreshTokenService refreshTokens;

    @Transactional
    public void requestEmailVerification(Long userId) {
        User user = lock(userId);
        if (!user.isEmailVerified()) issue(user, AccountChallengePurpose.VERIFY_EMAIL, user.getEmail());
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void verifyEmail(Long userId, String code) {
        User user = lock(userId);
        verify(user, AccountChallengePurpose.VERIFY_EMAIL, user.getEmail(), code);
        user.setEmailVerified(true);
    }

    @Transactional
    public void requestPasswordReset(String identifier) {
        resolve(identifier).ifPresent(found -> {
            User user = lock(found.getId());
            issue(user, AccountChallengePurpose.RESET_PASSWORD, destination(user, identifier));
        });
    }

    @Transactional
    public void requestPasswordChange(Long userId, String identifier) {
        User user = lock(userId);
        issue(user, AccountChallengePurpose.CHANGE_PASSWORD, destination(user, identifier));
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void resetPassword(String identifier, String code, String password) {
        User user = resolve(identifier).map(u -> lock(u.getId())).orElseThrow(AccountVerificationService::invalid);
        verify(user, AccountChallengePurpose.RESET_PASSWORD, destination(user, identifier), code);
        updatePassword(user, password);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void changePassword(Long userId, String identifier, String currentPassword, String code, String password) {
        User user = lock(userId);
        // Verify OTP first so incorrect current-password guesses also consume an attempt/code.
        verify(user, AccountChallengePurpose.CHANGE_PASSWORD, destination(user, identifier), code);
        if (!encoder.matches(currentPassword, user.getPasswordHash())) throw invalid();
        updatePassword(user, password);
    }

    private User lock(Long id) {
        User user = users.findLockedById(id).orElseThrow(AccountVerificationService::invalid);
        if (user.getStatus() != UserStatus.ACTIVE) throw invalid();
        return user;
    }

    private Optional<User> resolve(String identifier) {
        return users.findByEmailIgnoreCase(identifier)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE);
    }

    private String destination(User user, String identifier) {
        if (identifier.equalsIgnoreCase(user.getEmail())) return user.getEmail();
        throw invalid();
    }

    private void issue(User user, AccountChallengePurpose purpose, String destination) {
        Instant now = Instant.now();
        AccountChallenge challenge = challenges.findByUserIdAndPurpose(user.getId(), purpose).orElseGet(AccountChallenge::new);
        if (challenge.getCreatedAt() != null && challenge.getCreatedAt().plusSeconds(60).isAfter(now)) {
            // Same behavior as an unknown account for public reset requests.
            if (purpose == AccountChallengePurpose.RESET_PASSWORD) return;
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng chờ 60 giây trước khi gửi lại");
        }
        String code = String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        challenge.setUserId(user.getId());
        challenge.setPurpose(purpose);
        challenge.setDestination(destination);
        challenge.setCodeHash(encoder.encode(code));
        challenge.setCreatedAt(now);
        challenge.setExpiresAt(now.plusSeconds(300));
        challenge.setAttempts(0);
        challenge.setConsumed(false);
        challenges.save(challenge);
        delivery.send(destination, code, purpose);
    }

    private void verify(User user, AccountChallengePurpose purpose, String destination, String code) {
        AccountChallenge challenge = challenges.findByUserIdAndPurpose(user.getId(), purpose)
                .orElseThrow(AccountVerificationService::invalid);
        if (challenge.isConsumed() || !challenge.getExpiresAt().isAfter(Instant.now())
                || challenge.getAttempts() >= 5 || !challenge.getDestination().equals(destination)) throw invalid();
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!encoder.matches(code, challenge.getCodeHash())) throw invalid();
        challenge.setConsumed(true);
    }

    private void updatePassword(User user, String password) {
        user.setPasswordHash(encoder.encode(password));
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokens.revokeAllByUser(user.getId());
        challenges.findByUserId(user.getId()).forEach(c -> c.setConsumed(true));
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin xác thực hoặc mã OTP không hợp lệ, đã hết hạn hoặc đã sử dụng");
    }
}
