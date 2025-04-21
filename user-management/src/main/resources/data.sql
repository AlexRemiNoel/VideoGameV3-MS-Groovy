INSERT INTO admins (admin_id, username, password) VALUES
                                                      ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'admin1', 'adminpass1'),
                                                      ('b2c3d4e5-f6a7-8901-2345-67890abcdef0', 'admin2', 'secureadminpass'),
                                                      ('c3d4e5f6-a7b8-9012-3456-7890abcdef12', 'superadmin', 'verysecretadminpass');
INSERT INTO users (user_id, username, email, password, balance) VALUES
                                                                    ('123e4567-e89b-12d3-a456-426614174000', 'john_doe', 'john.doe@example.com', 'password123', 100.50),
                                                                    ('234e5678-f90c-23e4-b567-537725285111', 'jane_smith', 'jane.smith@example.com', 'securepass', 250.75);


INSERT INTO user_order_ids (user_id, order_id) VALUES
                                                   ('123e4567-e89b-12d3-a456-426614174000', 'aaaaaaaa-bbbb-cccc-dddd-111111111111'), -- Example Order ID 1
                                                   ('123e4567-e89b-12d3-a456-426614174000', 'aaaaaaaa-bbbb-cccc-dddd-222222222222'); -- Example Order ID 2

-- Jane Smith's Order (let's give her one)
INSERT INTO user_order_ids (user_id, order_id) VALUES
    ('234e5678-f90c-23e4-b567-537725285111', 'aaaaaaaa-bbbb-cccc-dddd-333333333333'); -- Example Order ID 3

INSERT INTO user_game_ids (user_id, game_id) VALUES
                                                 ('123e4567-e89b-12d3-a456-426614174000', 'gggggggg-hhhh-iiii-jjjj-111111111111'), -- Example Game ID 1
                                                 ('123e4567-e89b-12d3-a456-426614174000', 'gggggggg-hhhh-iiii-jjjj-222222222222'), -- Example Game ID 2
                                                 ('123e4567-e89b-12d3-a456-426614174000', 'gggggggg-hhhh-iiii-jjjj-333333333333'); -- Example Game ID 3

-- Jane Smith's Games (let's give her two, including one John also owns)
INSERT INTO user_game_ids (user_id, game_id) VALUES
                                                 ('234e5678-f90c-23e4-b567-537725285111', 'gggggggg-hhhh-iiii-jjjj-222222222222'), -- Example Game ID 2 (shared with John)
                                                 ('234e5678-f90c-23e4-b567-537725285111', 'gggggggg-hhhh-iiii-jjjj-444444444444'); -- Example Game ID 4