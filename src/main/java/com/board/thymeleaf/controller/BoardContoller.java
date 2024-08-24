package com.board.thymeleaf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.board.thymeleaf.domain.BaseSO;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.service.ifc.BoardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardContoller {

	private final BoardService boardService;

  @GetMapping("/list")
  public String getBoardList(@RequestParam(required = false) Map<String,Object> map,Model model) throws Exception {
    BaseSO so = new BaseSO(map);
		List<PageBoard> list = boardService.getBoardList(so);
    Pager<PageBoard> page =  Pager.formList(list);

    model.addAttribute("vo", map);
    model.addAttribute("page", page);
    return "board";

  }

}
