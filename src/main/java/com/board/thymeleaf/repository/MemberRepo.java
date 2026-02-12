package com.board.thymeleaf.repository;

import org.springframework.stereotype.Repository;

import com.board.thymeleaf.config.BoardMapper;
import com.board.thymeleaf.domain.Member;

/**
 * 회원 정보를 데이터베이스에서 조회하는 Repository 인터페이스
 * 
 * MyBatis를 사용하여 SQL 쿼리를 실행합니다.
 */
@BoardMapper
@Repository
public interface MemberRepo {

  /**
   * 회원 ID로 회원 정보를 조회합니다.
   * 
   * 삭제되지 않은 회원만 조회합니다.
   * 
   * @param memberId 조회할 회원 ID
   * @return 회원 정보 (없으면 null)
   */
  Member findByMemberId(String memberId);

  /**
   * 회원 ID와 비밀번호로 회원 정보를 조회합니다.
   * 
   * 로그인 인증에 사용됩니다.
   * 아이디와 비밀번호가 모두 일치하고, 삭제되지 않은 회원만 조회합니다.
   * 
   * @param memberId 회원 ID
   * @param password 비밀번호
   * @return 회원 정보 (없으면 null)
   */
  Member findByMemberIdAndPassword(String memberId, String password);
}

