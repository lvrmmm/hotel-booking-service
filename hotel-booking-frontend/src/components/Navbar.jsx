import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isStaff = user?.role === "ADMIN" || user?.role === "MANAGER";

  function handleLogout() {
    logout();
    navigate("/rooms");
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <NavLink to="/rooms" className="brand">
          <span className="brand-mark">◆</span> Meridian
        </NavLink>

        <nav className="nav-links">
          <NavLink to="/rooms" className={({ isActive }) => (isActive ? "active" : "")}>
            Номера
          </NavLink>

          {user && (
            <NavLink to="/my-bookings" className={({ isActive }) => (isActive ? "active" : "")}>
              Мои брони
            </NavLink>
          )}
          {user && (
            <NavLink to="/profile" className={({ isActive }) => (isActive ? "active" : "")}>
              Профиль
            </NavLink>
          )}

          {isStaff && (
            <>
              <span className="nav-divider" />
              <NavLink to="/admin/rooms" className={({ isActive }) => (isActive ? "active" : "")}>
                Номерной фонд
              </NavLink>
              <NavLink to="/admin/bookings" className={({ isActive }) => (isActive ? "active" : "")}>
                Все брони
              </NavLink>
              {user.role === "ADMIN" && (
                <NavLink to="/admin/users" className={({ isActive }) => (isActive ? "active" : "")}>
                  Пользователи
                </NavLink>
              )}
              <span className="nav-admin-tag">{user.role === "ADMIN" ? "Admin" : "Manager"}</span>
            </>
          )}

          {user ? (
            <>
              <span className="nav-user">{user.firstName || user.username}</span>
              <button className="nav-logout" onClick={handleLogout}>
                Выйти
              </button>
            </>
          ) : (
            <NavLink to="/login" className={({ isActive }) => (isActive ? "active" : "")}>
              Войти
            </NavLink>
          )}
        </nav>
      </div>
    </header>
  );
}
