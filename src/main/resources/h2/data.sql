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
-- 비밀번호는 BCrypt로 암호화되어 저장됩니다.
-- 
-- 주의: 아래 해시는 예시입니다. 실제 해시를 생성하려면 
-- PasswordHashGenerator 클래스를 실행하거나 PasswordEncoderUtil을 사용하세요.
-- 
-- 원본 비밀번호: admin123
-- BCrypt 해시 생성 방법:
--   1. PasswordHashGenerator.main() 실행
--   2. 또는 PasswordEncoderUtil.encode("admin123") 사용
--   3. 생성된 해시를 아래에 복사
INSERT INTO member (member_id, password, name, email, role, reg_date, delete_yn) 
VALUES ('admin', '$2a$10$i.FhsN5/E7E1q5tg2bdUIOJMtDjSR6JX6.MpC2cV8eJ0agdeJ2FsG', '관리자', 'admin@test.com', 'ROLE_ADMIN', CURRENT_TIMESTAMP, false);

-- 원본 비밀번호: user123
INSERT INTO member (member_id, password, name, email, role, reg_date, delete_yn) 
VALUES ('user1', '$2a$10$FoFCzhmwsg5TbbBwebvgTeoB7W3vVVRzrUVaKKj3qw4UPMs4ZTrze', '사용자1', 'user1@test.com', 'ROLE_USER', CURRENT_TIMESTAMP, false);

-- 원본 비밀번호: test123
INSERT INTO member (member_id, password, name, email, role, reg_date, delete_yn) 
VALUES ('test', '$2a$10$gPC4Uv/7Ny8bz6WbAAKrSOnVdY.qpDDNID2jHqOhbMoTI3HI9zw82', '테스트', 'test@test.com', 'ROLE_USER', CURRENT_TIMESTAMP, false);

-- 참고: BCrypt 해시는 매번 실행 시 다른 값이 생성되지만, 
-- 같은 평문 비밀번호는 모두 matches()로 검증 가능합니다.

