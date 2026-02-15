package com.board.thymeleaf.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 비밀번호 해시 생성 테스트 클래스
 * 
 * 이 클래스를 실행하여 실제 BCrypt 해시를 생성할 수 있습니다.
 * 생성된 해시를 data.sql에 사용하세요.
 */
public class PasswordHashGenerator {

  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    // 테스트용 비밀번호들
    String[] passwords = {"admin123", "user123", "test123"};
    
    System.out.println("=== BCrypt 비밀번호 해시 생성 ===\n");
    
    for (String password : passwords) {
      String hash = encoder.encode(password);
      System.out.println("원본 비밀번호: " + password);
      System.out.println("BCrypt 해시: " + hash);
      System.out.println("검증 결과: " + encoder.matches(password, hash));
      System.out.println();
    }
    
    System.out.println("=== 생성된 해시를 data.sql에 복사하여 사용하세요 ===");
  }
}

