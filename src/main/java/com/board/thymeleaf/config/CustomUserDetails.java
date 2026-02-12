package com.board.thymeleaf.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.board.thymeleaf.domain.Member;

import lombok.Getter;

/**
 * UserDetails 구현 클래스
 * 
 * Member 도메인을 Spring Security의 UserDetails로 변환합니다.
 */
@Getter
public class CustomUserDetails implements UserDetails {
  
  private final Member member;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  /**
   * 사용자의 권한 목록을 반환합니다.
   * 
   * Member의 role 필드에서 권한을 읽어옵니다.
   * - role이 있으면 파싱하여 권한 목록 생성
   * - role이 없거나 비어있으면 기본값 ROLE_USER 사용
   * - 여러 권한은 콤마로 구분 (예: "ROLE_USER,ROLE_ADMIN")
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    
    String role = member.getRole();
    
    // role이 없거나 비어있으면 기본값 ROLE_USER 사용
    if (role == null || role.trim().isEmpty()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
      return authorities;
    }
    
    // 콤마로 구분된 권한들을 파싱
    String[] roles = role.split(",");
    for (String r : roles) {
      String trimmedRole = r.trim();
      if (!trimmedRole.isEmpty()) {
        // ROLE_ 접두사가 없으면 자동으로 추가
        if (!trimmedRole.startsWith("ROLE_")) {
          trimmedRole = "ROLE_" + trimmedRole;
        }
        authorities.add(new SimpleGrantedAuthority(trimmedRole));
      }
    }
    
    // 권한이 하나도 없으면 기본값 ROLE_USER 사용
    if (authorities.isEmpty()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    return authorities;
  }

  /**
   * 사용자의 비밀번호를 반환합니다.
   */
  @Override
  public String getPassword() {
    return member.getPassword();
  }

  /**
   * 사용자명(아이디)을 반환합니다.
   */
  @Override
  public String getUsername() {
    return member.getMemberId();
  }

  /**
   * 계정이 만료되지 않았는지 확인합니다.
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * 계정이 잠겨있지 않은지 확인합니다.
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * 자격 증명이 만료되지 않았는지 확인합니다.
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * 계정이 활성화되어 있는지 확인합니다.
   */
  @Override
  public boolean isEnabled() {
    return !member.isDeleteYn();
  }
}

