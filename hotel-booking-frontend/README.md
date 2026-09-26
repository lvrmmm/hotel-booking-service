# Meridian — фронтенд для Hotel Booking Service

React + Vite приложение для Spring Boot бэкенда: каталог и поиск номеров по датам,
регистрация/вход, профиль, бронирование, панель управления для персонала.

## Дизайн

Бутик-отельный стиль: бутылочно-зелёный + пергамент + медный акцент, золото — отдельный
акцент для зоны управления. Заголовки — Newsreader (editorial serif с курсивом),
интерфейс — Sora. Обложки номеров — CSS-градиенты по категории комфорта.

## Страницы

**Гостевые:**
- `/rooms` — каталог с поиском свободных номеров по датам заезда/выезда и фильтрами
- `/login`, `/register` — вход и регистрация
- `/rooms/:id/book` — бронирование (даты подставляются автоматически, если пришли из поиска)
- `/my-bookings` — свои брони, отмена
- `/profile` — свои данные, редактирование, смена пароля

**ADMIN / MANAGER:**
- `/admin/rooms` — номера: создание, редактирование, стоп/активация, удаление
- `/admin/bookings` — все брони: отмена, отметка "завершено" для подтверждённых

**Только ADMIN:**
- `/admin/users` — список пользователей, смена роли, блокировка/разблокировка
  (администратора нельзя ни понизить, ни заблокировать — сервер и интерфейс это учитывают)

## 1. Установка

```bash
cd hotel-booking-frontend
npm install
```

## 2. Обязательно: CORS на бэкенде

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

Подключи через `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`
в `securityFilterChain`, перед `.csrf(...)`.

## 3. Требования к бэкенду — какие поля/эндпоинты нужны фронтенду

- `UserResponse`: `id, username, email, firstName, middleName, lastName, dateOfBirth, role, enabled`
- `BookingResponse`: поле владельца — `userId` (camelCase)
- `JwtResponse`: `{ token, user }`
- Эндпоинты: `GET /rooms/available?checkIn&checkOut`, `PATCH /rooms/{id}/activate`,
  `PATCH /rooms/{id}/deactivate`, `GET/PATCH /users/me`, `PATCH /users/me/password`,
  `PATCH /users/{id}/block`, `PATCH /users/{id}/unblock`, `PATCH /bookings/{id}/complete`

## 4. Нужен хотя бы один ADMIN

Обычная регистрация всегда создаёт роль `USER`. Используй сид-пользователя `admin`
из Flyway-миграции, либо назначь роль вручную:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'твой_логин';
```

## 5. Запуск

```bash
npm run dev
```

Откроется на `http://localhost:5173`. Бэкенд и PostgreSQL должны быть подняты.

## 6. Сборка

```bash
npm run build
```

## Технические детали

- Роутинг: `react-router-dom`. `RoleRoute` ограничивает `/admin/*` по ролям,
  `ProtectedRoute` — просто требует входа.
- Токен — в `localStorage` (осознанный компромисс для учебного проекта, см. предыдущие
  версии README про XSS-риск и альтернативу с httpOnly cookie).
- Все запросы — через `src/api/client.js`.

## Структура

```
src/
├── api/client.js
├── context/AuthContext.jsx
├── components/
│   ├── Navbar.jsx, ProtectedRoute.jsx, RoleRoute.jsx
│   ├── AdminLayout.jsx, RoomFormModal.jsx
│   └── RoomCard.jsx, Toast.jsx
├── pages/
│   ├── LoginPage, RegisterPage, RoomsPage, BookingPage, MyBookingsPage, ProfilePage
│   └── admin/AdminRoomsPage, AdminBookingsPage, AdminUsersPage
├── utils/roomVisuals.js
└── styles/global.css
```
