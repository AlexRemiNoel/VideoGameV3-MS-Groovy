-- Add 10 more admins
INSERT INTO admins (admin_id, username, password) VALUES
                                                      ('d4e5f6a7-b8c9-0123-4567-890abcdef123', 'admin_support', 'supportpass'),
                                                      ('e5f6a7b8-c9d0-1234-5678-90abcdef1234', 'tech_admin', 'techpass1'),
                                                      ('f6a7b8c9-d0e1-2345-6789-0abcdef12345', 'db_admin', 'databaseP@ss'),
                                                      ('a7b8c9d0-e1f2-3456-7890-abcdef123456', 'security_admin', 'secAdmin!23'),
                                                      ('b8c9d0e1-f2a3-4567-8901-bcdef1234567', 'billing_admin', 'billPass99'),
                                                      ('c9d0e1f2-a3b4-5678-9012-cdef12345678', 'content_mgr', 'contentRules'),
                                                      ('d0e1f2a3-b4c5-6789-0123-def123456789', 'network_admin', 'netw0rkAdm'),
                                                      ('e1f2a3b4-c5d6-7890-1234-ef1234567890', 'junior_admin', 'jrAdminPass'),
                                                      ('f2a3b4c5-d6e7-8901-2345-f12345678901', 'senior_admin', 'SrAdm!nPass'),
                                                      ('a3b4c5d6-e7f8-9012-3456-123456789012', 'temp_admin', 'tempPassXYZ');

-- Add 10 more users
INSERT INTO users (user_id, username, email, password, balance) VALUES
                                                                    ('123e4567-e89b-12d3-a456-426614174000', 'alice_wonder', 'alice.w@example.com', 'wonderland', 55.20),
                                                                    ('123e4567-ffff-12d3-a456-426614174000', 'bob_marley', 'bob.m@example.net', 'reggae4life', 120.00),
                                                                    ('567b8901-23fa-56b7-e890-860058518444', 'charlie_chap', 'charlie.c@domain.com', 'silentfilm', 0.50),
                                                                    ('678c9012-34ab-67c8-f901-971169629555', 'diana_ross', 'diana.r@example.com', 'supremeP@ss', 300.15),
                                                                    ('789d0123-45bc-78d9-a012-082270730666', 'elvis_p', 'elvis.p@example.net', 'kingOfRock', 1000.00),
                                                                    ('234e5678-f90c-23e4-b567-537725285111', 'freddie_m', 'freddie.m@domain.com', 'queenFan!', 75.80),
                                                                    ('901f2345-67de-90fb-c234-204492952888', 'grace_kelly', 'grace.k@example.com', 'princessG', 450.25),
                                                                    ('012a3456-78ef-01ac-d345-315503063999', 'harry_potter', 'harry.p@example.net', 'magicWand', 99.99),
                                                                    ('123b4567-89fa-12bd-e456-426614174001', 'irene_adler', 'irene.a@domain.com', 'theWoman', 12.34),
                                                                    ('234e5678-f90c-23e4-b567-537725ccc111', 'james_bond', 'james.b@example.com', 'shakenN0tStirred', 700.07);

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