package com.board.thymeleaf.mail.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class MailPO {
  private String from;
  private String fromName;
  // 수신, 쉼표로 다중 전송 가능
  private String to;
  // 참조, 쉼표로 다중 전송 가능
  private String cc;
  // 숨은참조, 쉼표로 다중 전송 가능
  private String bcc;
  private String subject;
  private String content;
  private String contentType;
}

