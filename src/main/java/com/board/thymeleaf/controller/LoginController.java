package com.board.thymeleaf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 로그인 컨트롤러
 * 
 * 로그인 페이지를 보여줍니다.
 */
@Controller
@RequestMapping("/login")
public class LoginController {

  /** 로그인 페이지 템플릿 경로 */
  private static final String VIEW_LOGIN_PAGE = "login/login";

  /**
   * 로그인 페이지를 보여줍니다.
   * 
   * @param model 뷰에 전달할 데이터를 담는 모델
   * @return 로그인 페이지
   */
  @GetMapping
  public String showLoginPage(Model model) {
    return VIEW_LOGIN_PAGE;
  }
}
