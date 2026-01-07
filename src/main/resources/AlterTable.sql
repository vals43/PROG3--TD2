ALTER TABLE Player
ADD COLUMN goal_nb INTEGER;

UPDATE Player SET goal_nb = 0 WHERE name = 'Thibaut Courtois';
UPDATE Player SET goal_nb = 2 WHERE name = 'Dani Carvajal';
UPDATE Player SET goal_nb = 5 WHERE name = 'Jude Bellingham';
UPDATE Player SET goal_nb = NULL WHERE name = 'Robert Lewandowski';
UPDATE Player SET goal_nb = NULL WHERE name = 'Antoine Griezmann';

