package com.board.thymeleaf.domain;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Alias("pageboard")
public class PageBoard extends PageVO {
  private int seq;
  private String title;
  private String content;
  private boolean display;
  private int groupId;
  private int groupOrder;
  private int depth;
  private String regDate;
  private String regId;
  private String updDate;
  private String updId;
  private boolean deleteYn;
}
