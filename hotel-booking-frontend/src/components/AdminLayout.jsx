import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AdminLayout({ eyebrow, title, description, children }) {
  const { user } = useAuth();

  return (
    <div className="admin-shell">
      <div className="page-header">
        <div className="container">
          <div className="page-eyebrow">{eyebrow}</div>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
      </div>

      <div className="page-body">
        <div className="container">
          <div className="admin-tabs">
            <NavLink to="/admin/rooms" className={({ isActive }) => `admin-tab ${isActive ? "active" : ""}`}>
              Номерной фонд
            </NavLink>
            <NavLink to="/admin/bookings" className={({ isActive }) => `admin-tab ${isActive ? "active" : ""}`}>
              Все бронирования
            </NavLink>
            {user?.role === "ADMIN" && (
              <NavLink to="/admin/users" className={({ isActive }) => `admin-tab ${isActive ? "active" : ""}`}>
                Пользователи
              </NavLink>
            )}
          </div>

          {children}
        </div>
      </div>
    </div>
  );
}
