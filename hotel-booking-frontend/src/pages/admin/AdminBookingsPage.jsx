import { useEffect, useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import Pagination from "../../components/Pagination";
import Toast from "../../components/Toast";
import { api, ApiError, collectionFrom, pageInfoFrom } from "../../api/client";
import { STATUS_LABELS, formatDate, formatPrice } from "../../utils/roomVisuals";

const CANCELLABLE = new Set(["PENDING", "CONFIRMED"]);

export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1 });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [busyId, setBusyId] = useState(null);
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    loadBookings(page, statusFilter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, statusFilter]);

  function showToast(message, variant = "default") {
    setToast({ message, variant });
    setTimeout(() => setToast(null), 3000);
  }

  function loadBookings(pageNumber = 0, status = "ALL") {
    setLoading(true);
    const statusParam = status !== "ALL" ? `&status=${status}` : "";
    api
      .get(`/bookings?page=${pageNumber}&size=10&sortBy=createdAt&direction=desc${statusParam}`)
      .then((data) => {
        setBookings(collectionFrom(data));
        setPageInfo(pageInfoFrom(data));
      })
      .catch(() => setError("Не удалось загрузить бронирования."))
      .finally(() => setLoading(false));
  }

  function handleStatusFilterChange(value) {
    setStatusFilter(value);
    setPage(0);
  }

  async function handleCancel(id) {
    setBusyId(id);
    try {
      await api.patch(`/bookings/${id}/cancel`);
      showToast("Бронирование отменено.");
      loadBookings(page, statusFilter);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось отменить бронирование.";
      showToast(message, "error");
    } finally {
      setBusyId(null);
    }
  }

  async function handleConfirm(id) {
    setBusyId(id);
    try {
      await api.patch(`/bookings/${id}/confirm`);
      showToast("Бронирование подтверждено.");
      loadBookings(page, statusFilter);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось подтвердить бронирование.";
      showToast(message, "error");
    } finally {
      setBusyId(null);
    }
  }

  async function handleComplete(id) {
    setBusyId(id);
    try {
      await api.patch(`/bookings/${id}/complete`);
      showToast("Бронирование отмечено как завершённое.");
      loadBookings(page, statusFilter);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось завершить бронирование.";
      showToast(message, "error");
    } finally {
      setBusyId(null);
    }
  }

  const visibleBookings = bookings;

  return (
    <AdminLayout
      eyebrow="Панель управления"
      title="Все бронирования"
      description="Полный список броней по всем гостям — для контроля загрузки и обработки обращений."
    >
      <div className="filters-bar">
        <div className="filters-bar-left">
          <select value={statusFilter} onChange={(e) => handleStatusFilterChange(e.target.value)}>
            <option value="ALL">Любой статус</option>
            {Object.entries(STATUS_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading && <p className="loading-text">Загружаем бронирования…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && visibleBookings.length === 0 && (
        <div className="empty-state">
          <h3>Нет бронирований</h3>
          <p>По выбранному фильтру брони не найдены на этой странице.</p>
        </div>
      )}

      {!loading && visibleBookings.length > 0 && (
        <div className="bookings-list">
          {visibleBookings.map((booking) => (
            <div className="booking-row" key={booking.id}>
              <div className={`booking-status-bar ${booking.bookingStatus.toLowerCase()}`} />
              <div className="booking-row-body">
                <div className="booking-row-title">Номер № {booking.roomId}</div>
                <div className="booking-row-dates">
                  {formatDate(booking.checkIn)} — {formatDate(booking.checkOut)}
                </div>
                <div className="booking-row-owner">Гость: {booking.userId}</div>
                <div className="booking-status-label">
                  {STATUS_LABELS[booking.bookingStatus] || booking.bookingStatus}
                </div>
              </div>
              <div className="booking-row-actions">
                <div className="booking-price">{formatPrice(booking.totalPrice)} ₽</div>

                {booking.bookingStatus === "PENDING" && (
                  <button
                    className="btn btn-admin btn-small"
                    disabled={busyId === booking.id}
                    onClick={() => handleConfirm(booking.id)}
                  >
                    {busyId === booking.id ? "…" : "Подтвердить"}
                  </button>
                )}

                {booking.bookingStatus === "CONFIRMED" && (
                  <button
                    className="btn btn-ghost btn-small"
                    disabled={busyId === booking.id}
                    onClick={() => handleComplete(booking.id)}
                  >
                    {busyId === booking.id ? "…" : "Завершить"}
                  </button>
                )}

                {CANCELLABLE.has(booking.bookingStatus) && (
                  <button
                    className="btn btn-danger btn-small"
                    disabled={busyId === booking.id}
                    onClick={() => handleCancel(booking.id)}
                  >
                    {busyId === booking.id ? "…" : "Отменить"}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && (
        <Pagination current={pageInfo.number} totalPages={pageInfo.totalPages} onChange={setPage} />
      )}

      <Toast message={toast?.message} variant={toast?.variant} />
    </AdminLayout>
  );
}
