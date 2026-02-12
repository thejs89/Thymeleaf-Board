package com.board.thymeleaf.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 커스텀 AuthenticationProvider
 * 
 * 사용자 인증 및 비밀번호 비교를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  /**
   * 인증 처리
   * 
   * 1. UserDetailsService를 통해 사용자 정보 조회
   * 2. 입력된 비밀번호와 저장된 비밀번호 비교
   * 3. 인증 성공 시 Authentication 객체 반환
   * 
   * @param authentication 인증 요청 정보 (username, password 포함)
   * @return 인증 성공 시 Authentication 객체
   * @throws AuthenticationException 인증 실패 시 예외 발생
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    log.debug("인증 시도: username={}", username);

    try {
      // 1. UserDetailsService를 통해 사용자 정보 조회
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (userDetails == null) {
        log.warn("사용자를 찾을 수 없습니다: username={}", username);
        throw new BadCredentialsException("사용자를 찾을 수 없습니다.");
      }

      // 2. 비밀번호 비교
      if (!passwordEncoder.matches(password, userDetails.getPassword())) {
        log.warn("비밀번호가 일치하지 않습니다: username={}", username);
        throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
      }

      // 3. 계정 활성화 여부 확인
      if (!userDetails.isEnabled()) {
        log.warn("비활성화된 계정입니다: username={}", username);
        throw new BadCredentialsException("비활성화된 계정입니다.");
      }

      // 4. 인증 성공 - Authentication 객체 생성 및 반환
      log.info("인증 성공: username={}", username);
      return new UsernamePasswordAuthenticationToken(
          userDetails,
          password,
          userDetails.getAuthorities()
      );

    } catch (BadCredentialsException e) {
      throw e;
    } catch (Exception e) {
      log.error("인증 처리 중 오류 발생: username={}", username, e);
      throw new BadCredentialsException("인증 처리 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 이 Provider가 처리할 수 있는 Authentication 타입인지 확인
   * 
   * @param authentication Authentication 타입
   * @return UsernamePasswordAuthenticationToken이면 true
   */
  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}


