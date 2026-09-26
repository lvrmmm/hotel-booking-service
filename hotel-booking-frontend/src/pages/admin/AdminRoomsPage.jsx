import { useEffect, useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import RoomFormModal from "../../components/RoomFormModal";
import Pagination from "../../components/Pagination";
import Toast from "../../components/Toast";
import { api, ApiError, collectionFrom, pageInfoFrom } from "../../api/client";
import { OCCUPANCY_LABELS, COMFORT_LABELS, formatPrice } from "../../utils/roomVisuals";

export default function AdminRoomsPage() {
  const [rooms, setRooms] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1 });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [editingRoom, setEditingRoom] = useState(null);
  const [showCreate, setShowCreate] = useState(false);

  useEffect(() => {
    loadRooms(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  function showToast(message, variant = "default") {
    setToast({ message, variant });
    setTimeout(() => setToast(null), 3000);
  }

  function loadRooms(pageNumber = 0) {
    setLoading(true);
    api
      .get(`/rooms?page=${pageNumber}&size=10&sortBy=roomNumber&direction=asc`, { auth: false })
      .then((data) => {
        setRooms(collectionFrom(data));
        setPageInfo(pageInfoFrom(data));
      })
      .catch(() => setError("Не удалось загрузить номера."))
      .finally(() => setLoading(false));
  }

  async function handleCreate(payload) {
    await api.post("/rooms", payload);
    setShowCreate(false);
    showToast("Номер создан.");
    loadRooms(page);
  }

  async function handleUpdate(payload) {
    const updated = await api.patch(`/rooms/${editingRoom.id}`, payload);
    setRooms((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
    setEditingRoom(null);
    showToast("Изменения сохранены.");
  }

  async function handleToggleActive(room) {
    try {
      const updated = room.active
        ? await api.patch(`/rooms/${room.id}/deactivate`)
        : await api.patch(`/rooms/${room.id}/activate`);
      setRooms((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      showToast(room.active ? "Номер поставлен на стоп." : "Номер снова активен.");
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось изменить статус номера.";
      showToast(message, "error");
    }
  }

  async function handleDelete(room) {
    if (!confirm(`Удалить номер № ${room.roomNumber}? Действие необратимо.`)) return;
    try {
      await api.del(`/rooms/${room.id}`);
      showToast("Номер удалён.");
      loadRooms(page);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось удалить номер.";
      showToast(message, "error");
    }
  }

  return (
    <AdminLayout
      eyebrow="Панель управления"
      title="Номерной фонд"
      description="Создавайте номера, меняйте параметры и убирайте из каталога устаревшие позиции."
    >
      <div className="filters-bar">
        <div />
        <button className="btn btn-admin" onClick={() => setShowCreate(true)}>
          + Новый номер
        </button>
      </div>

      {loading && <p className="loading-text">Загружаем номера…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th>№</th>
                <th>Категория</th>
                <th>Тип размещения</th>
                <th>Вместимость</th>
                <th>Цена / ночь</th>
                <th>Статус</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rooms.map((room) => (
                <tr key={room.id}>
                  <td>{room.roomNumber}</td>
                  <td>{COMFORT_LABELS[room.comfortLevel] || room.comfortLevel}</td>
                  <td>{OCCUPANCY_LABELS[room.occupancyType] || room.occupancyType}</td>
                  <td>{room.capacity} гостей</td>
                  <td>{formatPrice(room.pricePerNight)} ₽</td>
                  <td>{room.active ? "Активен" : "Скрыт"}</td>
                  <td>
                    <div className="table-actions">
                      <button className="btn btn-ghost btn-small" onClick={() => setEditingRoom(room)}>
                        Изменить
                      </button>
                      <button className="btn btn-ghost btn-small" onClick={() => handleToggleActive(room)}>
                        {room.active ? "Поставить на стоп" : "Активировать"}
                      </button>
                      <button className="btn btn-danger btn-small" onClick={() => handleDelete(room)}>
                        Удалить
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <Pagination current={pageInfo.number} totalPages={pageInfo.totalPages} onChange={setPage} />
        </>
      )}

      {showCreate && <RoomFormModal onSubmit={handleCreate} onClose={() => setShowCreate(false)} />}
      {editingRoom && (
        <RoomFormModal
          initialRoom={editingRoom}
          onSubmit={handleUpdate}
          onClose={() => setEditingRoom(null)}
        />
      )}

      <Toast message={toast?.message} variant={toast?.variant} />
    </AdminLayout>
  );
}
