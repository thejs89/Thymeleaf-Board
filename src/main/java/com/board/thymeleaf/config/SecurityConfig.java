package com.board.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationProvider customAuthenticationProvider;

  /**
   * AuthenticationManager 설정
   * 
   * 커스텀 AuthenticationProvider를 사용하도록 설정합니다.
   */
  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = 
        http.getSharedObject(AuthenticationManagerBuilder.class);
    
    // 커스텀 AuthenticationProvider 등록
    authenticationManagerBuilder
        .authenticationProvider(customAuthenticationProvider);
    
    return authenticationManagerBuilder.build();
  }

  /**
   * SecurityFilterChain 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // AuthenticationManager 설정
        .authenticationManager(authenticationManager(http))
        
        // CSRF 설정
        .csrf(csrf -> csrf.disable())
        
        // 요청별 권한 설정
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스는 모두 허용
            .antMatchers("/css/**", "/js/**", "/img/**", "/static/**").permitAll()
            // 로그인 페이지는 모두 허용
            .antMatchers("/login", "/login/**").permitAll()
            // 비밀번호 재설정 관련 페이지는 모두 허용
            .antMatchers("/password/**").permitAll()
            // 나머지 모든 요청은 인증 필요
            .anyRequest().authenticated()
        )
        
        // 폼 기반 로그인 설정
        .formLogin(form -> form
            // 로그인 페이지 URL
            .loginPage("/login")
            // 로그인 처리 URL
            .loginProcessingUrl("/login")
            // 로그인 성공 시 이동할 URL
            .defaultSuccessUrl("/board/list", true)
            // 로그인 실패 시 이동할 URL
            .failureUrl("/login?error=true")
            // 로그인 페이지에서 사용할 사용자명 파라미터명
            .usernameParameter("username")
            // 로그인 페이지에서 사용할 비밀번호 파라미터명
            .passwordParameter("password")
            // 모든 사용자가 로그인 페이지 접근 가능
            .permitAll()
        )
        
        // 로그아웃 설정
        .logout(logout -> logout
            // 로그아웃 URL
            .logoutUrl("/logout")
            // 로그아웃 성공 시 이동할 URL
            .logoutSuccessUrl("/board/list")
            // 세션 무효화
            .invalidateHttpSession(true)
            // 쿠키 삭제
            .deleteCookies("JSESSIONID")
            // 모든 사용자가 로그아웃 가능
            .permitAll()
        );

    return http.build();
  }

  /**
   * PasswordEncoder 빈 등록
   * 
   * 비밀번호 암호화를 위한 BCryptPasswordEncoder를 사용합니다.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

