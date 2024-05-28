INSERT INTO auto_mark_entity (name)
VALUES ('BMW');

INSERT INTO auto_model_entity (name, auto_mark_id)
VALUES ('5-Series', 1);

INSERT INTO generation_entity (generation_number, auto_model_id)
VALUES (8, 1),
       (7, 1),
       (6, 1),
       (5, 1),
       (4, 1),
       (3, 1),
       (2, 1),
       (1, 1);

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 2023, NULL, 1);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (1, 'G60');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 2020, NULL, 2);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (2, 'G30');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (2, 'G31');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 2016, 2020, 2);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (3, 'G30');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (3, 'G31');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 2013, 2017, 3);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (4, 'F10');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (4, 'F11');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 2009, 2013, 3);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (5, 'F10');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (5, 'F11');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 2007, 2010, 4);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (6, 'E60');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (6, 'E61');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 2003, 2007, 4);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (7, 'E60');
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (7, 'E61');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 2000, 2004, 5);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (8, 'E39');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 1995, 2000, 5);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (9, 'E39');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 1994, 1996, 6);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (10, 'E34');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 1987, 1997, 6);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (11, 'E34');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 1981, 1987, 7);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (12, 'E28');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (1, 1976, 1981, 8);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (13, 'E12');

INSERT INTO generation_item_entity (restyling, year_start, year_end, generation_id)
VALUES (0, 1972, 1976, 8);
INSERT INTO generation_item_frames_entity (generation_item_id, frame)
VALUES (14, 'E12');
