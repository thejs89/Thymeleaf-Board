package com.board.thymeleaf.service.ifc;

import java.util.List;
import java.util.Map;

import com.board.thymeleaf.domain.PageBoard;

public interface BoardService {
  List<PageBoard> getBoardList(Map<String,Object> map) throws Exception;
  
}
