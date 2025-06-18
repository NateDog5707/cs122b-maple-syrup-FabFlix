drop database moviedb;
CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;
SET GLOBAL autocommit = 0;

-- DROP TABLE IF EXISTS ratings;
-- DROP TABLE IF EXISTS sales;
-- DROP TABLE IF EXISTS customers;
-- DROP TABLE IF EXISTS creditcards;
-- DROP TABLE IF EXISTS genres_in_movies;
-- DROP TABLE IF EXISTS genres;
-- DROP TABLE IF EXISTS stars_in_movies;
-- DROP TABLE IF EXISTS stars;
-- DROP TABLE IF EXISTS movies;

CREATE TABLE movies(
    id VARCHAR(10) NOT NULL DEFAULT '',
    title VARCHAR(100) NOT NULL DEFAULT '',
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL DEFAULT '',
    PRIMARY KEY (id)
);
ALTER TABLE movies ADD FULLTEXT(title);
CREATE TABLE stars(
    id VARCHAR(10) NOT NULL DEFAULT '',
    name VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE stars_in_movies(
    starId VARCHAR(10) NOT NULL DEFAULT '',
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE genres(
    id INTEGER NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL DEFAULT '',
    PRIMARY KEY(id)
);

CREATE TABLE genres_in_movies(
    genreId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES  movies(id)
);

CREATE TABLE creditcards(
    id VARCHAR(20) NOT NULL DEFAULT '',
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    expiration DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE customers(
    id INTEGER NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    ccId VARCHAR(20) NOT NULL DEFAULT '',
    address VARCHAR(200) NOT NULL DEFAULT '',
    email VARCHAR(50) NOT NULL DEFAULT '',
    password VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);
CREATE TABLE sales(
    id INTEGER NOT NULL AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    saleDate DATE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE ratings(
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id)
);