package com.board.thymeleaf.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.thymeleaf.domain.PageBoard;
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
  public List<PageBoard> getBoardList(Map<String,Object> map) throws Exception {
    List<PageBoard> list = boardRepo.getBoardList(map);
    return list;

  }
  
}
