package com.board.thymeleaf.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.board.thymeleaf.config.BoardMapper;
import com.board.thymeleaf.domain.Organization;

@BoardMapper
@Repository
public interface OrganizationRepo {
  
  /**
   * 모든 조직 조회
   */
  List<Organization> findAll();
  
  /**
   * 특정 조직의 부모 조직 ID 조회 (depth=1인 직접 부모만)
   */
  Long findParentId(Long orgId);
  
  /**
   * 다음 조직 ID 조회 (새 조직 추가 시 사용)
   */
  Long getNextOrgId();
  
  /**
   * 조직 추가
   */
  void insertOrganization(Organization organization);
  
  /**
   * 조직 자기 자신 연결 추가 (depth=0)
   */
  void insertSelfRelation(Long orgId);
  
  /**
   * 부모와의 관계 추가 (부모의 모든 조상과의 관계 포함)
   */
  void insertParentRelations(Long newOrgId, Long parentOrgId);
  
  /**
   * 조직의 기존 부모 관계 제거 (자기 자신 제외)
   */
  void deleteParentRelations(Long orgId);
  
  /**
   * 조직 이동 (새 부모로 이동)
   */
  void moveToNewParent(Long orgId, Long newParentId);
}

