

INSERT INTO games (game_id, title, price, release_date, description, publisher, developer, genre, game_user_id) VALUES
                                                                                                                    ('game-1', 'Cyberpunk 2077', 49.99, '2020-12-10 00:00:00', 'An open-world, action-adventure RPG set in the megalopolis of Night City.', 'CD Projekt Red', 'CD Projekt Red', 'ACTION', '123e4567-e89b-12d3-a456-426614174000'),
                                                                                                                    ('game-2', 'The Witcher 3: Wild Hunt', 29.99, '2015-05-19 00:00:00', 'An open-world action RPG set in a visually stunning fantasy universe full of meaningful choices and impactful consequences.', 'CD Projekt Red', 'CD Projekt Red', 'ACTION', '123e4567-e89b-12d3-a456-426614174000'),
                                                                                                                    ('game-3', 'Red Dead Redemption 2', 59.99, '2018-10-26 00:00:00', 'An epic tale of life in America’s unforgiving heartland.', 'Rockstar Games', 'Rockstar Studios', 'FIGHTING', '123e4567-e89b-12d3-a456-426614174000'),
                                                                                                                    ('game-4', 'Elden Ring', 59.99, '2022-02-25 00:00:00', 'The Golden Order has been broken. Rise, Tarnished, and be guided by grace to brandish the power of the Elden Ring and become an Elden Lord in the Lands Between.', 'Bandai Namco Entertainment', 'FromSoftware', 'ACTION', '123e4567-e89b-12d3-a456-426614174000'),
                                                                                                                    ('game-5', 'Minecraft', 26.95, '2011-11-18 00:00:00', 'A game about placing blocks and going on adventures.', 'Mojang Studios', 'Mojang Studios', 'ADVENTURE', '123e4567-e89b-12d3-a456-426614174000');

-- Sample Data for Reviews Table
INSERT INTO reviews (review_id, comment, rating, timestamp, game_id) VALUES
                                                                         ('review-1', 'Great game, but had some bugs at launch.', '4/5', '2021-01-15 12:00:00', 'game-1'),
                                                                         ('review-2', 'Best RPG I have ever played!', '5/5', '2016-06-20 15:30:00', 'game-2'),
                                                                         ('review-3', 'Amazing story and graphics.', '5/5', '2019-11-05 10:00:00', 'game-3'),
                                                                         ('review-4', 'Very challenging, but rewarding.', '4/5', '2022-03-10 18:00:00', 'game-4'),
                                                                         ('review-5', 'Endless creativity!', '5/5', '2023-01-01 00:00:00', 'game-5'),
                                                                         ('review-6', 'The atmosphere is incredible.', '4/5', '2021-01-20 12:00:00', 'game-1'),
                                                                         ('review-7', 'Geralt is a fantastic character.', '5/5', '2016-07-01 15:30:00', 'game-2'),
                                                                         ('review-8', 'Arthur Morgan is a legend.', '5/5', '2019-11-10 10:00:00', 'game-3'),
                                                                         ('review-9', 'Prepare to die... a lot.', '4/5', '2022-03-15 18:00:00', 'game-4'),
                                                                         ('review-10', 'Build anything you can imagine.', '5/5', '2023-01-05 00:00:00', 'game-5');