-- 조직도 샘플 데이터

-- 1. organization 테이블에 조직 정보 INSERT
INSERT INTO organization (org_id, org_name) VALUES (1, 'CEO');
INSERT INTO organization (org_id, org_name) VALUES (2, '개발팀');
INSERT INTO organization (org_id, org_name) VALUES (3, '백엔드');
INSERT INTO organization (org_id, org_name) VALUES (4, '영업팀');

-- 2. organization_closure 테이블에 조직 관계 INSERT
-- Closure Table 패턴: 각 노드는 자기 자신을 depth=0으로 저장하고, 부모-자식 관계를 저장

-- CEO (1) - ROOT 노드
-- 자기 자신
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (1, 1, 0);

-- 개발팀 (2) - CEO의 자식
-- 자기 자신
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (2, 2, 0);
-- CEO의 자식 (depth=1)
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (1, 2, 1);

-- 백엔드 (3) - 개발팀의 자식, CEO의 손자
-- 자기 자신
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (3, 3, 0);
-- 개발팀의 자식 (depth=1)
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (2, 3, 1);
-- CEO의 손자 (depth=2)
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (1, 3, 2);

-- 영업팀 (4) - CEO의 자식
-- 자기 자신
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (4, 4, 0);
-- CEO의 자식 (depth=1)
INSERT INTO organization_closure (ancestor, descendant, depth) VALUES (1, 4, 1);

-- 테스트 멤버 데이터
INSERT INTO member (member_id, password, name, email, reg_date, delete_yn) 
VALUES ('admin', 'admin123', '관리자', 'admin@test.com', CURRENT_TIMESTAMP, false);

INSERT INTO member (member_id, password, name, email, reg_date, delete_yn) 
VALUES ('user1', 'user123', '사용자1', 'user1@test.com', CURRENT_TIMESTAMP, false);

INSERT INTO member (member_id, password, name, email, reg_date, delete_yn) 
VALUES ('test', 'test123', '테스트', 'test@test.com', CURRENT_TIMESTAMP, false);

