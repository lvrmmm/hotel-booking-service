INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    first_name,
    last_name,
    date_of_birth,
    role
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@meridian.local',
    '$2b$10$IIvfnrcZKHQEOUZwbwclKeGTb./qxQlqHVV1boeDor0GLXpOYH2Qe',
    'System',
    'Administrator',
    '1990-01-01',
    'ADMIN'
);