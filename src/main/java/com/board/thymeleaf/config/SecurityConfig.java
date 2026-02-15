package com.board.thymeleaf.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomAuthenticationProvider customAuthenticationProvider;

  /**
   * 생성자
   * @Lazy를 사용하여 순환 참조 문제 해결
   */
  public SecurityConfig(@Lazy CustomAuthenticationProvider customAuthenticationProvider) {
    this.customAuthenticationProvider = customAuthenticationProvider;
  }

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

               // 요청별 권한 설정 (RoleHierarchy 적용)
               .authorizeRequests(requests -> requests
                      // 정적 리소스는 모두 허용
                      .antMatchers("/css/**", "/js/**", "/img/**", "/static/**").permitAll()
                      // 로그인 페이지는 모두 허용
                      .antMatchers("/login", "/login/**").permitAll()
                      // 비밀번호 재설정 관련 페이지는 USER 권한 필요 (RoleHierarchy로 ADMIN도 접근 가능)
                      .antMatchers("/password/**").hasRole("USER")
                      // 에러 페이지는 모두 허용
                      .antMatchers("/error/**").permitAll()
                      // 게시판 관련 페이지는 관리자 권한 필요
                      .antMatchers("/board/**").hasRole("ADMIN")
                      // 나머지 모든 요청은 인증 필요
                      .anyRequest().authenticated())

              // 접근 거부 핸들러 설정
              .exceptionHandling(exception -> exception
                              .accessDeniedHandler(customAccessDeniedHandler())
              )

              // 폼 기반 로그인 설정
              .formLogin(form -> form
                              // 로그인 페이지 URL
                              .loginPage("/login")
                              // 로그인 처리 URL
                              .loginProcessingUrl("/login")
                              // 로그인 성공 시 이동할 URL
                              //.defaultSuccessUrl("/board/list", true)
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
                              //.logoutSuccessUrl("/board/list")
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
   * RoleHierarchy 빈 등록
   * 
   * 권한 계층 구조를 설정합니다.
   * ROLE_ADMIN > ROLE_USER: ADMIN 권한을 가진 사용자는 USER 권한도 자동으로 가집니다.
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    return roleHierarchy;
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

  /**
   * 접근 거부 핸들러
   * 
   * 권한이 없는 사용자가 접근 시 에러 페이지로 리다이렉트합니다.
   */
  @Bean
  public AccessDeniedHandler customAccessDeniedHandler() {
    return new AccessDeniedHandler() {
      @Override
      public void handle(HttpServletRequest request, HttpServletResponse response,
          AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증되지 않은 사용자인 경우 로그인 페이지로 리다이렉트
        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
          response.sendRedirect("/login?error=unauthorized");
          return;
        }
        
        // 인증되었지만 권한이 없는 경우 에러 페이지로 리다이렉트
        response.sendRedirect("/error/403");
      }
    };
  }
}

