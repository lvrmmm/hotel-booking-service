import { useEffect, useState } from "react";
import { api, ApiError } from "../api/client";
import Toast from "../components/Toast";

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  const [form, setForm] = useState(null);
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileError, setProfileError] = useState("");

  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "" });
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordError, setPasswordError] = useState("");

  useEffect(() => {
    api
      .get("/users/me")
      .then((data) => {
        setProfile(data);
        setForm({
          firstName: data.firstName || "",
          middleName: data.middleName || "",
          lastName: data.lastName || "",
          dateOfBirth: data.dateOfBirth || "",
        });
      })
      .finally(() => setLoading(false));
  }, []);

  function showToast(message, variant = "default") {
    setToast({ message, variant });
    setTimeout(() => setToast(null), 3000);
  }

  function updateField(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleProfileSubmit(e) {
    e.preventDefault();
    setProfileError("");
    setSavingProfile(true);
    try {
      const updated = await api.patch("/users/me", {
        ...form,
        middleName: form.middleName || null,
      });
      setProfile(updated);
      const stored = JSON.parse(localStorage.getItem("user") || "{}");
      localStorage.setItem("user", JSON.stringify({ ...stored, ...updated }));
      showToast("Профиль обновлён.");
    } catch (err) {
      setProfileError(err instanceof ApiError ? err.message : "Не удалось сохранить изменения.");
    } finally {
      setSavingProfile(false);
    }
  }

  async function handlePasswordSubmit(e) {
    e.preventDefault();
    setPasswordError("");
    setSavingPassword(true);
    try {
      await api.patch("/users/me/password", passwordForm);
      setPasswordForm({ currentPassword: "", newPassword: "" });
      showToast("Пароль изменён.");
    } catch (err) {
      setPasswordError(err instanceof ApiError ? err.message : "Не удалось изменить пароль.");
    } finally {
      setSavingPassword(false);
    }
  }

  if (loading || !form) {
    return (
      <div className="page-body">
        <div className="container">
          <p className="loading-text">Загружаем профиль…</p>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-eyebrow">Личный кабинет</div>
          <h1>Мой профиль</h1>
          <p>Управляйте своими данными и паролем.</p>
        </div>
      </div>

      <div className="page-body">
        <div className="container">
          <div className="booking-layout">
            <div>
              <h3 style={{ marginBottom: 18 }}>Личные данные</h3>
              {profileError && <div className="form-error">{profileError}</div>}

              <form onSubmit={handleProfileSubmit}>
                <div className="field-row">
                  <div className="field">
                    <label htmlFor="firstName">Имя</label>
                    <input id="firstName" value={form.firstName} onChange={updateField("firstName")} required />
                  </div>
                  <div className="field">
                    <label htmlFor="lastName">Фамилия</label>
                    <input id="lastName" value={form.lastName} onChange={updateField("lastName")} required />
                  </div>
                </div>

                <div className="field">
                  <label htmlFor="middleName">Отчество</label>
                  <input id="middleName" value={form.middleName} onChange={updateField("middleName")} />
                </div>

                <div className="field">
                  <label htmlFor="dateOfBirth">Дата рождения</label>
                  <input
                    id="dateOfBirth"
                    type="date"
                    value={form.dateOfBirth}
                    onChange={updateField("dateOfBirth")}
                  />
                </div>

                <button className="btn btn-primary" type="submit" disabled={savingProfile}>
                  {savingProfile ? "Сохраняем…" : "Сохранить изменения"}
                </button>
              </form>

              <h3 style={{ margin: "40px 0 18px" }}>Смена пароля</h3>
              {passwordError && <div className="form-error">{passwordError}</div>}

              <form onSubmit={handlePasswordSubmit}>
                <div className="field">
                  <label htmlFor="currentPassword">Текущий пароль</label>
                  <input
                    id="currentPassword"
                    type="password"
                    value={passwordForm.currentPassword}
                    onChange={(e) =>
                      setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="field">
                  <label htmlFor="newPassword">Новый пароль</label>
                  <input
                    id="newPassword"
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
                    required
                  />
                </div>

                <button className="btn btn-ghost" type="submit" disabled={savingPassword}>
                  {savingPassword ? "Меняем…" : "Изменить пароль"}
                </button>
              </form>
            </div>

            <aside className="booking-summary">
              <h3 style={{ marginBottom: 14 }}>Аккаунт</h3>
              <div className="summary-row">
                <span>Логин</span>
                <span>{profile.username}</span>
              </div>
              <div className="summary-row">
                <span>Email</span>
                <span>{profile.email}</span>
              </div>
            </aside>
          </div>
        </div>
      </div>

      <Toast message={toast?.message} variant={toast?.variant} />
    </>
  );
}
