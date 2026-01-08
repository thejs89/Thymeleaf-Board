package com.board.thymeleaf.mail.service.ifc;

import java.util.Map;

import com.board.thymeleaf.mail.domain.MailPO;

/**
 * MailService.java
 * 설명: 메일 전송 service
 **/
public interface MailService {
  void sendMail(Map<String, Object> map) throws Exception;
  void sendMail(MailPO po) throws Exception;
}

