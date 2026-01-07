package com.board.thymeleaf.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * jstree에서 사용하는 트리 노드 형식
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TreeNode {
  private String id;        // 노드 ID (조직 ID)
  private String parent;    // 부모 노드 ID ("#"는 루트)
  private String text;      // 표시할 텍스트 (조직명)
  private TreeNodeData data; // 추가 데이터

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class TreeNodeData {
    private Long orgId;
    private String orgName;
  }
}

