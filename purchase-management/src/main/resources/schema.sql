CREATE TABLE orders (
                        order_id VARCHAR(255) NOT NULL,
                        order_date DATETIME,
                        total_price DOUBLE,
                        order_user_id VARCHAR(255),
                        PRIMARY KEY (order_id),
                        FOREIGN KEY (order_user_id) REFERENCES users(user_id)
);