package com.board.thymeleaf.service.ifc;

import java.util.List;
import java.util.Map;

import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;

public interface BoardService {
  List<PageBoard> getBoardList(Map<String,Object> map) throws Exception;
  void insertBoard(Board board) throws Exception;
  Board getBoardView(Integer seq) throws Exception;
  void insertReplyBoard(Map<String,Object> map) throws Exception;
  void deleteBoard(Integer seq) throws Exception;
   
}
