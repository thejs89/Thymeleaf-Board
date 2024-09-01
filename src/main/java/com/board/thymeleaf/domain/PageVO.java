package com.board.thymeleaf.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class PageVO {

	@Builder.Default
	private Integer rnum = 0;
	@Builder.Default
	@JsonIgnore
	private Integer totalCount = 0;
	@Builder.Default
	@JsonIgnore
	private Integer currentPage = 0;
	@Builder.Default
	@JsonIgnore
	private Integer totalPage = 0;
	@Builder.Default
	@JsonIgnore
	private Integer size = 0;
}
