package com.board.thymeleaf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.board.thymeleaf.domain.BaseSO;
import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.service.ifc.BoardService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  public String insertBoard(@RequestPart(value = "file") List<MultipartFile> fileList, @RequestParam(required=false) Map<String, Object> map,Model model) throws Exception {  
    boardService.insertBoard(fileList,map);
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
  public String insertReplyBoard(@RequestParam(required = false) Map<String,Object> map,Model model) throws Exception {

    map = Optional.of(Optional.ofNullable(map).orElse(new HashMap<>()))
      .map(m -> {
        m.put("display", Optional.ofNullable((Boolean) m.get("display")).orElse(false));
        return m;
      }).get();

    boardService.insertReplyBoard(map);
    return "redirect:/board/list";
  }

  @PostMapping("/remove")
  public String postMethodName(@RequestParam Integer seq) throws Exception {
    boardService.deleteBoard(seq);
    return "redirect:/board/list";
  }

  @GetMapping("/tree")
  public String getTreeView(@RequestParam(required = false) Map<String,Object> map,Model mode) {
    return "board/tree";
  }
  
  



}
