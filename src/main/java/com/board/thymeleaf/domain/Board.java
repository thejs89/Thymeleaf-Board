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
@Alias("board")
public class Board {
  private int seq;
  private String title;
  private String content;
  private boolean display;
  private int groupId;
  private int groupOrder;
  private int depth;
  private boolean deleteYn;
  private Date regDate;
  private String regId;
  private Date updDate;
  private String updId;
}
