package com.outthedoor.withsoil.global.security.entity;

import com.outthedoor.withsoil.api.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final Member member;
    private String password;

    public CustomUserDetails(Member member) {
        this.member = member;
        this.password = member.getPassword();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole().toString()));
    }

    @Override
    public boolean isEnabled() {
        return !member.isDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !member.isDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !member.isDeleted();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !member.isDeleted();
    }
}