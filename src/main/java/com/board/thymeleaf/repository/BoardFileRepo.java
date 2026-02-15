package com.board.thymeleaf.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.board.thymeleaf.config.BoardMapper;
import com.board.thymeleaf.domain.BoardFile;

@BoardMapper
@Repository
public interface BoardFileRepo {

  Integer insertBoardFile(BoardFile boardFile);
  List<BoardFile> getBoardFileList(Integer boardSeq);
  Integer deleteBoardFile(Integer fileSeq);
  
}

