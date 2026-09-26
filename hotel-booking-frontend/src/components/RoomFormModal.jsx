import { useState } from "react";
import { OCCUPANCY_LABELS, COMFORT_LABELS } from "../utils/roomVisuals";

const EMPTY = {
  roomNumber: "",
  pricePerNight: "",
  occupancyType: "SINGLE",
  comfortLevel: "STANDARD",
  capacity: "1",
};

export default function RoomFormModal({ initialRoom, onSubmit, onClose }) {
  const isEdit = Boolean(initialRoom);
  const [form, setForm] = useState(
    initialRoom
      ? {
          roomNumber: String(initialRoom.roomNumber),
          pricePerNight: String(initialRoom.pricePerNight),
          occupancyType: initialRoom.occupancyType,
          comfortLevel: initialRoom.comfortLevel,
          capacity: String(initialRoom.capacity),
        }
      : EMPTY
  );
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await onSubmit({
        roomNumber: Number(form.roomNumber),
        pricePerNight: Number(form.pricePerNight),
        occupancyType: form.occupancyType,
        comfortLevel: form.comfortLevel,
        capacity: Number(form.capacity),
      });
    } catch (err) {
      setError(err.message || "Не удалось сохранить номер.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>{isEdit ? "Редактировать номер" : "Новый номер"}</h2>

        {error && <div className="form-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field-row">
            <div className="field">
              <label htmlFor="roomNumber">Номер комнаты</label>
              <input
                id="roomNumber"
                type="number"
                min="1"
                value={form.roomNumber}
                onChange={update("roomNumber")}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="capacity">Вместимость</label>
              <input
                id="capacity"
                type="number"
                min="1"
                value={form.capacity}
                onChange={update("capacity")}
                required
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="pricePerNight">Цена за ночь, ₽</label>
            <input
              id="pricePerNight"
              type="number"
              min="0"
              step="0.01"
              value={form.pricePerNight}
              onChange={update("pricePerNight")}
              required
            />
          </div>

          <div className="field-row">
            <div className="field">
              <label htmlFor="occupancyType">Тип размещения</label>
              <select id="occupancyType" value={form.occupancyType} onChange={update("occupancyType")}>
                {Object.entries(OCCUPANCY_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="comfortLevel">Категория комфорта</label>
              <select id="comfortLevel" value={form.comfortLevel} onChange={update("comfortLevel")}>
                {Object.entries(COMFORT_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-ghost" onClick={onClose}>
              Отмена
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? "Сохраняем…" : isEdit ? "Сохранить" : "Создать номер"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
