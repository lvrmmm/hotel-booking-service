import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { ApiError } from "../api/client";

const EMPTY_FORM = {
  username: "",
  email: "",
  password: "",
  firstName: "",
  middleName: "",
  lastName: "",
  dateOfBirth: "",
};

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      const payload = { ...form, middleName: form.middleName || null };
      await register(payload);
      navigate("/rooms", { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        if (err.fieldErrors) setFieldErrors(err.fieldErrors);
      } else {
        setError("Не удалось создать аккаунт. Попробуйте снова.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card" style={{ maxWidth: 460 }}>
        <div className="auth-eyebrow">Meridian</div>
        <h1>Создать аккаунт</h1>
        <p className="auth-sub">Регистрация займёт меньше минуты.</p>

        {error && <div className="form-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field-row">
            <div className="field">
              <label htmlFor="firstName">Имя</label>
              <input id="firstName" value={form.firstName} onChange={update("firstName")} required />
              {fieldErrors.firstName && <div className="field-error">{fieldErrors.firstName}</div>}
            </div>
            <div className="field">
              <label htmlFor="lastName">Фамилия</label>
              <input id="lastName" value={form.lastName} onChange={update("lastName")} required />
              {fieldErrors.lastName && <div className="field-error">{fieldErrors.lastName}</div>}
            </div>
          </div>

          <div className="field">
            <label htmlFor="username">Имя пользователя</label>
            <input id="username" value={form.username} onChange={update("username")} required />
            {fieldErrors.username && <div className="field-error">{fieldErrors.username}</div>}
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" value={form.email} onChange={update("email")} required />
            {fieldErrors.email && <div className="field-error">{fieldErrors.email}</div>}
          </div>

          <div className="field">
            <label htmlFor="password">Пароль</label>
            <input
              id="password"
              type="password"
              value={form.password}
              onChange={update("password")}
              autoComplete="new-password"
              required
            />
            {fieldErrors.password && <div className="field-error">{fieldErrors.password}</div>}
          </div>

          <div className="field">
            <label htmlFor="dateOfBirth">Дата рождения</label>
            <input
              id="dateOfBirth"
              type="date"
              value={form.dateOfBirth}
              onChange={update("dateOfBirth")}
              required
            />
          </div>

          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? "Создаём аккаунт…" : "Зарегистрироваться"}
          </button>
        </form>

        <p className="auth-switch">
          Уже есть аккаунт? <Link to="/login">Войти</Link>
        </p>
      </div>
    </div>
  );
}
