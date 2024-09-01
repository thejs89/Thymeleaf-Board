package com.board.thymeleaf.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Pager<T extends PageVO> {

  @Builder.Default
  private Integer totalCount = 0;
  @Builder.Default
  private Integer currentPage = 0;
  @Builder.Default
  private Integer totalPage = 0;
  @Builder.Default
  private List<T> contents = new ArrayList<>();

  public Pager(List<T> contents) {
    this.contents = Optional.ofNullable(contents).filter(list -> list.size() > 0).orElse(new ArrayList<>());
    if (this.contents.size() == 0) {
      this.totalCount = 0;
      this.currentPage = 0;
      this.totalPage = 1;
    } else {
      T item = contents.get(0);
      this.totalCount = item.getTotalCount();
      this.currentPage = item.getCurrentPage();
      this.totalPage =  (int) Math.ceil(item.getTotalCount() * 1.0  / item.getSize());
    }
  }
  public static <P extends PageVO> Pager<P> formList(List<P> contents) {
    return new Pager<P>(contents);
  }

}
