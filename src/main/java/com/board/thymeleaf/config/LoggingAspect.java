package com.board.thymeleaf.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  /**
   * Controller 패키지의 모든 메서드에 대한 포인트컷
   */
  @Pointcut("execution(* com.board.thymeleaf.controller..*(..))")
  public void controllerPointcut() {}

  /**
   * Service 패키지의 모든 메서드에 대한 포인트컷
   */
  @Pointcut("execution(* com.board.thymeleaf.service..*(..))")
  public void servicePointcut() {}

  /**
   * Repository 패키지의 모든 메서드에 대한 포인트컷
   */
  @Pointcut("execution(* com.board.thymeleaf.repository..*(..))")
  public void repositoryPointcut() {}

  /**
   * Controller 메서드 실행 전후 로깅
   */
  @Around("controllerPointcut()")
  public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    log.info("[Controller 시작] {}.{}() - 파라미터: {}", className, methodName, formatArgs(args));
    
    long startTime = System.currentTimeMillis();
    Object result = null;
    try {
      result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.info("[Controller 종료] {}.{}() - 실행시간: {}ms - 결과: {}", 
          className, methodName, executionTime, formatResult(result));
      return result;
    } catch (Throwable e) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.error("[Controller 예외] {}.{}() - 실행시간: {}ms - 예외: {}", 
          className, methodName, executionTime, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Service 메서드 실행 전후 로깅
   */
  @Around("servicePointcut()")
  public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    log.info("[Service 시작] {}.{}() - 파라미터: {}", className, methodName, formatArgs(args));
    
    long startTime = System.currentTimeMillis();
    Object result = null;
    try {
      result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.info("[Service 종료] {}.{}() - 실행시간: {}ms - 결과: {}", 
          className, methodName, executionTime, formatResult(result));
      return result;
    } catch (Throwable e) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.error("[Service 예외] {}.{}() - 실행시간: {}ms - 예외: {}", 
          className, methodName, executionTime, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Repository 메서드 실행 전후 로깅
   */
  @Around("repositoryPointcut()")
  public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    log.info("[Repository 시작] {}.{}() - 파라미터: {}", className, methodName, formatArgs(args));
    
    long startTime = System.currentTimeMillis();
    Object result = null;
    try {
      result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.info("[Repository 종료] {}.{}() - 실행시간: {}ms - 결과: {}", 
          className, methodName, executionTime, formatResult(result));
      return result;
    } catch (Throwable e) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      
      log.error("[Repository 예외] {}.{}() - 실행시간: {}ms - 예외: {}", 
          className, methodName, executionTime, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 파라미터 포맷팅
   */
  private String formatArgs(Object[] args) {
    if (args == null || args.length == 0) {
      return "없음";
    }
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(formatObject(args[i]));
    }
    return sb.toString();
  }

  /**
   * 결과 포맷팅
   */
  private String formatResult(Object result) {
    if (result == null) {
      return "null";
    }
    
    // Collection이나 배열인 경우 크기만 표시
    if (result instanceof java.util.Collection) {
      int size = ((java.util.Collection<?>) result).size();
      return String.format("Collection(size=%d)", size);
    }
    
    if (result.getClass().isArray()) {
      int length = java.lang.reflect.Array.getLength(result);
      return String.format("Array(length=%d)", length);
    }
    
    // 큰 객체는 간단히 표시
    String resultStr = result.toString();
    if (resultStr.length() > 200) {
      return resultStr.substring(0, 200) + "...";
    }
    
    return resultStr;
  }

  /**
   * 객체 포맷팅
   */
  private String formatObject(Object obj) {
    if (obj == null) {
      return "null";
    }
    
    // MultipartFile은 파일명만 표시
    if (obj instanceof org.springframework.web.multipart.MultipartFile) {
      org.springframework.web.multipart.MultipartFile file = 
          (org.springframework.web.multipart.MultipartFile) obj;
      return String.format("MultipartFile(name=%s, size=%d)", 
          file.getOriginalFilename(), file.getSize());
    }
    
    // List인 경우 크기만 표시
    if (obj instanceof java.util.List) {
      int size = ((java.util.List<?>) obj).size();
      return String.format("List(size=%d)", size);
    }
    
    // Map인 경우 간단히 표시
    if (obj instanceof java.util.Map) {
      int size = ((java.util.Map<?, ?>) obj).size();
      return String.format("Map(size=%d)", size);
    }
    
    // 큰 문자열은 잘라서 표시
    String str = obj.toString();
    if (str.length() > 100) {
      return str.substring(0, 100) + "...";
    }
    
    return str;
  }
}

