package com.datn.pc_store.modules.auth.entity;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurpose;

import com.datn.pc_store.modules.auth.enums.AccountChallengePurposeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "account_challenges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "purpose"}))
@Getter
@Setter
public class AccountChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Convert(converter = AccountChallengePurposeConverter.class)
    @Column(nullable = false)
    private AccountChallengePurpose purpose;
    @Column(nullable = false)
    private String destination;
    @Column(name = "code_hash", nullable = false)
    private String codeHash;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    private int attempts;
    private boolean consumed;
}
