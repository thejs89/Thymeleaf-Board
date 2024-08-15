package com.board.thymeleaf.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.board.thymeleaf.config.BoardMapper;
import com.board.thymeleaf.domain.Board;

@BoardMapper
@Repository
public interface BoardRepo {

  List<Board> getBoardList();
  
}
