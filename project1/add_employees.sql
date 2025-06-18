USE moviedb;

DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
    email VARCHAR(50) NOT NULL DEFAULT '',
    `password` VARCHAR(20) NOT NULL DEFAULT '',
    fullname VARCHAR(100),
    PRIMARY KEY (email)
);

INSERT INTO employees (email, `password`, fullname)
VALUES ('classta@email.edu', 'classta', 'TA CS122B');
COMMIT;