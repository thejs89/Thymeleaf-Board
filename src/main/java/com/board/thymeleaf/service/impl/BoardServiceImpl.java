package com.board.thymeleaf.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.repository.BoardRepo;
import com.board.thymeleaf.service.ifc.BoardService;

import groovyjarjarantlr4.v4.parse.ANTLRParser.notSet_return;
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

  @Transactional(readOnly = false)
  @Override
  public void insertBoard(Board board) throws Exception {
    Date date = new Date();

    board.setGroupOrder(0);
    board.setDepth(0);
    board.setDeleteYn(false);
    board.setRegDate(date);
    board.setRegId("jsjeon");
    board.setUpdDate(date);
    board.setUpdId("jsjeon");
    
    boardRepo.insertBoard(board);
  }

  @Transactional(readOnly = true)
  @Override
  public Board getBoardView(Integer seq) throws Exception {
    return boardRepo.getBoardView(seq);
  }

  @Transactional(readOnly = false)
  @Override
  public void insertReplyBoard(Map<String,Object> map) throws Exception {
    
    Board info = boardRepo.getBoardView(Integer.parseInt((String)map.get("parentSeq")));
    
    if (info == null) return;

    Map<String,Object> po = new HashMap<String,Object>();
    po.put("groupId", info.getGroupId());
    po.put("groupOrder", info.getGroupOrder());
    boardRepo.updateGroupOrd(po);

    Date date = new Date();
    map.put("deleteYn", false);
    map.put("regDate", date);
    map.put("regId", "jsjeon");
    map.put("updDate", date);
    map.put("updId", "jsjeon");
    boardRepo.insertReplyBoard(map);

  }
  
}
