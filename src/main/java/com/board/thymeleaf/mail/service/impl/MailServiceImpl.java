package com.board.thymeleaf.mail.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.board.thymeleaf.mail.domain.MailPO;
import com.board.thymeleaf.mail.service.ifc.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * MailServiceImpl.java
 * 설명: 메일 전송 service impl
 **/
@Slf4j
@Service
public class MailServiceImpl implements MailService {
  
  @Autowired
  private JavaMailSender javaMailSender;
  
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  public void sendMail(Map<String, Object> map) throws Exception {
    MailPO po = objectMapper.convertValue(map, MailPO.class);
    sendMail(po);
  }
  
  @Override
  public void sendMail(MailPO po) throws Exception {
    MimeMessage msg = javaMailSender.createMimeMessage();

    String from = Optional.ofNullable(po.getFrom()).orElse("");
    String fromName = Optional.ofNullable(po.getFromName()).orElse("");
    String to = Optional.ofNullable(po.getTo()).orElse("");
    String cc = Optional.ofNullable(po.getCc()).orElse("");
    String bcc = Optional.ofNullable(po.getBcc()).orElse("");
    String subject = Optional.ofNullable(po.getSubject()).orElse("");
    String content = Optional.ofNullable(po.getContent()).orElse("");
    String contentType = Optional.ofNullable(po.getContentType()).orElse("text/html;charset=UTF-8");
    Date now = new Date();
    
    log.debug("from: {}", from);
    log.debug("fromName: {}", fromName);
    log.debug("to: {}", to);
    log.debug("cc: {}", cc);
    log.debug("bcc: {}", bcc);
    log.debug("subject: {}", subject);
    log.debug("content: {}", content);
    log.debug("contentType: {}", contentType);
    log.debug("now: {}", now);

    // 빈 문자열 체크하여 설정
    if (!from.isEmpty()) {
      msg.setFrom(fromName.isEmpty() 
          ? new InternetAddress(from) 
          : new InternetAddress(from, fromName));
    }
    
    if (!to.isEmpty()) {
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    }
    
    if (!cc.isEmpty()) {
      msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
    }
    
    if (!bcc.isEmpty()) {
      msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
    }
    
    msg.setSubject(subject);
    msg.setContent(content, contentType);
    msg.setSentDate(now);
    
    javaMailSender.send(msg);
  }
}

