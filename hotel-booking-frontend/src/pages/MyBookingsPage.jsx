import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { api, ApiError, collectionFrom } from "../api/client";
import { STATUS_LABELS, formatDate, formatPrice } from "../utils/roomVisuals";
import Toast from "../components/Toast";

const CANCELLABLE = new Set(["PENDING", "CONFIRMED"]);

export default function MyBookingsPage() {
  const location = useLocation();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    loadBookings();
    if (location.state?.justBooked) {
      showToast("Бронирование создано и ожидает подтверждения.");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function showToast(message, variant = "default") {
    setToast({ message, variant });
    setTimeout(() => setToast(null), 3200);
  }

  function loadBookings() {
    setLoading(true);
    api
      .get("/bookings/my")
      .then((data) => setBookings(collectionFrom(data)))
      .catch(() => setError("Не удалось загрузить бронирования."))
      .finally(() => setLoading(false));
  }

  async function handleCancel(id) {
    setCancellingId(id);
    try {
      const updated = await api.patch(`/bookings/${id}/cancel`);
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)));
      showToast("Бронирование отменено.");
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось отменить бронирование.";
      showToast(message, "error");
    } finally {
      setCancellingId(null);
    }
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-eyebrow">Личный кабинет</div>
          <h1>Мои бронирования</h1>
          <p>Здесь собраны все ваши брони — текущие и прошедшие.</p>
        </div>
      </div>

      <div className="page-body">
        <div className="container">
          {loading && <p className="loading-text">Загружаем бронирования…</p>}

          {error && <div className="form-error">{error}</div>}

          {!loading && !error && bookings.length === 0 && (
            <div className="empty-state">
              <h3>Пока нет бронирований</h3>
              <p>Выберите номер в каталоге, чтобы забронировать свой первый визит.</p>
            </div>
          )}

          {!loading && bookings.length > 0 && (
            <div className="bookings-list">
              {bookings
                .slice()
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                .map((booking) => (
                  <div className="booking-row" key={booking.id}>
                    <div className={`booking-status-bar ${booking.bookingStatus.toLowerCase()}`} />
                    <div className="booking-row-body">
                      <div className="booking-row-title">Номер № {booking.roomId}</div>
                      <div className="booking-row-dates">
                        {formatDate(booking.checkIn)} — {formatDate(booking.checkOut)}
                      </div>
                      <div className="booking-status-label">
                        {STATUS_LABELS[booking.bookingStatus] || booking.bookingStatus}
                      </div>
                    </div>
                    <div className="booking-row-actions">
                      <div className="booking-price">{formatPrice(booking.totalPrice)} ₽</div>
                      {CANCELLABLE.has(booking.bookingStatus) && (
                        <button
                          className="btn btn-danger"
                          disabled={cancellingId === booking.id}
                          onClick={() => handleCancel(booking.id)}
                        >
                          {cancellingId === booking.id ? "Отменяем…" : "Отменить"}
                        </button>
                      )}
                    </div>
                  </div>
                ))}
            </div>
          )}
        </div>
      </div>

      <Toast message={toast?.message} variant={toast?.variant} />
    </>
  );
}
