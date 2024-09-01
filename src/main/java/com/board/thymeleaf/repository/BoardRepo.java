package com.board.thymeleaf.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.board.thymeleaf.config.BoardMapper;
import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;

@BoardMapper
@Repository
public interface BoardRepo {

  List<PageBoard> getBoardList(Map<String,Object> map);
  Integer insertBoard(Board board);
  Board getBoardView(Integer seq);
  
}
