DELIMITER //

CREATE PROCEDURE add_movie(
	IN movie_id varchar(10),
    IN movie_title varchar(100),
    IN movie_year int,
    IN movie_director varchar(100),
    IN star_id varchar(10),
    IN star_name varchar(100),
    IN star_birth_year int,
    IN genre_id int,
    IN genre_name varchar(32),
    OUT success int
)
sp: BEGIN
	
    DECLARE movie_exists INT;
    DECLARE movie_price INT;
    DECLARE genre_exists INT;
    DECLARE star_id_existing varchar(10) DEFAULT NULL;
    DECLARE genre_id_existing INT DEFAULT NULL;

   -- create price
    SET movie_price = FLOOR(1 + (RAND(CAST(SUBSTRING(movie_id, LENGTH(movie_id) - 6, 7) AS SIGNED))) * 20);
    
    -- check if movie exists. if it does, exit as unsuccessful 
    SELECT COUNT(*) INTO movie_exists
    FROM movies
    WHERE title = movie_title AND year = movie_year AND director = movie_director;
	
    IF movie_exists > 0 THEN
        SET success = 0;
        LEAVE sp;
    END IF;
    
     -- insert movie
    INSERT INTO movies (id, title, year, director, price)
    VALUES (movie_id, movie_title, movie_year, movie_director, movie_price);
    
    -- check if genre exists. if it doesn't, add to genres. if is does, do nothing
    SELECT id INTO genre_id_existing 
	FROM genres 
	WHERE name = genre_name 
	LIMIT 1;

	IF genre_id_existing IS NULL THEN
		INSERT INTO genres (id, name)
		VALUES (genre_id, genre_name);
		SET genre_id_existing = genre_id;
	END IF;

	-- use genre_id_existing to link
	INSERT INTO genres_in_movies (genreId, movieId)
	VALUES (genre_id_existing, movie_id);
    
    -- check if star exists. if they don't, add to stars. if they do, do nothing
    
	SELECT id INTO star_id_existing 
    FROM stars 
    WHERE name = star_name LIMIT 1;

	IF star_id_existing IS NULL THEN
		INSERT INTO stars (id, name, birthYear) 
        VALUES (star_id, star_name, star_birth_year);
        SET star_id_existing = star_id;
	END IF;
    
    -- add to stars_in_movies
    INSERT INTO stars_in_movies (starId, movieId)
	VALUES (star_id_existing, movie_id);
    
    SET success = 1;
    
END //

COMMIT ;
DELIMITER //
