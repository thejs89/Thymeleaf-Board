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

