import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import RoomCard from "../components/RoomCard";
import Pagination from "../components/Pagination";
import { OCCUPANCY_LABELS, COMFORT_LABELS } from "../utils/roomVisuals";

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default function RoomsPage() {
  const [rooms, setRooms] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [occupancyFilter, setOccupancyFilter] = useState("ALL");
  const [comfortFilter, setComfortFilter] = useState("ALL");
  const [page, setPage] = useState(0);

  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [searchMode, setSearchMode] = useState(false);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    if (!searchMode) loadAllRooms(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  function loadAllRooms(pageNumber = 0) {
    setLoading(true);
    setSearchMode(false);
    api
      .get(`/rooms?page=${pageNumber}&size=9&sortBy=roomNumber&direction=asc`, { auth: false })
      .then((data) => {
        setRooms(data.content);
        setPageInfo({ number: data.number, totalPages: data.totalPages });
      })
      .catch(() => setError("Не удалось загрузить номера. Попробуйте обновить страницу."))
      .finally(() => setLoading(false));
  }

  async function handleSearch(e) {
    e.preventDefault();
    if (!checkIn || !checkOut) return;
    setSearching(true);
    setError("");
    try {
      const data = await api.get(
        `/rooms/available?checkIn=${checkIn}&checkOut=${checkOut}`,
        { auth: false }
      );
      setRooms(data);
      setSearchMode(true);
      setPageInfo({ number: 0, totalPages: 1 });
    } catch {
      setError("Не удалось выполнить поиск. Проверьте даты и попробуйте снова.");
    } finally {
      setSearching(false);
    }
  }

  function resetSearch() {
    setPage(0);
    loadAllRooms(0);
  }

  const filteredRooms = useMemo(() => {
    return rooms.filter((room) => {
      if (occupancyFilter !== "ALL" && room.occupancyType !== occupancyFilter) return false;
      if (comfortFilter !== "ALL" && room.comfortLevel !== comfortFilter) return false;
      return true;
    });
  }, [rooms, occupancyFilter, comfortFilter]);

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-eyebrow">Каталог номеров</div>
          <h1>Найдите номер, который подходит именно вам</h1>
          <p>Укажите даты, чтобы увидеть только свободные номера — или просмотрите весь каталог.</p>

          <form onSubmit={handleSearch} style={{ marginTop: 28 }}>
            <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "flex-end" }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 180 }}>
                <label htmlFor="searchCheckIn">Заезд</label>
                <input
                  id="searchCheckIn"
                  type="date"
                  min={todayIso()}
                  value={checkIn}
                  onChange={(e) => setCheckIn(e.target.value)}
                  required
                />
              </div>
              <div className="field" style={{ marginBottom: 0, minWidth: 180 }}>
                <label htmlFor="searchCheckOut">Выезд</label>
                <input
                  id="searchCheckOut"
                  type="date"
                  min={checkIn || todayIso()}
                  value={checkOut}
                  onChange={(e) => setCheckOut(e.target.value)}
                  required
                />
              </div>
              <button className="btn btn-primary" type="submit" disabled={searching}>
                {searching ? "Ищем…" : "Найти свободные номера"}
              </button>
              {searchMode && (
                <button type="button" className="btn btn-ghost" onClick={resetSearch}>
                  Сбросить поиск
                </button>
              )}
            </div>
          </form>
        </div>
      </div>

      <div className="page-body">
        <div className="container">
          <div className="filters-bar">
            <div className="filters-bar-left">
              <select value={occupancyFilter} onChange={(e) => setOccupancyFilter(e.target.value)}>
                <option value="ALL">Любая вместимость</option>
                {Object.entries(OCCUPANCY_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>

              <select value={comfortFilter} onChange={(e) => setComfortFilter(e.target.value)}>
                <option value="ALL">Любая категория</option>
                {Object.entries(COMFORT_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </div>

            {searchMode && (
              <span style={{ color: "var(--stone)", fontSize: 14, alignSelf: "center" }}>
                Показаны номера, свободные на выбранные даты
              </span>
            )}
          </div>

          {loading && <p className="loading-text">Загружаем номера…</p>}
          {error && <div className="form-error">{error}</div>}

          {!loading && !error && filteredRooms.length === 0 && (
            <div className="empty-state">
              <h3>Ничего не найдено</h3>
              <p>
                {searchMode
                  ? "На выбранные даты свободных номеров с такими параметрами нет. Попробуйте другие даты."
                  : "Попробуйте изменить фильтры или загляните позже."}
              </p>
            </div>
          )}

          {!loading && filteredRooms.length > 0 && (
            <>
              <div className="rooms-grid">
                {filteredRooms.map((room) => (
                  <RoomCard
                    key={room.id}
                    room={room}
                    bookingDates={searchMode ? { checkIn, checkOut } : null}
                  />
                ))}
              </div>

              {!searchMode && (
                <Pagination
                  current={pageInfo.number}
                  totalPages={pageInfo.totalPages}
                  onChange={setPage}
                />
              )}
            </>
          )}
        </div>
      </div>
    </>
  );
}
