CREATE TABLE board (
	seq NUMBER NOT NULL PRIMARY KEY,
	title VARCHAR(200) NOT NULL,
	content VARCHAR(2000) NOT NULL,
	display BIT,
	group_id INT NOT NULL,
	group_order INT NOT NULL,
	depth INT NOT NULL,
	delete_yn BIT,
	reg_date DATETIME,
	reg_id VARCHAR(10),
	upd_date DATETIME,
	upd_id VARCHAR(10)
);

CREATE TABLE board_file (
	file_seq NUMBER NOT NULL PRIMARY KEY,
	board_seq NUMBER NOT NULL,
	file_name VARCHAR(500) NOT NULL,
	file_size INT NOT NULL,
	upload_name VARCHAR(500) NOT NULL,
	upload_path VARCHAR(500) NOT NULL,
	delete_yn BIT,
	reg_date DATETIME,
	reg_id VARCHAR(10),
	upd_date DATETIME,
	upd_id VARCHAR(10),
	FOREIGN KEY (board_seq) REFERENCES board(seq)
);

CREATE TABLE organization (
    org_id      NUMBER PRIMARY KEY,
    org_name    VARCHAR2(100) NOT NULL
);

CREATE TABLE organization_closure (
    ancestor    NUMBER NOT NULL,
    descendant  NUMBER NOT NULL,
    depth       NUMBER NOT NULL,
    CONSTRAINT pk_org_closure PRIMARY KEY (ancestor, descendant),
    CONSTRAINT fk_org_ancestor FOREIGN KEY (ancestor) REFERENCES organization(org_id),
    CONSTRAINT fk_org_descendant FOREIGN KEY (descendant) REFERENCES organization(org_id)
);