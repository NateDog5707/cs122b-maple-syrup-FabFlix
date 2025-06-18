DELIMITER //

CREATE PROCEDURE init_check_ids()
BEGIN
	-- Declare temp variables
        DECLARE max_genre INT DEFAULT 0;
        DECLARE max_movie INT DEFAULT 0;
        DECLARE max_star INT DEFAULT 0;
        
    -- Check if the table already exists
    IF NOT EXISTS (
        SELECT * FROM information_schema.tables 
        WHERE table_schema = DATABASE() AND table_name = 'check_ids'
    ) THEN

        -- Create the table
        CREATE TABLE check_ids (
            next_genre_id INT NOT NULL,
            next_movie_id INT NOT NULL,
            next_star_id INT NOT NULL
        );

        -- Get max genre ID
        SELECT MAX(id) INTO max_genre FROM genres;

        -- Get max numeric part of movie ID (e.g., tt1234567890)
        SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO max_movie FROM movies;

        -- Get max numeric part of star ID (e.g., nm00012345)
        SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO max_star FROM stars;

        -- Insert the initial next IDs (add +1 to each)
        INSERT INTO check_ids (next_genre_id, next_movie_id, next_star_id)
        VALUES (
            max_genre + 1,
            max_movie + 1,
            max_star + 1
        );

    END IF;
END //

DELIMITER ;

