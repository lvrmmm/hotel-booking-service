import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api/client";
import {
  OCCUPANCY_LABELS,
  COMFORT_LABELS,
  roomGradient,
  formatPrice,
  formatDate,
} from "../utils/roomVisuals";

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default function BookingPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const prefilledDates = location.state || {};

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [checkIn, setCheckIn] = useState(prefilledDates.checkIn || todayIso());
  const [checkOut, setCheckOut] = useState(prefilledDates.checkOut || "");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get(`/rooms/${roomId}`, { auth: false })
      .then(setRoom)
      .catch(() => setLoadError("Не удалось загрузить информацию о номере."))
      .finally(() => setLoading(false));
  }, [roomId]);

  const nights = useMemo(() => {
    if (!checkIn || !checkOut) return 0;
    const diff = (new Date(checkOut) - new Date(checkIn)) / (1000 * 60 * 60 * 24);
    return diff > 0 ? diff : 0;
  }, [checkIn, checkOut]);

  const total = room ? nights * room.pricePerNight : 0;

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (nights <= 0) {
      setError("Дата выезда должна быть позже даты заезда.");
      return;
    }

    setSubmitting(true);
    try {
      const booking = await api.post("/bookings", {
        roomId: Number(roomId),
        checkIn,
        checkOut,
      });
      navigate("/my-bookings", { state: { justBooked: booking.id } });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Не удалось создать бронирование. Попробуйте снова.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="page-body">
        <div className="container">
          <p className="loading-text">Загружаем номер…</p>
        </div>
      </div>
    );
  }

  if (loadError || !room) {
    return (
      <div className="page-body">
        <div className="container">
          <div className="empty-state">
            <h3>Номер не найден</h3>
            <p>{loadError || "Возможно, он был удалён из каталога."}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-eyebrow">Бронирование</div>
          <h1>
            {COMFORT_LABELS[room.comfortLevel] || room.comfortLevel} · № {room.roomNumber}
          </h1>
          <p>Укажите даты заезда и выезда, чтобы увидеть итоговую стоимость.</p>
        </div>
      </div>

      <div className="page-body">
        <div className="container">
          <div className="booking-layout">
            <div>
              {error && <div className="form-error">{error}</div>}

              <form onSubmit={handleSubmit}>
                <div className="field-row">
                  <div className="field">
                    <label htmlFor="checkIn">Заезд</label>
                    <input
                      id="checkIn"
                      type="date"
                      min={todayIso()}
                      value={checkIn}
                      onChange={(e) => setCheckIn(e.target.value)}
                      required
                    />
                  </div>
                  <div className="field">
                    <label htmlFor="checkOut">Выезд</label>
                    <input
                      id="checkOut"
                      type="date"
                      min={checkIn}
                      value={checkOut}
                      onChange={(e) => setCheckOut(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <button
                  className="btn btn-primary btn-block"
                  type="submit"
                  disabled={submitting || nights <= 0}
                >
                  {submitting ? "Бронируем…" : "Подтвердить бронирование"}
                </button>
              </form>
            </div>

            <aside className="booking-summary">
              <div
                className="booking-summary-visual"
                style={{ background: roomGradient(room.comfortLevel) }}
              />
              <h3 style={{ marginBottom: 4 }}>
                {COMFORT_LABELS[room.comfortLevel] || room.comfortLevel}
              </h3>
              <p style={{ color: "var(--slate)", fontSize: 14, marginBottom: 18 }}>
                {OCCUPANCY_LABELS[room.occupancyType] || room.occupancyType} · до {room.capacity} гостей
              </p>

              <div className="summary-row">
                <span>Цена за ночь</span>
                <span>{formatPrice(room.pricePerNight)} ₽</span>
              </div>
              <div className="summary-row">
                <span>Заезд</span>
                <span>{checkIn ? formatDate(checkIn) : "—"}</span>
              </div>
              <div className="summary-row">
                <span>Выезд</span>
                <span>{checkOut ? formatDate(checkOut) : "—"}</span>
              </div>
              <div className="summary-row">
                <span>Количество ночей</span>
                <span>{nights || "—"}</span>
              </div>

              <div className="summary-total">
                <span>Итого</span>
                <span className="amount">{formatPrice(total)} ₽</span>
              </div>
            </aside>
          </div>
        </div>
      </div>
    </>
  );
}
