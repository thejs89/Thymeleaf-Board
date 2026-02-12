package com.board.thymeleaf.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.thymeleaf.domain.Member;
import com.board.thymeleaf.repository.MemberRepo;
import com.board.thymeleaf.service.ifc.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 
 * 데이터베이스에서 회원 정보를 조회하고 로그인 인증을 처리합니다.
 */
@Slf4j
@Transactional(transactionManager = "boardTxManager", readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  // ========== 로그 메시지 상수 ==========
  
  private static final String LOG_MEMBER_NOT_FOUND = "회원을 찾을 수 없습니다: memberId={}";
  private static final String LOG_LOGIN_SUCCESS = "로그인 성공: memberId={}";
  private static final String LOG_LOGIN_FAILURE = "로그인 실패: memberId={}";

  private final MemberRepo memberRepo;

  /**
   * 회원 ID로 회원 정보를 조회합니다.
   * 
   * 처리 과정:
   * 1. 입력된 아이디가 유효한지 확인 (null이 아니고 공백이 아닌지)
   * 2. 데이터베이스에서 회원 정보 조회
   * 3. 조회 결과 반환
   * 
   * @param memberId 조회할 회원 ID
   * @return 회원 정보 (없으면 null)
   * @throws Exception 조회 중 발생할 수 있는 예외
   */
  @Override
  public Member findByMemberId(String memberId) throws Exception {
    // 1단계: 입력값 검증
    if (!isValidMemberId(memberId)) {
      log.warn(LOG_MEMBER_NOT_FOUND, memberId);
      return null;
    }
    
    // 2단계: 아이디 앞뒤 공백 제거
    String trimmedMemberId = memberId.trim();
    
    // 3단계: 데이터베이스에서 회원 정보 조회
    Member member = memberRepo.findByMemberId(trimmedMemberId);
    
    // 4단계: 조회 결과가 없으면 로그 기록
    if (member == null) {
      log.warn(LOG_MEMBER_NOT_FOUND, trimmedMemberId);
    }
    
    return member;
  }

  /**
   * 아이디와 비밀번호로 로그인 인증을 처리합니다.
   * 
   * 처리 과정:
   * 1. 입력된 아이디와 비밀번호가 유효한지 확인
   * 2. 데이터베이스에서 해당 아이디와 비밀번호를 가진 회원 조회
   * 3. 조회된 회원이 있으면 로그인 성공, 없으면 실패
   * 
   * @param memberId 사용자가 입력한 아이디
   * @param password 사용자가 입력한 비밀번호
   * @return 인증 성공 시 회원 정보, 실패 시 null
   * @throws Exception 인증 처리 중 발생할 수 있는 예외
   */
  @Override
  public Member login(String memberId, String password) throws Exception {
    // 1단계: 입력값 검증
    if (!isValidMemberId(memberId)) {
      log.warn(LOG_LOGIN_FAILURE, memberId);
      return null;
    }
    
    if (!isValidPassword(password)) {
      log.warn(LOG_LOGIN_FAILURE, memberId);
      return null;
    }
    
    // 2단계: 아이디와 비밀번호 앞뒤 공백 제거
    String trimmedMemberId = memberId.trim();
    String trimmedPassword = password.trim();
    
    // 3단계: 데이터베이스에서 아이디와 비밀번호로 회원 조회
    Member member = memberRepo.findByMemberIdAndPassword(trimmedMemberId, trimmedPassword);
    
    // 4단계: 조회 결과에 따라 로그 기록
    if (member != null) {
      log.info(LOG_LOGIN_SUCCESS, trimmedMemberId);
    } else {
      log.warn(LOG_LOGIN_FAILURE, trimmedMemberId);
    }
    
    return member;
  }

  // ========== private 메서드 ==========

  /**
   * 입력된 아이디가 유효한지 확인합니다.
   * 
   * @param memberId 확인할 아이디
   * @return null이 아니고 공백이 아니면 true, 아니면 false
   */
  private boolean isValidMemberId(String memberId) {
    return memberId != null && !memberId.trim().isEmpty();
  }

  /**
   * 입력된 비밀번호가 유효한지 확인합니다.
   * 
   * @param password 확인할 비밀번호
   * @return null이 아니고 공백이 아니면 true, 아니면 false
   */
  private boolean isValidPassword(String password) {
    return password != null && !password.trim().isEmpty();
  }
}

