package com.board.thymeleaf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 에러 페이지 컨트롤러
 * 
 * 접근 거부 및 기타 에러 페이지를 처리합니다.
 */
@Controller
@RequestMapping("/error")
public class ErrorController {

  /**
   * 403 접근 거부 페이지
   * 
   * @return 403 에러 페이지
   */
  @GetMapping("/403")
  public String error403() {
    return "error/403";
  }
}

