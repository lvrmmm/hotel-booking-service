import { useEffect, useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import Pagination from "../../components/Pagination";
import Toast from "../../components/Toast";
import { api, ApiError, collectionFrom, pageInfoFrom } from "../../api/client";
import { ROLE_LABELS } from "../../utils/roomVisuals";

const ROLE_OPTIONS = ["USER", "MANAGER", "ADMIN"];

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1 });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    loadUsers(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  function loadUsers(pageNumber = 0) {
    setLoading(true);
    api
      .get(`/users?page=${pageNumber}&size=10&sortBy=username&direction=asc`)
      .then((data) => {
        setUsers(collectionFrom(data));
        setPageInfo(pageInfoFrom(data));
      })
      .catch(() => setError("Не удалось загрузить пользователей."))
      .finally(() => setLoading(false));
  }

  function showToast(message, variant = "default") {
    setToast({ message, variant });
    setTimeout(() => setToast(null), 3200);
  }

  async function handleRoleChange(user, newRole) {
    if (newRole === user.role) return;
    setUpdatingId(user.id);
    try {
      const updated = await api.patch(`/users/${user.id}/role`, { role: newRole });
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      showToast(`${updated.username} теперь ${ROLE_LABELS[updated.role]}.`);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось изменить роль.";
      showToast(message, "error");
    } finally {
      setUpdatingId(null);
    }
  }

  async function handleToggleBlock(user) {
    setUpdatingId(user.id);
    try {
      const updated = user.enabled
        ? await api.patch(`/users/${user.id}/block`)
        : await api.patch(`/users/${user.id}/unblock`);
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      showToast(user.enabled ? `${updated.username} заблокирован.` : `${updated.username} разблокирован.`);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Не удалось изменить статус пользователя.";
      showToast(message, "error");
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <AdminLayout
      eyebrow="Панель управления"
      title="Пользователи"
      description="Назначайте роли и при необходимости блокируйте доступ."
    >
      {loading && <p className="loading-text">Загружаем пользователей…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th>Имя</th>
                <th>Логин</th>
                <th>Email</th>
                <th>Роль</th>
                <th>Статус</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => {
                const isAdmin = user.role === "ADMIN";
                return (
                  <tr key={user.id}>
                    <td>
                      {user.firstName} {user.lastName}
                    </td>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>
                      <select
                        value={user.role}
                        disabled={updatingId === user.id || isAdmin}
                        onChange={(e) => handleRoleChange(user, e.target.value)}
                        title={isAdmin ? "Нельзя изменить роль администратора" : undefined}
                      >
                        {ROLE_OPTIONS.map((role) => (
                          <option key={role} value={role}>
                            {ROLE_LABELS[role]}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      {user.enabled === false ? (
                        <span className="role-tag" style={{ borderColor: "#8c3d2e", color: "#8c3d2e" }}>
                          Заблокирован
                        </span>
                      ) : (
                        <span className="role-tag">Активен</span>
                      )}
                    </td>
                    <td>
                      {!isAdmin && (
                        <button
                          className={`btn btn-small ${user.enabled === false ? "btn-ghost" : "btn-danger"}`}
                          disabled={updatingId === user.id}
                          onClick={() => handleToggleBlock(user)}
                        >
                          {user.enabled === false ? "Разблокировать" : "Заблокировать"}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          <Pagination current={pageInfo.number} totalPages={pageInfo.totalPages} onChange={setPage} />
        </>
      )}

      <Toast message={toast?.message} variant={toast?.variant} />
    </AdminLayout>
  );
}
