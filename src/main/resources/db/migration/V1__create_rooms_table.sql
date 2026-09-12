CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number INTEGER NOT NULL UNIQUE,
    price_per_night NUMERIC(10,2) NOT NULL,
    occupancy_type VARCHAR(30) NOT NULL,
    comfort_level VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL
)