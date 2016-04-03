CREATE DATABASE bd_db;
USE bd_db;

CREATE TABLE Word (
	word_id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	word VARCHAR(64) NOT NULL
);

CREATE TABLE Meaning (
	meaning_id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	meaning VARCHAR(64) NOT NULL,
	meaningExample VARCHAR(255)
);

CREATE TABLE WordMeaning (
	word_meaning_id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	word_id INT(6) UNSIGNED,
	meaning_id INT(6) UNSIGNED,
	word_meaning_score INT(4) UNSIGNED,
	FOREIGN KEY (word_id) REFERENCES Word (word_id),
	FOREIGN KEY (meaning_id) REFERENCES Meaning (meaning_id)
);

/* sample values */
insert into Word ( word ) Values ( "achieve" );
insert into Meaning ( meaning, meaningExample ) Values ( "Arjon", "Amar kono arjon nei" );
insert into WordMeaning (word_id, meaning_id, word_meaning_score) Values (1, 1, 3);
