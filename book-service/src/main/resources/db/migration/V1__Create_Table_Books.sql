CREATE TABLE book
(
    id          SERIAL PRIMARY KEY,
    author      TEXT,
    launch_date TIMESTAMP      NOT NULL,
    price       NUMERIC(65, 2) NOT NULL,
    title       TEXT
);