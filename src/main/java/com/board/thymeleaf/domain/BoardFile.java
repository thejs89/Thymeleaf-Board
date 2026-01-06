package com.board.thymeleaf.domain;

import java.util.Date;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Alias("boardFile")
public class BoardFile {
  private int fileSeq;
  private int boardSeq;
  private String fileName;
  private int fileSize;
  private String uploadName;
  private String uploadPath;
  private boolean deleteYn;
  private Date regDate;
  private String regId;
  private Date updDate;
  private String updId;
}

