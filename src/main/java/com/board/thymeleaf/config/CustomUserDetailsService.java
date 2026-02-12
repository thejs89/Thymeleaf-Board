package com.board.thymeleaf.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.board.thymeleaf.domain.Member;
import com.board.thymeleaf.service.ifc.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security의 UserDetailsService 구현 클래스
 * 
 * 기존 MemberService를 활용하여 사용자 인증 정보를 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberService memberService;

  /**
   * 사용자명(아이디)으로 사용자 정보를 조회합니다.
   * 
   * @param username 사용자명 (memberId)
   * @return UserDetails 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      // 기존 MemberService를 통해 회원 정보 조회
      Member member = memberService.findByMemberId(username);
      
      if (member == null) {
        log.warn("사용자를 찾을 수 없습니다: memberId={}", username);
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
      }
      
      // 삭제된 회원인지 확인
      if (member.isDeleteYn()) {
        log.warn("삭제된 회원입니다: memberId={}", username);
        throw new UsernameNotFoundException("삭제된 회원입니다: " + username);
      }
      
      // UserDetails 객체 생성 및 반환
      return new CustomUserDetails(member);
      
    } catch (Exception e) {
      log.error("사용자 조회 중 오류 발생: memberId={}", username, e);
      throw new UsernameNotFoundException("사용자 조회 중 오류가 발생했습니다: " + username, e);
    }
  }
}

