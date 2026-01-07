package com.board.thymeleaf.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.thymeleaf.domain.Organization;
import com.board.thymeleaf.domain.TreeNode;
import com.board.thymeleaf.repository.OrganizationRepo;
import com.board.thymeleaf.service.ifc.OrganizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "boardTxManager", rollbackFor = {Exception.class})
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationRepo organizationRepo;

  @Override
  public List<TreeNode> getTree() throws Exception {
    // 1. 모든 조직 조회
    List<Organization> organizations = organizationRepo.findAll();
    
    if (organizations == null || organizations.isEmpty()) {
      return new ArrayList<>();
    }

    // 2. 각 조직의 부모 ID 조회하여 맵에 저장
    Map<Long, Long> parentMap = new HashMap<>();
    for (Organization org : organizations) {
      Long parentId = organizationRepo.findParentId(org.getOrgId());
      if (parentId != null) {
        parentMap.put(org.getOrgId(), parentId);
      }
    }

    // 3. TreeNode 리스트로 변환
    List<TreeNode> treeNodes = new ArrayList<>();
    for (Organization org : organizations) {
      TreeNode node = createTreeNode(org, parentMap);
      treeNodes.add(node);
    }

    return treeNodes;
  }

  @Override
  @Transactional(readOnly = false)
  public Long addOrganization(String orgName, Long parentOrgId) throws Exception {
    // 1. 다음 조직 ID 조회
    Long newOrgId = organizationRepo.getNextOrgId();
    
    // 2. 조직 정보 추가
    Organization organization = new Organization();
    organization.setOrgId(newOrgId);
    organization.setOrgName(orgName);
    organizationRepo.insertOrganization(organization);
    
    // 3. 자기 자신 연결 추가 (depth=0)
    organizationRepo.insertSelfRelation(newOrgId);
    
    // 4. 부모가 있으면 부모와의 관계 추가
    if (parentOrgId != null) {
      organizationRepo.insertParentRelations(newOrgId, parentOrgId);
    }
    
    return newOrgId;
  }

  @Override
  @Transactional(readOnly = false)
  public void moveOrganization(Long orgId, Long newParentId) throws Exception {
    // 1. 기존 부모 관계 제거 (자기 자신 제외)
    organizationRepo.deleteParentRelations(orgId);
    
    // 2. 새 부모로 이동 (새 부모의 모든 조상과의 관계 추가)
    if (newParentId != null) {
      organizationRepo.moveToNewParent(orgId, newParentId);
    }
    // newParentId가 null이면 루트로 이동 (부모 관계만 제거하면 됨)
  }

  /**
   * Organization을 TreeNode로 변환
   */
  private TreeNode createTreeNode(Organization org, Map<Long, Long> parentMap) {
    TreeNode node = new TreeNode();
    
    // 노드 ID 설정
    node.setId(String.valueOf(org.getOrgId()));
    
    // 부모 설정 (부모가 있으면 부모 ID, 없으면 "#"는 루트)
    Long parentId = parentMap.get(org.getOrgId());
    node.setParent(parentId != null ? String.valueOf(parentId) : "#");
    
    // 표시 텍스트 설정
    node.setText(org.getOrgName());
    
    // 추가 데이터 설정
    TreeNode.TreeNodeData data = new TreeNode.TreeNodeData();
    data.setOrgId(org.getOrgId());
    data.setOrgName(org.getOrgName());
    node.setData(data);
    
    return node;
  }
}

