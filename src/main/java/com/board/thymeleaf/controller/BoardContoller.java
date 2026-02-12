package com.board.thymeleaf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.board.thymeleaf.domain.BaseSO;
import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.BoardFile;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.domain.Pager;
import com.board.thymeleaf.domain.TreeNode;
import com.board.thymeleaf.service.ifc.BoardService;
import com.board.thymeleaf.service.ifc.OrganizationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardContoller {

  private static final String REDIRECT_LIST = "redirect:/board/list";
  private static final String VIEW_BOARD_LIST = "board/list";
  private static final String VIEW_BOARD_WRITE = "board/write";
  private static final String VIEW_BOARD_VIEW = "board/view";
  private static final String VIEW_BOARD_REPLY = "board/reply";
  private static final String VIEW_BOARD_TREE = "board/tree";

  private static final String MODEL_BOARD = "board";
  private static final String MODEL_REPLY_BOARD = "replyboard";
  private static final String MODEL_FILE_LIST = "fileList";
  private static final String MODEL_PAGE = "page";
  private static final String MODEL_VO = "vo";

  private final BoardService boardService;
  private final OrganizationService organizationService;

  @GetMapping("/list")
  public String getBoardList(@RequestParam(required = false) Map<String, Object> map, Model model) throws Exception {
    BaseSO so = new BaseSO(map);
    List<PageBoard> list = boardService.getBoardList(so);
    Pager<PageBoard> page = Pager.formList(list);

    model.addAttribute(MODEL_VO, map);
    model.addAttribute(MODEL_PAGE, page);
    return VIEW_BOARD_LIST;
  }

  @GetMapping("/write")
  public String getBoardWrite(Model model) throws Exception {
    model.addAttribute(MODEL_BOARD, new Board());
    return VIEW_BOARD_WRITE;
  }

  @GetMapping("/view")
  public String getBoardView(@RequestParam Integer seq, Model model) throws Exception {
    Board board = boardService.getBoardView(seq);
    List<BoardFile> fileList = boardService.getBoardFileList(seq);
    
    model.addAttribute(MODEL_BOARD, board);
    model.addAttribute(MODEL_FILE_LIST, fileList);
    return VIEW_BOARD_VIEW;
  }

  @PostMapping("/insert")
  @ResponseBody
  public Map<String, Object> insertBoard(
      @RequestPart(value = "file", required = false) List<MultipartFile> fileList,
      @RequestParam(required = false) Map<String, Object> map,
      Model model) throws Exception {
    boardService.insertBoard(fileList, map);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("redirectUrl", "/board/list");
    result.put("message", "게시글이 등록되었습니다.");
    return result;
  }

  @GetMapping("/reply")
  public String getBoardReply(@RequestParam Integer seq, Model model) throws Exception {
    Board board = boardService.getBoardView(seq);
    model.addAttribute(MODEL_BOARD, board);
    model.addAttribute(MODEL_REPLY_BOARD, new Board());
    return VIEW_BOARD_REPLY;
  }

  @PostMapping("/reply/insert")
  public String insertReplyBoard(
      @RequestPart(value = "file", required = false) List<MultipartFile> fileList,
      @RequestParam(required = false) Map<String, Object> map,
      Model model) throws Exception {
    Map<String, Object> processedMap = processReplyBoardMap(map);
    boardService.insertReplyBoard(fileList, processedMap);
    return REDIRECT_LIST;
  }

  @PostMapping("/remove")
  public String deleteBoard(@RequestParam Integer seq) throws Exception {
    boardService.deleteBoard(seq);
    return REDIRECT_LIST;
  }

  @GetMapping("/tree")
  public String getTreeView(@RequestParam(required = false) Map<String, Object> map, Model model) {
    return VIEW_BOARD_TREE;
  }

  /**
   * 조직 트리 데이터 조회 API
   */
  @GetMapping("/tree/api")
  @ResponseBody
  public List<TreeNode> getTreeData() throws Exception {
    return organizationService.getTree();
  }

  /**
   * 새 조직 추가 API
   */
  @PostMapping("/tree/api/add")
  @ResponseBody
  public Map<String, Object> addOrganization(@RequestParam String orgName,
      @RequestParam(required = false) Long parentOrgId) throws Exception {
    Long newOrgId = organizationService.addOrganization(orgName, parentOrgId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("orgId", newOrgId);
    result.put("message", "조직이 추가되었습니다.");
    return result;
  }

  /**
   * 조직 이동 API
   */
  @PostMapping("/tree/api/move")
  @ResponseBody
  public Map<String, Object> moveOrganization(
      @RequestParam Long orgId,
      @RequestParam(required = false) Long newParentId) throws Exception {
    organizationService.moveOrganization(orgId, newParentId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "조직이 이동되었습니다.");
    return result;
  }

  /**
   * 답글 게시글 맵 처리 (display 기본값 설정)
   */
  private Map<String, Object> processReplyBoardMap(Map<String, Object> map) {
    Map<String, Object> processedMap = Optional.ofNullable(map).orElse(new HashMap<>());
    processedMap.put("display", Optional.ofNullable((Boolean) processedMap.get("display")).orElse(false));
    return processedMap;
  }
}
