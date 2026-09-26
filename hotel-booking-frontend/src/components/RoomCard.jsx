import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import {
  OCCUPANCY_LABELS,
  COMFORT_LABELS,
  roomGradient,
  formatPrice,
  formatDate,
} from "../utils/roomVisuals";

export default function RoomCard({ room, bookingDates }) {
  const navigate = useNavigate();
  const [occupiedRanges, setOccupiedRanges] = useState(null);

  // Даты занятости показываем только в режиме обычного каталога —
  // если карточка пришла из поиска по датам, номер уже гарантированно
  // свободен на выбранные даты, и показывать что-либо ещё не нужно.
  useEffect(() => {
    if (bookingDates || !room.active) return;
    let cancelled = false;
    api
      .get(`/rooms/${room.id}/occupied-dates`, { auth: false })
      .then((data) => {
        if (!cancelled) setOccupiedRanges(data);
      })
      .catch(() => {
        if (!cancelled) setOccupiedRanges([]);
      });
    return () => {
      cancelled = true;
    };
  }, [room.id, room.active, bookingDates]);

  function handleBook() {
    navigate(`/rooms/${room.id}/book`, bookingDates ? { state: bookingDates } : undefined);
  }

  return (
    <div className="room-card">
      <div className="room-visual" style={{ background: roomGradient(room.comfortLevel) }}>
        <span className="room-number">№ {room.roomNumber}</span>
      </div>

      <div className="room-body">
        <h3 className="room-title">{COMFORT_LABELS[room.comfortLevel] || room.comfortLevel}</h3>

        <div className="room-meta">
          <span>{OCCUPANCY_LABELS[room.occupancyType] || room.occupancyType}</span>
          <span>до {room.capacity} гостей</span>
        </div>

        {occupiedRanges && occupiedRanges.length > 0 && (
          <div className="room-occupied-note">
            Занято: {occupiedRanges.map((r) => `${formatDate(r.checkIn)} — ${formatDate(r.checkOut)}`).join(", ")}
          </div>
        )}

        <div className="room-footer">
          <div className="room-price">
            {formatPrice(room.pricePerNight)} ₽ <small>/ ночь</small>
          </div>

          {room.active ? (
            <button className="btn btn-primary" onClick={handleBook}>
              Забронировать
            </button>
          ) : (
            <span className="room-inactive-tag">Недоступен</span>
          )}
        </div>
      </div>
    </div>
  );
}
