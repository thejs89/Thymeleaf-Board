package com.board.thymeleaf.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.board.thymeleaf.service.ifc.PasswordResetService;
import com.google.common.collect.ImmutableMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 재설정 컨트롤러
 * 
 * 주요 기능:
 * 1. 비밀번호 재설정 요청 페이지 표시
 * 2. 비밀번호 재설정 메일 발송 요청 처리
 * 3. 토큰 검증 및 비밀번호 재설정 페이지 표시
 * 4. 비밀번호 재설정 처리
 */
@Slf4j
@Controller
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

  // ========== 상수 정의 ==========
  
  /** 뷰 이름 */
  private static final String VIEW_RESET_REQUEST = "password/reset-request";
  private static final String VIEW_RESET = "password/reset";
  
  /** 모델 속성 키 */
  private static final String MODEL_EMAIL = "email";
  private static final String MODEL_ERROR = "error";
  private static final String MODEL_TOKEN = "token";
  
  /** 요청 파라미터 키 */
  private static final String PARAM_EMAIL = "email";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_NEW_PASSWORD = "newPassword";
  
  /** 응답 키 */
  private static final String RESPONSE_SUCCESS = "success";
  private static final String RESPONSE_MESSAGE = "message";
  
  /** 성공/실패 메시지 */
  private static final String MSG_MAIL_SENT = "비밀번호 재설정 메일이 발송되었습니다.";
  private static final String MSG_MAIL_FAILED = "메일 발송에 실패했습니다.";
  private static final String MSG_PASSWORD_CHANGED = "비밀번호가 성공적으로 변경되었습니다.";
  private static final String MSG_PASSWORD_CHANGE_FAILED = "비밀번호 변경에 실패했습니다.";
  private static final String MSG_PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
  private static final String MSG_TOKEN_REQUIRED = "토큰이 없습니다.";
  private static final String MSG_INVALID_TOKEN = "유효하지 않은 토큰입니다.";
  
  // ========== 의존성 주입 ==========
  
  private final PasswordResetService passwordResetService;

  // ========== 공개 메서드 ==========

  /**
   * 비밀번호 재설정 요청 페이지 표시
   * 
   * 사용자가 이메일을 입력하여 비밀번호 재설정 메일을 요청하는 페이지
   */
  @GetMapping("/reset-request")
  public String showResetRequestPage() {
    return VIEW_RESET_REQUEST;
  }

  /**
   * 비밀번호 재설정 메일 발송 요청 처리
   * 
   * 처리 과정:
   * 1. 이메일과 접속 IP 추출
   * 2. 서비스에 메일 발송 요청
   * 3. 결과에 따른 응답 반환
   * 
   * @param email 사용자 이메일
   * @param request HTTP 요청 (IP 주소 추출용)
   * @return 성공/실패 여부와 메시지
   */
  @PostMapping("/reset-request")
  @ResponseBody
  public Map<String, Object> handleResetRequest(
      @RequestParam String email,
      HttpServletRequest request) {
    
    try {
      // 접속 IP 추출
      String accessIp = extractClientIp(request);
      
      // 서비스에 메일 발송 요청
      Map<String, Object> requestData = ImmutableMap.of(
          PARAM_EMAIL, email,
          "accessIp", accessIp
      );
      
      Boolean isSuccess = passwordResetService.sendPasswordResetMail(requestData);
      
      // 결과에 따른 응답 생성
      if (Boolean.TRUE.equals(isSuccess)) {
        return createSuccessResponse(MSG_MAIL_SENT);
      } else {
        return createFailureResponse(MSG_MAIL_FAILED);
      }
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 메일 발송 실패: email={}", email, e);
      return createFailureResponse("메일 발송 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /**
   * 비밀번호 재설정 페이지 표시 (토큰 검증 포함)
   * 
   * 처리 과정:
   * 1. 토큰 존재 여부 확인
   * 2. 토큰 검증
   * 3. 검증 성공 시 재설정 페이지 표시, 실패 시 요청 페이지로 리다이렉트
   * 
   * @param token 비밀번호 재설정 토큰
   * @param model 뷰에 전달할 데이터
   * @return 뷰 이름
   */
  @GetMapping("/reset")
  public String showResetPage(
      @RequestParam(required = false) String token,
      Model model) {
    
    // 토큰이 없으면 요청 페이지로 이동
    if (token == null || token.trim().isEmpty()) {
      model.addAttribute(MODEL_ERROR, MSG_TOKEN_REQUIRED);
      return VIEW_RESET_REQUEST;
    }

    try {
      // 토큰 검증
      Map<String, Object> requestData = ImmutableMap.of(PARAM_TOKEN, token);
      Map<String, Object> verifyResult = passwordResetService.verifyPasswordResetToken(requestData);
      
      // 검증 성공 시 재설정 페이지 표시
      if (Boolean.TRUE.equals(verifyResult.get("result"))) {
        model.addAttribute(MODEL_TOKEN, token);
        model.addAttribute(MODEL_EMAIL, verifyResult.get("email"));
        return VIEW_RESET;
      } else {
        // 검증 실패 시 요청 페이지로 이동
        model.addAttribute(MODEL_ERROR, MSG_INVALID_TOKEN);
        return VIEW_RESET_REQUEST;
      }
      
    } catch (Exception e) {
      log.error("토큰 검증 실패: token={}", token, e);
      model.addAttribute(MODEL_ERROR, e.getMessage());
      return VIEW_RESET_REQUEST;
    }
  }

  /**
   * 비밀번호 재설정 처리
   * 
   * 처리 과정:
   * 1. 비밀번호 일치 여부 확인
   * 2. 서비스에 비밀번호 재설정 요청
   * 3. 결과에 따른 응답 반환
   * 
   * @param token 비밀번호 재설정 토큰
   * @param newPassword 새 비밀번호
   * @param confirmPassword 비밀번호 확인
   * @return 성공/실패 여부와 메시지
   */
  @PostMapping("/reset")
  @ResponseBody
  public Map<String, Object> handlePasswordReset(
      @RequestParam String token,
      @RequestParam String newPassword,
      @RequestParam(required = false) String confirmPassword) {
    
    try {
      // 비밀번호 일치 여부 확인
      if (!newPassword.equals(confirmPassword)) {
        return createFailureResponse(MSG_PASSWORD_MISMATCH);
      }
      
      // 서비스에 비밀번호 재설정 요청
      Map<String, Object> requestData = ImmutableMap.of(
          PARAM_TOKEN, token,
          PARAM_NEW_PASSWORD, newPassword
      );
      
      Boolean isSuccess = passwordResetService.resetPassword(requestData);
      
      // 결과에 따른 응답 생성
      if (Boolean.TRUE.equals(isSuccess)) {
        return createSuccessResponse(MSG_PASSWORD_CHANGED);
      } else {
        return createFailureResponse(MSG_PASSWORD_CHANGE_FAILED);
      }
      
    } catch (Exception e) {
      log.error("비밀번호 재설정 실패: token={}", token, e);
      return createFailureResponse("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // ========== private 메서드 ==========

  /**
   * 클라이언트 IP 주소 추출
   * 
   * 프록시나 로드밸런서를 거친 경우를 고려하여
   * X-Forwarded-For, X-Real-IP 헤더를 확인
   */
  private String extractClientIp(HttpServletRequest request) {
    // 프록시를 거친 경우의 IP 주소 확인
    String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
        .orElse(request.getHeader("X-Real-IP"));
    
    // 헤더에 IP가 없거나 "unknown"인 경우 직접 연결 IP 사용
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    
    return ip;
  }

  /**
   * 성공 응답 생성
   */
  private Map<String, Object> createSuccessResponse(String message) {
    return ImmutableMap.of(
        RESPONSE_SUCCESS, true,
        RESPONSE_MESSAGE, message
    );
  }

  /**
   * 실패 응답 생성
   */
  private Map<String, Object> createFailureResponse(String message) {
    return ImmutableMap.of(
        RESPONSE_SUCCESS, false,
        RESPONSE_MESSAGE, message
    );
  }
}
