INSERT INTO roles (id, role)
VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN')
ON CONFLICT (id) DO NOTHING;
