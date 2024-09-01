package com.board.thymeleaf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.board.thymeleaf.domain.BaseSO;
import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.service.ifc.BoardService;

import groovyjarjarantlr4.v4.parse.ANTLRParser.sync_return;
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
    return "board/list";
  }

  @GetMapping("/write")
  public String getBoardWrite(Model model) throws Exception {
    model.addAttribute("board", new Board());
    return "board/write";
  }

  @GetMapping("/view")
  public String getBoardView(@RequestParam Integer seq, Model model) throws Exception {
    Board board = boardService.getBoardView(seq);
    model.addAttribute("board", board);
    return "board/view";
  }

  @PostMapping("/insert")
  public String insertBoard(@ModelAttribute Board board,Model model) throws Exception {
    boardService.insertBoard(board);
    return "redirect:/board/list";
  }

  @GetMapping("/reply")
  public String getBoardReply(@RequestParam Integer seq, Model model) throws Exception {
    Board board = boardService.getBoardView(seq);
    model.addAttribute("board", board);
    model.addAttribute("replyboard", new Board());
    return "board/reply";
  }

  @PostMapping("/reply/insert")
  public String insertReplyBoard(@RequestParam("parentSeq") int parentSeq, @ModelAttribute Board board,Model model) throws Exception {
    System.out.println("parentSeq============"+parentSeq);
    System.out.println("board==========="+board);
    
    //boardService.insertBoard(board);
    return "redirect:/board/list";
  }



}
