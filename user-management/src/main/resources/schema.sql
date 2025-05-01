CREATE TABLE IF NOT EXISTS UserId (
    uuid VARCHAR(255) PRIMARY KEY
);
CREATE TABLE IF NOT EXISTS AdminId (
    uuid VARCHAR(255) PRIMARY KEY
);


DROP TABLE IF EXISTS user_order_ids;
DROP TABLE IF EXISTS user_game_ids;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS admins;

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
    user_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, order_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS user_game_ids (
    user_id VARCHAR(255) NOT NULL,
    game_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, game_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE
    );