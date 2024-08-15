package com.board.thymeleaf.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.repository.BoardRepo;
import com.board.thymeleaf.service.ifc.BoardService;

import lombok.RequiredArgsConstructor;

@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class} )
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

  private final BoardRepo boardRepo;

  @Transactional(readOnly = true)
  @Override
  public List<Board> getBoardList() throws Exception {
    
    List<Board> list = boardRepo.getBoardList();
    return list;

  }
  
}
