package com.board.thymeleaf.service.ifc;

import com.board.thymeleaf.domain.Member;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 * 
 * 주요 기능:
 * - 회원 정보 조회
 * - 로그인 인증 처리
 */
public interface MemberService {
  
  /**
   * 회원 ID로 회원 정보를 조회합니다.
   * 
   * @param memberId 조회할 회원 ID
   * @return 회원 정보 (없으면 null)
   * @throws Exception 조회 중 발생할 수 있는 예외
   */
  Member findByMemberId(String memberId) throws Exception;
  
  /**
   * 아이디와 비밀번호로 로그인 인증을 처리합니다.
   * 
   * 데이터베이스에서 해당 아이디와 비밀번호를 가진 회원을 조회합니다.
   * 조회된 회원이 있으면 로그인 성공, 없으면 로그인 실패입니다.
   * 
   * @param memberId 사용자가 입력한 아이디
   * @param password 사용자가 입력한 비밀번호
   * @return 인증 성공 시 회원 정보, 실패 시 null
   * @throws Exception 인증 처리 중 발생할 수 있는 예외
   */
  Member login(String memberId, String password) throws Exception;
}

