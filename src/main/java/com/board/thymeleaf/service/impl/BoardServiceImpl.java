package com.board.thymeleaf.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.board.thymeleaf.domain.Board;
import com.board.thymeleaf.domain.BoardFile;
import com.board.thymeleaf.domain.PageBoard;
import com.board.thymeleaf.repository.BoardFileRepo;
import com.board.thymeleaf.repository.BoardRepo;
import com.board.thymeleaf.service.ifc.BoardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

  private static final String DEFAULT_USER_ID = "jsjeon";
  private static final String DEFAULT_UPLOAD_PATH = "board";
  private static final String TEMP_FILE_PREFIX = "777";
  private static final int DEFAULT_GROUP_ORDER = 0;
  private static final int DEFAULT_DEPTH = 0;

  private final BoardRepo boardRepo;
  private final BoardFileRepo boardFileRepo;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${path.upload}")
  private String UPLOAD_PATH;

  @Transactional(readOnly = true)
  @Override
  public List<PageBoard> getBoardList(Map<String, Object> map) throws Exception {
    return boardRepo.getBoardList(map);
  }

  @Transactional(readOnly = false)
  @Override
  public void insertBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    Board board = convertToBoard(map);
    setDefaultBoardValues(board);
    boardRepo.insertBoard(board);

    uploadFiles(board.getSeq(), fileList, map);
  }

  @Transactional(readOnly = true)
  @Override
  public Board getBoardView(Integer seq) throws Exception {
    return boardRepo.getBoardView(seq);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BoardFile> getBoardFileList(Integer boardSeq) throws Exception {
    return boardFileRepo.getBoardFileList(boardSeq);
  }

  @Transactional(readOnly = false)
  @Override
  public void insertReplyBoard(List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    String parentSeqStr = (String) map.get("parentSeq");
    if (parentSeqStr == null) {
      throw new IllegalArgumentException("parentSeq는 필수입니다.");
    }

    Board parentBoard = boardRepo.getBoardView(Integer.parseInt(parentSeqStr));
    if (parentBoard == null) {
      throw new IllegalArgumentException("부모 게시글이 존재하지 않습니다.");
    }

    updateGroupOrder(parentBoard.getGroupId(), parentBoard.getGroupOrder());
    setDefaultReplyBoardValues(map);
    boardRepo.insertReplyBoard(map);
    
    // insertReplyBoard 후 selectKey로 seq가 map에 설정됨
    Integer replySeq = (Integer) map.get("seq");
    if (replySeq != null) {
      uploadFiles(replySeq, fileList, map);
    }
  }

  @Transactional(readOnly = false)
  @Override
  public void deleteBoard(Integer seq) throws Exception {
    boardRepo.deleteBoard(seq);
  }

  /**
   * Map을 Board 객체로 변환
   */
  private Board convertToBoard(Map<String, Object> map) {
    return objectMapper.convertValue(map, Board.class);
  }

  /**
   * 게시글 기본값 설정
   */
  private void setDefaultBoardValues(Board board) {
    Date now = new Date();
    board.setGroupOrder(DEFAULT_GROUP_ORDER);
    board.setDepth(DEFAULT_DEPTH);
    board.setDeleteYn(false);
    board.setRegDate(now);
    board.setRegId(DEFAULT_USER_ID);
    board.setUpdDate(now);
    board.setUpdId(DEFAULT_USER_ID);
  }

  /**
   * 답글 게시글 기본값 설정
   */
  private void setDefaultReplyBoardValues(Map<String, Object> map) {
    Date now = new Date();
    map.put("deleteYn", false);
    map.put("regDate", now);
    map.put("regId", DEFAULT_USER_ID);
    map.put("updDate", now);
    map.put("updId", DEFAULT_USER_ID);
  }

  /**
   * 파일 업로드 처리
   */
  private void uploadFiles(Integer boardSeq, List<MultipartFile> fileList, Map<String, Object> map) throws Exception {
    List<MultipartFile> validFileList = filterValidFiles(fileList);
    if (validFileList.isEmpty()) { 
      return;
    }

    List<Map<String, Object>> fileInfoList = parseFileInfoList(map, validFileList.size());
    Map<String, Object> baseFileInfo = parseBaseFileInfo(map);

    List<Map<String, Object>> fileUploadParams = createFileUploadParams(boardSeq, validFileList, fileInfoList, baseFileInfo);
    
    processFileUploads(fileUploadParams);
  }

  /**
   * 유효한 파일만 필터링
   */
  private List<MultipartFile> filterValidFiles(List<MultipartFile> fileList) {
    if (fileList == null || fileList.isEmpty()) {
      return new ArrayList<>();
    }
    return fileList.stream()
        .filter(file -> file != null && !file.isEmpty())
        .collect(Collectors.toList());
  }

  /**
   * 파일 정보 리스트 파싱
   */
  private List<Map<String, Object>> parseFileInfoList(Map<String, Object> map, int fileCount) {
    return Optional.ofNullable(map)
        .map(m -> m.get("fileInfo"))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(json -> parseJsonToList(json))
        .filter(list -> list != null && list.size() == fileCount)
        .orElseGet(() -> createEmptyFileInfoList(fileCount));
  }

  /**
   * JSON 문자열을 List로 파싱
   */
  private List<Map<String, Object>> parseJsonToList(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    } catch (Exception e) {
      log.warn("파일 정보 JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 빈 파일 정보 리스트 생성
   */
  private List<Map<String, Object>> createEmptyFileInfoList(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> new HashMap<String, Object>())
        .collect(Collectors.toList());
  }

  /**
   * 기본 파일 정보 파싱
   */
  private Map<String, Object> parseBaseFileInfo(Map<String, Object> map) {
    return Optional.ofNullable(map)
        .map(m -> m.get("baseFileInfo"))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(json -> parseJsonToMap(json))
        .orElseGet(HashMap::new);
  }

  /**
   * JSON 문자열을 Map으로 파싱
   */
  private Map<String, Object> parseJsonToMap(String json) {
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      log.warn("기본 파일 정보 JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 파일 업로드 파라미터 생성
   */
  private List<Map<String, Object>> createFileUploadParams(
      Integer boardSeq,
      List<MultipartFile> validFileList,
      List<Map<String, Object>> fileInfoList,
      Map<String, Object> baseFileInfo) {
    
    return IntStream.range(0, validFileList.size())
        .mapToObj(i -> {
          MultipartFile file = validFileList.get(i);
          Map<String, Object> fileInfo = fileInfoList.get(i);
          return ImmutableMap.<String, Object>builder()
              .putAll(baseFileInfo)
              .putAll(fileInfo)
              .put("seq", boardSeq)
              .put("file", file)
              .put("userId", DEFAULT_USER_ID)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 파일 업로드 처리 및 검증
   */
  private void processFileUploads(List<Map<String, Object>> fileUploadParams) throws Exception {
    for (Map<String, Object> params : fileUploadParams) {
      Map<String, Object> result = uploadFile(params);
      if (!Boolean.TRUE.equals(result.get("result"))) {
        String fileName = extractFileNameFromResult(result);
        throw new RuntimeException("파일 업로드 실패: " + fileName);
      }
    }
  }

  /**
   * 결과에서 파일명 추출
   */
  private String extractFileNameFromResult(Map<String, Object> result) {
    Map<String, Object> fileInfo = (Map<String, Object>) result.get("file");
    return fileInfo != null ? (String) fileInfo.get("fileName") : "알 수 없는 파일";
  }

  /**
   * 단일 파일 업로드 처리
   */
  private Map<String, Object> uploadFile(Map<String, Object> params) {
    MultipartFile file = (MultipartFile) params.get("file");
    if (file == null) {
      return createFailureResult("파일이 null입니다.", null, null);
    }

    String fileName = file.getOriginalFilename();
    if (fileName == null || fileName.isEmpty()) {
      return createFailureResult("파일명이 없습니다.", null, null);
    }

    Integer fileSize = ((Long) file.getSize()).intValue();
    String uploadPath = Optional.ofNullable((String) params.get("path"))
        .filter(path -> !path.isEmpty())
        .orElse(DEFAULT_UPLOAD_PATH);

    try {
      String uploadName = saveFileToDisk(file, uploadPath, fileName);
      BoardFile boardFile = createBoardFile(params, fileName, fileSize, uploadPath, uploadName);
      boardFileRepo.insertBoardFile(boardFile);

      return createSuccessResult(fileName, fileSize, uploadPath, uploadName, boardFile.getFileSeq());
    } catch (Exception e) {
      log.error("파일 업로드 실패: {}", fileName, e);
      return createFailureResult(fileName, fileSize, uploadPath);
    }
  }

  /**
   * 파일을 디스크에 저장
   */
  private String saveFileToDisk(MultipartFile file, String relativePath, String fileName) throws Exception {
    String extension = com.google.common.io.Files.getFileExtension(fileName);
    String suffix = String.format(".%s", extension);

    Path targetPath = Paths.get(UPLOAD_PATH, relativePath);
    if (!java.nio.file.Files.exists(targetPath)) {
      java.nio.file.Files.createDirectories(targetPath);
    }

    File saveFile = File.createTempFile(
        TEMP_FILE_PREFIX,
        suffix,
        new File(UPLOAD_PATH + "/" + relativePath));
    file.transferTo(saveFile);

    return saveFile.getName();
  }

  /**
   * BoardFile 엔티티 생성
   */
  private BoardFile createBoardFile(
      Map<String, Object> params,
      String fileName,
      Integer fileSize,
      String uploadPath,
      String uploadName) {
    
    Integer boardSeq = (Integer) params.get("seq");
    String userId = Optional.ofNullable((String) params.get("userId")).orElse(DEFAULT_USER_ID);
    Date now = new Date();

    BoardFile boardFile = new BoardFile();
    boardFile.setBoardSeq(boardSeq);
    boardFile.setFileName(fileName);
    boardFile.setFileSize(fileSize);
    boardFile.setUploadName(uploadName);
    boardFile.setUploadPath(uploadPath);
    boardFile.setDeleteYn(false);
    boardFile.setRegDate(now);
    boardFile.setRegId(userId);
    boardFile.setUpdDate(now);
    boardFile.setUpdId(userId);

    return boardFile;
  }

  /**
   * 성공 결과 생성
   */
  private Map<String, Object> createSuccessResult(
      String fileName, Integer fileSize, String uploadPath, String uploadName, Integer fileSeq) {
    return ImmutableMap.of(
        "result", true,
        "file", ImmutableMap.builder()
            .put("fileName", fileName)
            .put("fileSize", fileSize)
            .put("uploadPath", uploadPath)
            .put("uploadName", uploadName)
            .put("fileSeq", fileSeq)
            .build());
  }

  /**
   * 실패 결과 생성
   */
  private Map<String, Object> createFailureResult(String fileName, Integer fileSize, String uploadPath) {
    return ImmutableMap.of(
        "result", false,
        "file", ImmutableMap.builder()
            .put("fileName", fileName != null ? fileName : "알 수 없는 파일")
            .put("fileSize", fileSize != null ? fileSize : 0)
            .put("uploadPath", uploadPath != null ? uploadPath : "")
            .build());
  }

  /**
   * 그룹 순서 업데이트
   */
  private void updateGroupOrder(Integer groupId, Integer groupOrder) {
    Map<String, Object> params = new HashMap<>();
    params.put("groupId", groupId);
    params.put("groupOrder", groupOrder);
    boardRepo.updateGroupOrd(params);
  }
}
