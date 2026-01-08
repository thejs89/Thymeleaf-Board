package com.board.thymeleaf.domain;

import java.util.Date;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Alias("passwordReset")
public class PasswordReset {
  private int seq;
  private String email;
  private String token;
  private Date expDate;
  private boolean usedYn;
  private Date regDate;
  private String regIp;
}

