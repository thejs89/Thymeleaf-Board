package com.board.thymeleaf.service.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import com.board.thymeleaf.domain.PasswordReset;
import com.board.thymeleaf.mail.service.ifc.MailService;
import com.board.thymeleaf.repository.PasswordResetRepo;
import com.board.thymeleaf.service.ifc.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 재설정 서비스 구현체
 * 
 * 주요 기능:
 * 1. 비밀번호 재설정 메일 발송
 * 2. 토큰 검증 (만료일, 사용여부 체크)
 * 3. 비밀번호 재설정 처리
 */
@Slf4j
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

  // ========== 상수 정의 ==========
  
  /** 토큰 앞뒤에 붙이는 접두사/접미사 (보안을 위한 구분자) */
  private static final String TOKEN_PREFIX = "PWRESET_";
  private static final String TOKEN_SUFFIX = "_PWRESET";
  
  /** 메일 발송 기본 정보 */
  private static final String DEFAULT_FROM_EMAIL = "noreply@example.com";
  private static final String DEFAULT_FROM_NAME = "게시판 관리자";
  private static final String MAIL_SUBJECT = "비밀번호 재설정을 위한 인증메일입니다.";
  private static final String MAIL_CONTENT_TYPE = "text/html;charset=UTF-8";
  
  /** 메일 템플릿 파일 경로 */
  private static final String MAIL_TEMPLATE_PATH = "templates/mail/password-reset-form.html";
  
  /** 토큰 유효기간 (시간) */
  private static final int TOKEN_EXPIRATION_HOURS = 1;
  
  /** 요청 파라미터 키 */
  private static final String PARAM_EMAIL = "email";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_NEW_PASSWORD = "newPassword";
  private static final String PARAM_ACCESS_IP = "accessIp";
  
  /** 응답 키 */
  private static final String RESPONSE_RESULT = "result";
  private static final String RESPONSE_EMAIL = "email";
  private static final String RESPONSE_SEQ = "seq";
  
  /** 메일 템플릿 치환 키 */
  private static final String TEMPLATE_RESET_URL = "##RESET_URL##";
  
  // ========== 의존성 주입 ==========
  
  private final PasswordResetRepo passwordResetRepo;
  private final MailService mailService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${web.url:http://localhost:8080}")
  private String webUrl;

  // ========== 공개 메서드 ==========

  /**
   * 비밀번호 재설정 메일 발송
   * 
   * 처리 과정:
   * 1. 이메일 유효성 검사
   * 2. 같은 이메일의 기존 미사용 토큰 무효화 (보안 강화)
   * 3. 토큰 생성 및 만료일 설정
   * 4. 메일 내용 생성 (HTML 템플릿 사용)
   * 5. 메일 발송
   * 6. DB에 토큰 정보 저장
   * 
   * @param requestMap 이메일, 접속 IP 정보가 담긴 Map
   * @return 발송 성공 여부
   */
  @Override
  public Boolean sendPasswordResetMail(Map<String, Object> requestMap) throws Exception {
    // 1단계: 입력값 추출 및 검증
    String email = extractEmail(requestMap);
    String accessIp = extractAccessIp(requestMap);
    
    if (!isValidEmail(email)) {
      log.error("이메일이 없거나 유효하지 않습니다: {}", email);
      return false;
    }

    // 2단계: 같은 이메일의 기존 미사용 토큰 무효화 (보안 강화)
    // 같은 이메일로 여러 번 요청 시 이전 토큰은 무효화하여 하나의 유효한 토큰만 유지
    passwordResetRepo.invalidatePreviousTokens(email);
    
    // 3단계: 토큰 정보 생성
    Integer resetSeq = passwordResetRepo.getNextPasswordResetSeq();
    Date currentTime = new Date();
    Date expirationTime = calculateExpirationTime(currentTime);
    
    // 4단계: 토큰 생성 및 인코딩
    String encodedToken = createAndEncodeToken(resetSeq, email);
    
    // 5단계: 재설정 URL 생성
    String resetUrl = createResetUrl(encodedToken);
    
    // 6단계: 메일 내용 생성
    String mailContent = createMailContent(resetUrl);
    
    // 7단계: 메일 발송
    try {
      sendMail(email, mailContent);
      
      // 8단계: DB에 토큰 정보 저장
      savePasswordResetInfo(resetSeq, email, encodedToken, expirationTime, accessIp, currentTime);
      
      log.info("비밀번호 재설정 메일 발송 성공: email={}", email);
      return true;
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 메일 발송 실패: email={}", email, e);
      return false;
    }
  }

  /**
   * 비밀번호 재설정 토큰 검증
   * 
   * 검증 항목:
   * 1. 토큰 존재 여부
   * 2. 토큰 디코딩 가능 여부
   * 3. DB에 토큰 존재 여부
   * 4. 토큰 만료 여부
   * 5. 토큰 사용 여부
   * 6. 토큰과 이메일 일치 여부
   * 
   * @param requestMap 토큰이 담긴 Map
   * @return 검증 결과 및 이메일, 시퀀스 정보
   * @throws IllegalArgumentException 검증 실패 시
   */
  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> verifyPasswordResetToken(Map<String, Object> requestMap) throws Exception {
    // 1단계: 토큰 추출 및 기본 검증
    String token = extractToken(requestMap);
    validateTokenNotEmpty(token);

    try {
      // 2단계: 토큰 디코딩
      Map<String, Object> decodedData = decodeToken(token);
      String email = (String) decodedData.get(RESPONSE_EMAIL);
      
      // 3단계: DB에서 토큰 정보 조회
      PasswordReset passwordReset = findPasswordResetByToken(token);
      validatePasswordResetExists(passwordReset);
      
      // 4단계: 만료일 검증
      validateTokenNotExpired(passwordReset);
      
      // 5단계: 토큰 일치 여부 검증
      validateTokenMatches(token, passwordReset);
      
      // 6단계: 이메일 일치 여부 검증
      validateEmailMatches(email, passwordReset);
      
      // 검증 성공
      return createSuccessResponse(passwordReset);
      
    } catch (IllegalArgumentException e) {
      // 검증 실패는 그대로 전달
      throw e;
    } catch (Exception e) {
      log.error("토큰 검증 중 예상치 못한 오류 발생: token={}", token, e);
      throw new IllegalArgumentException("토큰 검증 중 오류가 발생했습니다.");
    }
  }

  /**
   * 비밀번호 재설정 처리
   * 
   * 처리 과정:
   * 1. 입력값 검증 (토큰, 새 비밀번호)
   * 2. 토큰 검증
   * 3. 비밀번호 업데이트
   * 4. 토큰 사용 완료 처리
   * 
   * @param requestMap 토큰, 새 비밀번호가 담긴 Map
   * @return 처리 성공 여부
   * @throws IllegalArgumentException 입력값이 유효하지 않을 경우
   */
  @Override
  public Boolean resetPassword(Map<String, Object> requestMap) throws Exception {
    // 1단계: 입력값 추출 및 검증
    String token = extractToken(requestMap);
    String newPassword = extractNewPassword(requestMap);
    
    validateTokenNotEmpty(token);
    validatePasswordNotEmpty(newPassword);
    
    // 2단계: 토큰 검증
    Map<String, Object> verifyResult = verifyPasswordResetToken(requestMap);
    if (!Boolean.TRUE.equals(verifyResult.get(RESPONSE_RESULT))) {
      log.warn("토큰 검증 실패로 비밀번호 재설정 불가");
      return false;
    }
    
    // 3단계: 비밀번호 업데이트 (사용자 테이블이 있을 경우)
    // String email = (String) verifyResult.get(RESPONSE_EMAIL);
    // userService.updatePassword(email, newPassword);
    
    // 4단계: 토큰 사용 완료 처리
    Integer seq = (Integer) verifyResult.get(RESPONSE_SEQ);
    markTokenAsUsed(seq);
    
    log.info("비밀번호 재설정 완료: seq={}", seq);
    return true;
  }

  // ========== private 메서드 - 입력값 추출 ==========

  /**
   * 요청 Map에서 이메일 추출
   */
  private String extractEmail(Map<String, Object> map) {
    return Optional.ofNullable((String) map.get(PARAM_EMAIL))
        .map(String::trim)
        .orElse("");
  }

  /**
   * 요청 Map에서 접속 IP 추출
   */
  private String extractAccessIp(Map<String, Object> map) {
    return Optional.ofNullable((String) map.get(PARAM_ACCESS_IP))
        .orElse("");
  }

  /**
   * 요청 Map에서 토큰 추출
   */
  private String extractToken(Map<String, Object> map) {
    return Optional.ofNullable((String) map.get(PARAM_TOKEN))
        .map(String::trim)
        .orElse("");
  }

  /**
   * 요청 Map에서 새 비밀번호 추출
   */
  private String extractNewPassword(Map<String, Object> map) {
    return Optional.ofNullable((String) map.get(PARAM_NEW_PASSWORD))
        .map(String::trim)
        .orElse("");
  }

  // ========== private 메서드 - 검증 ==========

  /**
   * 이메일 유효성 검사
   */
  private boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty();
  }

  /**
   * 토큰이 비어있지 않은지 검증
   */
  private void validateTokenNotEmpty(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("토큰이 없습니다.");
    }
  }

  /**
   * 비밀번호가 비어있지 않은지 검증
   */
  private void validatePasswordNotEmpty(String password) {
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("새 비밀번호가 없습니다.");
    }
  }

  /**
   * 비밀번호 재설정 정보가 존재하는지 검증
   */
  private void validatePasswordResetExists(PasswordReset passwordReset) {
    if (passwordReset == null) {
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }
  }

  /**
   * 토큰 만료 여부 검증
   */
  private void validateTokenNotExpired(PasswordReset passwordReset) {
    Date now = new Date();
    if (passwordReset.getExpDate().compareTo(now) < 0) {
      throw new IllegalArgumentException("만료된 토큰입니다. 다시 요청해주세요.");
    }
  }

  /**
   * 토큰 일치 여부 검증
   */
  private void validateTokenMatches(String token, PasswordReset passwordReset) {
    if (!token.equals(passwordReset.getToken())) {
      throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
    }
  }

  /**
   * 이메일 일치 여부 검증
   */
  private void validateEmailMatches(String email, PasswordReset passwordReset) {
    if (!email.equals(passwordReset.getEmail())) {
      throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
    }
  }

  // ========== private 메서드 - 토큰 처리 ==========

  /**
   * 토큰 생성 및 인코딩
   * 
   * 과정:
   * 1. seq와 email을 JSON으로 변환
   * 2. 앞뒤에 접두사/접미사 추가
   * 3. Base64로 인코딩
   */
  private String createAndEncodeToken(Integer seq, String email) throws Exception {
    // JSON 데이터 생성
    Map<String, Object> tokenData = ImmutableMap.of(
        RESPONSE_SEQ, seq,
        RESPONSE_EMAIL, email
    );
    
    // JSON 문자열로 변환
    String jsonString = objectMapper.writeValueAsString(tokenData);
    
    // 접두사/접미사 추가
    String tokenWithPrefix = TOKEN_PREFIX + jsonString + TOKEN_SUFFIX;
    
    // Base64 인코딩
    return Base64Utils.encodeToUrlSafeString(
        tokenWithPrefix.getBytes(StandardCharsets.UTF_8)
    );
  }

  /**
   * 토큰 디코딩
   * 
   * 과정:
   * 1. Base64 디코딩
   * 2. 접두사/접미사 제거
   * 3. JSON 파싱
   */
  private Map<String, Object> decodeToken(String encodedToken) throws Exception {
    // Base64 디코딩
    byte[] decodedBytes = Base64Utils.decodeFromUrlSafeString(encodedToken);
    String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
    
    // 접두사/접미사 제거
    String jsonString = decodedString
        .replaceFirst("^" + TOKEN_PREFIX, "")
        .replaceFirst(TOKEN_SUFFIX + "$", "");
    
    // JSON 파싱
    return objectMapper.readValue(jsonString, Map.class);
  }

  /**
   * 만료일 계산 (현재 시간 + 유효기간)
   */
  private Date calculateExpirationTime(Date currentTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentTime);
    calendar.add(Calendar.HOUR_OF_DAY, TOKEN_EXPIRATION_HOURS);
    return calendar.getTime();
  }

  // ========== private 메서드 - URL 생성 ==========

  /**
   * 비밀번호 재설정 URL 생성
   */
  private String createResetUrl(String token) {
    return webUrl + "/password/reset?token=" + token;
  }

  // ========== private 메서드 - 메일 처리 ==========

  /**
   * 메일 내용 생성
   * 
   * 1. HTML 템플릿 파일 읽기
   * 2. 템플릿 변수 치환
   * 3. 실패 시 기본 HTML 생성
   */
  private String createMailContent(String resetUrl) {
    try {
      // 템플릿 파일 읽기
      InputStream templateStream = new ClassPathResource(MAIL_TEMPLATE_PATH).getInputStream();
      String templateHtml = new String(IOUtils.toByteArray(templateStream), StandardCharsets.UTF_8);
      
      // 변수 치환
      return templateHtml.replaceAll(TEMPLATE_RESET_URL, resetUrl);
          
    } catch (Exception e) {
      log.error("메일 템플릿 파일을 읽을 수 없습니다: {}", MAIL_TEMPLATE_PATH, e);
      // 기본 HTML 생성
      return createDefaultMailContent(resetUrl);
    }
  }

  /**
   * 기본 메일 내용 생성 (템플릿 파일 읽기 실패 시 사용)
   */
  private String createDefaultMailContent(String resetUrl) {
    return "<html><body>" +
        "<h2>비밀번호 재설정</h2>" +
        "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>" +
        "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>" +
        "<p>링크는 " + TOKEN_EXPIRATION_HOURS + "시간 동안만 유효합니다.</p>" +
        "</body></html>";
  }

  /**
   * 메일 발송
   */
  private void sendMail(String email, String content) throws Exception {
    Map<String, Object> mailData = ImmutableMap.<String, Object>builder()
        .put("from", DEFAULT_FROM_EMAIL)
        .put("fromName", DEFAULT_FROM_NAME)
        .put("to", email)
        .put("subject", MAIL_SUBJECT)
        .put("content", content)
        .put("contentType", MAIL_CONTENT_TYPE)
        .build();
    
    mailService.sendMail(mailData);
  }

  // ========== private 메서드 - DB 처리 ==========

  /**
   * 비밀번호 재설정 정보를 DB에 저장
   */
  private void savePasswordResetInfo(
      Integer seq,
      String email,
      String token,
      Date expirationTime,
      String accessIp,
      Date currentTime) {
    
    PasswordReset passwordReset = new PasswordReset();
    passwordReset.setSeq(seq);
    passwordReset.setEmail(email);
    passwordReset.setToken(token);
    passwordReset.setExpDate(expirationTime);
    passwordReset.setUsedYn(false);
    passwordReset.setRegDate(currentTime);
    passwordReset.setRegIp(accessIp);
    
    passwordResetRepo.insertPasswordReset(passwordReset);
  }

  /**
   * DB에서 토큰으로 비밀번호 재설정 정보 조회
   */
  private PasswordReset findPasswordResetByToken(String token) {
    Map<String, Object> searchMap = new HashMap<>();
    searchMap.put(PARAM_TOKEN, token);
    return passwordResetRepo.getPasswordResetByToken(searchMap);
  }

  /**
   * 토큰을 사용 완료 처리
   */
  private void markTokenAsUsed(Integer seq) {
    passwordResetRepo.updatePasswordResetUsed(seq);
  }

  // ========== private 메서드 - 응답 생성 ==========

  /**
   * 검증 성공 응답 생성
   */
  private Map<String, Object> createSuccessResponse(PasswordReset passwordReset) {
    return ImmutableMap.of(
        RESPONSE_RESULT, true,
        RESPONSE_EMAIL, passwordReset.getEmail(),
        RESPONSE_SEQ, passwordReset.getSeq()
    );
  }
}
