CREATE TABLE books
(
    id               UUID PRIMARY KEY,
    title            VARCHAR(150) NOT NULL,
    author           VARCHAR(100) NOT NULL,
    publisher        VARCHAR(100) NOT NULL,
    publication_year INTEGER      NOT NULL,
    price       NUMERIC(65, 2) NOT NULL,
    review           TEXT
);