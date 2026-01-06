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

