package com.board.thymeleaf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.board.thymeleaf.domain.BaseSO;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.service.ifc.BoardService;
import com.google.common.collect.ImmutableMap;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/board")
@RestController
@RequiredArgsConstructor
public class BoardContollerApi {

	private final BoardService boardService;

  @GetMapping("/list")
  public Map<?,?> getBoardList(@RequestParam(required = false) Map<String,Object> map) throws Exception {
    BaseSO so = new BaseSO(map);
		List<PageBoard> list = boardService.getBoardList(so);
    Pager<PageBoard> page =  Pager.formList(list);

    return ImmutableMap.<String, Object>builder()
    .put("page",page)
    .build();

  }

}
