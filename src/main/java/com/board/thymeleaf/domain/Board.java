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
@Alias("board")
public class Board {
  private int seq;
  private String title;
  private String content;
  private int groupId;
  private int groupOrder;
  private int depth;
}
