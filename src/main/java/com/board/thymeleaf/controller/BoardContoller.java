package com.board.thymeleaf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.service.ifc.BoardService;
import com.google.common.collect.ImmutableMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequestMapping("/api/board")
@RestController
@RequiredArgsConstructor
@Slf4j
public class BoardContoller {

	private final BoardService boardService;

  @GetMapping("/list")
  public Map<?,?> getBoardList(@RequestParam(required = false) Map<String,Object> map) throws Exception {
		List<PageBoard> list = boardService.getBoardList(map);
    Pager<PageBoard> page =  Pager.formList(list);

    return ImmutableMap.<String, Object>builder()
    .put("page",page)
    .build();

  }

}
