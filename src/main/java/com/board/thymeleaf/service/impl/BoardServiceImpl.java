package com.board.thymeleaf.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.codehaus.groovy.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.repository.BoardRepo;
import com.board.thymeleaf.service.ifc.BoardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

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
  @SuppressWarnings("unchecked")
  public void insertBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    // board insert
    Date date = new Date();

    ObjectMapper mapper = new ObjectMapper();
    Board board = mapper.convertValue(map, Board.class);

    board.setGroupOrder(0);
    board.setDepth(0);
    board.setDeleteYn(false);
    board.setRegDate(date);
    board.setRegId("jsjeon");
    board.setUpdDate(date);
    board.setUpdId("jsjeon");
    
    boardRepo.insertBoard(board);

    //file upload
    Integer seq = board.getSeq();

    List<Map<String,Object>> fileInfoList = Optional.ofNullable(map)
    .map(m -> m.get("fileInfo"))
    .filter(String.class::isInstance)
    .map(String.class::cast)
    .map(json -> {
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return null;
        }
    })
    .filter(l -> l != null && l.size() == fileList.size())
    .orElseGet(() ->
        IntStream.range(0, fileList.size()).mapToObj(i -> new HashMap<String, Object>()).collect(Collectors.toList())
    );

    Map<String,Object> baseFileInfo = Optional.ofNullable(map)
    .map(m -> m.get("baseFileInfo"))
    .filter(String.class::isInstance)
    .map(String.class::cast)
    .map(json -> {
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    })
    .orElseGet(HashMap::new);

    List<Map<String, Object>> poList = IntStream.range(0, fileList.size()).boxed().map(i -> {
    MultipartFile file = fileList.get(i);
    Map<String, Object> fileInfo = fileInfoList.get(i);
    return ImmutableMap.<String, Object>builder()
      .putAll(baseFileInfo)
      .putAll(fileInfo)
      .put("seq", seq)
      .put("file", file)
      .put("userId", "jsjeon")
      .build();
    }).collect(Collectors.toList());

    System.out.println("poList======"+poList);

    //file remove

  }

  @Value("${path.upload}")
  private String UPLOAD_PATH;
  
  private Map<String, Object> uploadImagesFileOne(Map<String, Object> po) {

    MultipartFile file =  (MultipartFile) po.get("file");

    String fileName = file.getOriginalFilename();
    Integer fileSize = ((Long) file.getSize()).intValue();
    String uploadPath = (String) po.get("path");
    
    String ext = Files.getFileExtension(fileName);
    String suffix = String.format(".%s", ext);


    try {

      Path TempuploadPath = Paths.get(UPLOAD_PATH,uploadPath);
      if (!TempuploadPath.toFile().exists()) {
        TempuploadPath.toFile().mkdirs();
      }


    } catch(Exception e) {

      return ImmutableMap.of(
          "result", false,
          "file", ImmutableMap.builder()
              .put("fileName", fileName)
              .put("fileSize", fileSize)
              .put("uploadPath", uploadPath)
              .build());
    }

      return ImmutableMap.of(
          "result", false,
          "file", ImmutableMap.builder()
              .put("fileName", fileName)
              .put("fileSize", fileSize)
              .put("uploadPath", uploadPath)
              .build());

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
  
  @Transactional(readOnly = false)
  @Override
  public void deleteBoard(Integer seq) throws Exception {
    boardRepo.deleteBoard(seq);
  }
  
}
