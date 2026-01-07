package com.board.thymeleaf.domain;

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
@Alias("organization")
public class Organization {
  private Long orgId;
  private String orgName;
}

