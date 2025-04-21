CREATE TABLE IF NOT EXISTS UserId (
                                      uuid VARCHAR(255) PRIMARY KEY
    );
CREATE TABLE IF NOT EXISTS AdminId (
                                       uuid VARCHAR(255) PRIMARY KEY
    );


DROP TABLE IF EXISTS user_order_ids;
DROP TABLE IF EXISTS user_game_ids;

-- Now drop the tables they referenced
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS admins; -- Order doesn't strictly matter relative to users, but keep related drops together
CREATE TABLE IF NOT EXISTS users (
                       user_id VARCHAR(255) NOT NULL PRIMARY KEY ,
                       username VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       balance DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS admins (
                        admin_id VARCHAR(255) NOT NULL PRIMARY KEY,
                        username VARCHAR(255),
                        password VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS user_order_ids (
                                              user_id VARCHAR(255) NOT NULL,        -- Foreign key referencing users table PK
    order_id VARCHAR(255) NOT NULL,     -- Stores the actual order ID string
    PRIMARY KEY (user_id, order_id),    -- Composite primary key
    FOREIGN KEY (user_id) REFERENCES users(user_id) -- Add FK constraint
    ON DELETE CASCADE -- Optional: delete rows here if user is deleted
    );

CREATE TABLE IF NOT EXISTS user_game_ids (
                                             user_id VARCHAR(255) NOT NULL,         -- Foreign key referencing users table PK
    game_id VARCHAR(255) NOT NULL,      -- Stores the actual game ID string
    PRIMARY KEY (user_id, game_id),     -- Composite primary key
    FOREIGN KEY (user_id) REFERENCES users(user_id) -- Add FK constraint
    ON DELETE CASCADE -- Optional: delete rows here if user is deleted
    );