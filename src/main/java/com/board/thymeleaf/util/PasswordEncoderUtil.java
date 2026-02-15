package com.board.thymeleaf.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 유틸리티 클래스
 * 
 * BCrypt를 사용하여 비밀번호를 암호화합니다.
 * 이 클래스는 주로 초기 데이터 설정이나 마이그레이션 시 사용됩니다.
 */
public class PasswordEncoderUtil {

  private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  /**
   * 비밀번호를 BCrypt로 암호화합니다.
   * 
   * @param rawPassword 평문 비밀번호
   * @return 암호화된 비밀번호 (BCrypt 해시)
   */
  public static String encode(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  /**
   * 평문 비밀번호와 암호화된 비밀번호를 비교합니다.
   * 
   * @param rawPassword 평문 비밀번호
   * @param encodedPassword 암호화된 비밀번호
   * @return 일치하면 true, 아니면 false
   */
  public static boolean matches(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  /**
   * 메인 메서드 - 비밀번호 암호화 테스트 및 해시 생성
   * 
   * 이 메서드를 실행하여 BCrypt 해시를 생성할 수 있습니다.
   * 
   * @param args [0]: 암호화할 비밀번호
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("사용법: java PasswordEncoderUtil <비밀번호>");
      System.out.println("예시: java PasswordEncoderUtil admin123");
      return;
    }

    String password = args[0];
    String encoded = encode(password);
    System.out.println("원본 비밀번호: " + password);
    System.out.println("암호화된 비밀번호: " + encoded);
    System.out.println("검증 결과: " + matches(password, encoded));
  }
}

