package com.board.thymeleaf.domain;

import java.util.Date;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 회원 정보를 담는 도메인 클래스
 * 
 * 데이터베이스의 member 테이블과 매핑됩니다.
 * MyBatis에서 "member"라는 별칭으로 사용됩니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Alias("member")
public class Member {
  
  /** 회원 ID (기본키) */
  private String memberId;
  
  /** 비밀번호 */
  private String password;
  
  /** 회원 이름 */
  private String name;
  
  /** 이메일 주소 */
  private String email;
  
  /** 등록일시 */
  private Date regDate;
  
  /** 수정일시 */
  private Date updDate;
  
  /** 삭제 여부 (true: 삭제됨, false: 정상) */
  private boolean deleteYn;
  
  /** 권한 (콤마로 구분된 여러 권한 가능, 예: "ROLE_USER,ROLE_ADMIN", 없으면 기본값 ROLE_USER 사용) */
  private String role;
}

