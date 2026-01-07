package com.board.thymeleaf.service.ifc;

import java.util.List;

import com.board.thymeleaf.domain.TreeNode;

public interface OrganizationService {
  
  /**
   * 조직 트리 구조 조회
   * @return jstree 형식의 트리 노드 목록
   */
  List<TreeNode> getTree() throws Exception;
  
  /**
   * 새 조직 추가
   * @param orgName 조직명
   * @param parentOrgId 부모 조직 ID (null이면 루트에 추가)
   * @return 생성된 조직 ID
   */
  Long addOrganization(String orgName, Long parentOrgId) throws Exception;
  
  /**
   * 조직 이동 (다른 부모로 이동)
   * @param orgId 이동할 조직 ID
   * @param newParentId 새 부모 조직 ID (null이면 루트로 이동)
   */
  void moveOrganization(Long orgId, Long newParentId) throws Exception;
}

