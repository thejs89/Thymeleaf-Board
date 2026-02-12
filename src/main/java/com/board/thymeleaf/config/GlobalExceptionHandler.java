package com.board.thymeleaf.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
// import org.springframework.validation.BindException;
// import org.springframework.validation.FieldError;
// import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리 핸들러
 * 
 * 현재 프로젝트는 @RequestParam, @RequestPart를 사용하므로
 * Validation 예외 핸들러는 실제로 작동하지 않습니다.
 * 향후 @ModelAttribute + @Valid 또는 @RequestBody + @Valid 사용 시 활성화됩니다.
 * 
 * - 사용자 에러 (IllegalArgumentException, IllegalStateException): JSON/HTML 응답
 * - DB 제약 조건 위반 (DataIntegrityViolationException): JSON/HTML 응답
 * - 서버 에러 (나머지): 에러 페이지 HTML
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  // ========== Validation 예외 핸들러 (현재 프로젝트에서 사용 안 함) ==========
  // 현재 프로젝트는 @RequestParam, @RequestPart만 사용하므로 아래 핸들러는 작동하지 않습니다.
  // 향후 @ModelAttribute + @Valid 또는 @RequestBody + @Valid 사용 시 활성화됩니다.

  /**
   * Validation 에러 처리 - BindException
   * @ModelAttribute + @Valid 조합에서 발생하는 예외 처리
   * 현재 프로젝트에서는 사용하지 않음 (향후 사용 가능)
   */
  // @ExceptionHandler(BindException.class)
  // public Object handleBindException(BindException e, HttpServletRequest request, Model model) {
  //   // 구현 생략 - 필요 시 주석 해제
  // }

  /**
   * Validation 에러 처리 - MethodArgumentNotValidException
   * @RequestBody + @Valid 조합에서 발생하는 예외 처리
   * 현재 프로젝트에서는 사용하지 않음 (향후 REST API 사용 시 활성화)
   */
  // @ExceptionHandler(MethodArgumentNotValidException.class)
  // public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request, Model model) {
  //   // 구현 생략 - 필요 시 주석 해제
  // }

  /**
   * DB 제약 조건 위반 에러 처리 - DataIntegrityViolationException
   * DB 크기 초과, NOT NULL 위반 등 사용자 입력 오류로 처리
   * 요청 타입에 따라 JSON 또는 HTML 응답 반환
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public Object handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request, Model model) {
    log.warn("DB 제약 조건 위반: {}", e.getMessage());
    
    // Accept 헤더 확인하여 JSON 요청인지 판단
    String acceptHeader = request.getHeader("Accept");
    boolean isJsonRequest = acceptHeader != null && acceptHeader.contains("application/json");
    
    // 에러 메시지 추출 및 정리
    String errorMessage = extractUserFriendlyMessage(e);
    
    if (isJsonRequest) {
      // JSON 요청인 경우 JSON 응답
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
      errorResponse.put("error", "입력한 데이터가 올바르지 않습니다.");
      errorResponse.put("message", errorMessage);
      errorResponse.put("userError", true);
      
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    } else {
      // HTML 요청인 경우 HTML 에러 페이지
      ModelAndView mav = new ModelAndView("error/4xx");
      mav.addObject("status", HttpStatus.BAD_REQUEST.value());
      mav.addObject("error", "입력한 데이터가 올바르지 않습니다.");
      mav.addObject("message", errorMessage);
      mav.setStatus(HttpStatus.BAD_REQUEST);
      
      return mav;
    }
  }

  /**
   * 사용자 에러 처리 - IllegalArgumentException, IllegalStateException
   * 요청 타입에 따라 JSON 또는 HTML 응답 반환
   */
  @ExceptionHandler({
      IllegalArgumentException.class,
      IllegalStateException.class
  })
  public Object handleUserErrors(Exception e, HttpServletRequest request, Model model) {
    log.warn("사용자 입력 오류 ({}): {}", e.getClass().getSimpleName(), e.getMessage());
    
    // Accept 헤더 확인하여 JSON 요청인지 판단
    String acceptHeader = request.getHeader("Accept");
    boolean isJsonRequest = acceptHeader != null && acceptHeader.contains("application/json");
    
    String errorMessage = "잘못된 요청입니다.";
    if (e instanceof IllegalStateException) {
      errorMessage = "잘못된 요청 상태입니다.";
    }
    
    if (isJsonRequest) {
      // JSON 요청인 경우 JSON 응답
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
      errorResponse.put("error", errorMessage);
      errorResponse.put("message", e.getMessage());
      errorResponse.put("userError", true);
      
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    } else {
      // HTML 요청인 경우 HTML 에러 페이지
      ModelAndView mav = new ModelAndView("error/4xx");
      mav.addObject("status", HttpStatus.BAD_REQUEST.value());
      mav.addObject("error", errorMessage);
      mav.addObject("message", e.getMessage());
      mav.setStatus(HttpStatus.BAD_REQUEST);
      
      return mav;
    }
  }

  /**
   * RuntimeException 처리
   * 요청 타입에 따라 JSON 또는 HTML 응답 반환
   * - JSON 요청 (Accept: application/json): JSON 응답
   * - HTML 요청: HTML 에러 페이지
   * 
   * RuntimeException은 서버 에러(500)로 처리
   * (DB 제약 조건 위반은 DataIntegrityViolationException 핸들러에서 처리)
   */
  @ExceptionHandler(RuntimeException.class)
  public Object handleRuntimeException(RuntimeException e, HttpServletRequest request, Model model) {
    log.error("런타임 예외 발생: {}", e.getMessage(), e);
    
    // Accept 헤더 확인하여 JSON 요청인지 판단
    String acceptHeader = request.getHeader("Accept");
    boolean isJsonRequest = acceptHeader != null && acceptHeader.contains("application/json");
    
    String errorMessage = e.getMessage();
    if (errorMessage == null || errorMessage.isEmpty()) {
      errorMessage = "서버 내부 오류가 발생했습니다.";
    }
    
    if (isJsonRequest) {
      // JSON 요청인 경우 JSON 응답
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
      errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
      errorResponse.put("message", errorMessage);
      errorResponse.put("userError", false);
      
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    } else {
      // HTML 요청인 경우 HTML 에러 페이지
      ModelAndView mav = new ModelAndView("error/500");
      mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
      mav.addObject("error", "서버 내부 오류가 발생했습니다.");
      mav.addObject("message", errorMessage);
      mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      
      return mav;
    }
  }

  /**
   * 서버 에러 - 일반 Exception 처리 (500 에러)
   * 요청 타입에 따라 JSON 또는 HTML 응답 반환
   */
  @ExceptionHandler(Exception.class)
  public Object handleException(Exception e, HttpServletRequest request, Model model) {
    log.error("예외 발생: {}", e.getMessage(), e);
    
    // Accept 헤더 확인하여 JSON 요청인지 판단
    String acceptHeader = request.getHeader("Accept");
    boolean isJsonRequest = acceptHeader != null && acceptHeader.contains("application/json");
    
    String errorMessage = e.getMessage();
    if (errorMessage == null || errorMessage.isEmpty()) {
      errorMessage = "서버 내부 오류가 발생했습니다.";
    }
    
    if (isJsonRequest) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
      errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
      errorResponse.put("message", errorMessage);
      errorResponse.put("userError", false);
      
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    } else {
      ModelAndView mav = new ModelAndView("error/500");
      mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
      mav.addObject("error", "서버 내부 오류가 발생했습니다.");
      mav.addObject("message", errorMessage);
      mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      
      return mav;
    }
  }

  /**
   * 사용자 친화적인 에러 메시지 추출
   */
  private String extractUserFriendlyMessage(Exception e) {
    String errorMessage = e.getMessage();
    if (errorMessage == null || errorMessage.isEmpty()) {
      return "입력한 정보를 확인해주세요.";
    }
    
    String lowerMessage = errorMessage.toLowerCase();
    
    // DB 크기 초과 에러
    if (lowerMessage.contains("value too long") || 
        lowerMessage.contains("too long for column") ||
        lowerMessage.contains("data too long") ||
        lowerMessage.contains("string data, right truncated")) {
      return "입력한 데이터가 너무 깁니다. 길이를 확인해주세요.";
    }
    
    // NOT NULL 제약 위반
    if (lowerMessage.contains("cannot be null") || 
        lowerMessage.contains("not null constraint")) {
      return "필수 입력 항목이 누락되었습니다.";
    }
    
    // 외래키 제약 위반
    if (lowerMessage.contains("foreign key") || 
        lowerMessage.contains("referential integrity")) {
      return "연관된 데이터가 존재하지 않습니다.";
    }
    
    // 중복 키 에러
    if (lowerMessage.contains("unique constraint") || 
        lowerMessage.contains("duplicate key")) {
      return "이미 존재하는 데이터입니다.";
    }
    
    // 기타 제약 조건 위반
    if (lowerMessage.contains("constraint") || 
        lowerMessage.contains("integrity constraint")) {
      return "입력한 데이터가 데이터베이스 제약 조건을 위반했습니다.";
    }
    
    // 원본 메시지 반환 (한글이 포함된 경우 그대로 사용)
    return errorMessage;
  }
}


