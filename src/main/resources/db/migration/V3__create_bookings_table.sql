CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    room_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(id),

    CONSTRAINT chk_dates CHECK (check_out > check_in),

    CONSTRAINT chk_price CHECK (total_price >= 0)
);

-- Индекс для быстрого поиска бронирований конкретной комнаты
CREATE INDEX idx_bookings_room_id ON bookings(room_id);

-- Индекс для быстрого поиска бронирований конкретного пользователя
CREATE INDEX idx_bookings_user_id ON bookings(user_id);

-- Составной индекс для быстрой проверки пересечения дат
-- Ускоряет запрос: "найди все бронирования комнаты X, где даты пересекаются"
CREATE INDEX idx_bookings_room_dates ON bookings(room_id, check_in, check_out);

-- Индекс по статусу для фильтрации активных бронирований
CREATE INDEX idx_bookings_status ON bookings(booking_status);