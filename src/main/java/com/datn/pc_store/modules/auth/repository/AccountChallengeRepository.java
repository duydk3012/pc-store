package com.datn.pc_store.modules.auth.repository;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import com.datn.pc_store.modules.auth.entity.AccountChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountChallengeRepository extends JpaRepository<AccountChallenge, Long> {
    Optional<AccountChallenge> findByUserIdAndPurpose(Long userId, AccountChallengePurpose purpose);
    java.util.List<AccountChallenge> findByUserId(Long userId);
}
