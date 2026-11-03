package com.datn.pc_store.modules.user.repository;

import com.datn.pc_store.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.datn.pc_store.modules.user.enums.RoleType;
import com.datn.pc_store.modules.user.enums.UserStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findLockedById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
        SELECT u FROM User u
        WHERE (:status IS NULL OR u.status = :status)
          AND (:role IS NULL OR u.role = :role)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR u.phoneNumber LIKE CONCAT('%', :keyword, '%'))
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") RoleType role,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
