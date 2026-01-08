package com.board.thymeleaf.service.ifc;

import java.util.Map;

public interface PasswordResetService {
  
  /**
   * 비밀번호 재설정 메일 발송
   * @param map email, accessIp 포함
   * @return 발송 성공 여부
   * @throws Exception
   */
  Boolean sendPasswordResetMail(Map<String, Object> map) throws Exception;
  
  /**
   * 비밀번호 재설정 토큰 검증
   * @param map token 포함
   * @return 검증 결과 및 이메일 정보
   * @throws Exception
   */
  Map<String, Object> verifyPasswordResetToken(Map<String, Object> map) throws Exception;
  
  /**
   * 비밀번호 재설정 완료 처리
   * @param map token, newPassword 포함
   * @return 처리 성공 여부
   * @throws Exception
   */
  Boolean resetPassword(Map<String, Object> map) throws Exception;
  
}

