package com.datn.pc_store.modules.auth.security;

import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public boolean isEmailVerified() {
        return user.isEmailVerified();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public int getTokenVersion() {
        return user.getTokenVersion();
    }

    /**
     * BANNED -> LockedException khi đăng nhập
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BANNED;
    }
}
