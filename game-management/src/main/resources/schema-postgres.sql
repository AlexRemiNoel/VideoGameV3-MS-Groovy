CREATE TABLE IF NOT EXISTS GameId (
                                      uuid VARCHAR(255) PRIMARY KEY
    );

-- Create ReviewId Embeddable Table (if needed, H2 might handle it implicitly)
CREATE TABLE IF NOT EXISTS ReviewId (
                                        uuid VARCHAR(255) PRIMARY KEY
    );


drop table if exists reviews;
drop table if exists games;


CREATE TABLE IF NOT EXISTS games (
                                     game_id VARCHAR(255) PRIMARY KEY, -- Using UUID for GameId
    title VARCHAR(255),
    price DECIMAL(2),
    release_date TIMESTAMP,
    description VARCHAR(2000), -- Adjusted size
    publisher VARCHAR(255),
    developer VARCHAR(255),
    genre VARCHAR(255),
    game_user_id VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS reviews (
                                       review_id VARCHAR(255) PRIMARY KEY, -- Using UUID for ReviewId
    comment VARCHAR(2000), -- Adjusted size
    rating VARCHAR(255),
    timestamp TIMESTAMP,
    game_id VARCHAR(255),
    FOREIGN KEY (game_id) REFERENCES games(game_id)
    );

