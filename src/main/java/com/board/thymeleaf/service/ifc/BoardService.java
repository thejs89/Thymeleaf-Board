package com.board.thymeleaf.service.ifc;

import java.util.List;

import com.board.thymeleaf.domain.Board;

public interface BoardService {
  List<Board> getBoardList() throws Exception;
  
}
