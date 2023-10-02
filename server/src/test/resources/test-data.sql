BEGIN TRANSACTION;

DROP TABLE IF EXISTS transfer, tenmo_user, account;

DROP SEQUENCE IF EXISTS seq_transfer_id, seq_user_id, seq_account_id;

-- Sequence to start user_id values at 1001 instead of 1
CREATE SEQUENCE seq_user_id
  INCREMENT BY 1
  START WITH 1001
  NO MAXVALUE;

-- Sequence to start account_id values at 2001 instead of 1
-- Note: Use similar sequences with unique starting values for additional tables
CREATE SEQUENCE seq_account_id
  INCREMENT BY 1
  START WITH 2001
  NO MAXVALUE;

CREATE SEQUENCE seq_transfer_id
  INCREMENT BY 1
  START WITH 3001
  NO MAXVALUE;

CREATE TABLE tenmo_user (
	user_id int NOT NULL DEFAULT nextval('seq_user_id'),
	username varchar(50) NOT NULL,
	password_hash varchar(200) NOT NULL,
	CONSTRAINT PK_tenmo_user PRIMARY KEY (user_id),
	CONSTRAINT UQ_username UNIQUE (username)
);

CREATE TABLE account (
	account_id int NOT NULL DEFAULT nextval('seq_account_id'),
	user_id int NOT NULL,
	balance decimal(13, 2) NOT NULL,
	CONSTRAINT PK_account PRIMARY KEY (account_id),
	CONSTRAINT FK_account_tenmo_user FOREIGN KEY (user_id) REFERENCES tenmo_user (user_id)
);

CREATE TABLE transfer (
	transfer_id int NOT NULL DEFAULT nextval('seq_transfer_id'),
	sending_account_id int NOT NULL,
	receiving_account_id int NOT NULL,
	amount numeric(13, 2) NOT NULL,
	transfer_timestamp TIMESTAMP DEFAULT now() NOT NULL,
	status varchar(50) DEFAULT 'Approved' NOT NULL,
	CONSTRAINT pk_transfer PRIMARY KEY (transfer_id),
	CONSTRAINT fk_transfer_account_sending FOREIGN KEY (sending_account_id) REFERENCES account(account_id),
	CONSTRAINT fk_transfer_account_receiving FOREIGN KEY (receiving_account_id) REFERENCES account(account_id),
	CONSTRAINT chk_sending_receiving_not_same_account CHECK (sending_account_id != receiving_account_id),
	CONSTRAINT chk_amount_greater_than_zero CHECK (amount > 0)
);

INSERT INTO tenmo_user (username, password_hash)
VALUES ('bob', '$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2'),
       ('user', '$2a$10$Ud8gSvRS4G1MijNgxXWzcexeXlVs4kWDOkjE7JFIkNLKEuE57JAEy');

INSERT INTO account (user_id, balance)
VALUES (1001, 1000),
(1002, 1111.11);

INSERT INTO transfer (sending_account_id, receiving_account_id, amount, transfer_timestamp, status)
VALUES(2001,2002,1000, '2018/3/3 03:03:03', 'Approved'),
(2002,2001,500, '2018/3/3 03:03:03', 'Pending');

COMMIT;