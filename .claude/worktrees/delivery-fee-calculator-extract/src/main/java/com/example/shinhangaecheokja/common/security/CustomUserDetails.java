package com.example.shinhandelivery.common.security;

import com.example.shinhandelivery.member.entity.Member;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Long id;
  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomUserDetails(Member member) {
    this.id = member.getId();
    this.email = member.getEmail();
    this.password = member.getPassword();
    this.authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
  }

  public CustomUserDetails(Long id, String email, String password, String role) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.authorities =
        Collections.singletonList(
            new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
