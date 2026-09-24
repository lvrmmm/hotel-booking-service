# Meridian — Hotel Booking Service

Учебный pet-проект: сервис бронирования отеля с REST API на Spring Boot и React-фронтендом.
Полный цикл: аутентификация по JWT, ролевая авторизация, бизнес-логика бронирования
с проверкой пересечения дат, и панель управления для персонала.

## Стек

**Backend:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL,
Flyway, JJWT, Maven, JUnit 5 + Mockito

**Frontend:** React, Vite, React Router

**Инфраструктура:** Docker, Docker Compose, nginx

## Архитектура

Монолит, организованный по фичам (package by feature), а не по слоям:

```
ru.lvrmmm.hotelbookingservice
├── room/           — номера: entity, dto, repository, service, controller, exception
├── user/           — пользователи и профиль
├── booking/        — бронирования, проверка пересечения дат
├── availability/   — поиск доступности (комбинирует room + booking)
├── security/       — JWT, UserDetails, фильтр аутентификации
└── common/         — сквозная инфраструктура: обработка ошибок, конфигурация
```

## Функциональность

**Гости (роль USER):**
- Регистрация, вход, управление своим профилем и паролем
- Просмотр каталога номеров, поиск свободных номеров по датам
- Создание бронирования с автоматическим расчётом стоимости
- Просмотр и отмена своих бронирований

**Персонал (ADMIN / MANAGER):**
- Управление номерным фондом: создание, редактирование, постановка на стоп, удаление
- Просмотр всех бронирований, подтверждение, отмена, отметка о завершении
- (только ADMIN) Управление пользователями: назначение ролей, блокировка

Администратора нельзя понизить в роли или заблокировать — ни другим админом,
ни самим собой.

## Модель данных

- **Room** — номер: тип размещения, категория комфорта, вместимость, цена
- **User** — пользователь: роль (USER/MANAGER/ADMIN), статус блокировки
- **Booking** — бронь: даты, статус (PENDING → CONFIRMED → COMPLETED, либо CANCELLED),
  рассчитанная стоимость

Полная схема — в Flyway-миграциях (`src/main/resources/db/migration`).

## Безопасность

- Аутентификация без хранения сессий на сервере (stateless), через JWT
- Пароли хешируются через BCrypt, никогда не хранятся в открытом виде
- Ролевая авторизация на уровне эндпоинтов (`@PreAuthorize`) и владения записями
  (пользователь видит и отменяет только свои брони, если не является персоналом)
- Единообразный формат ошибок API (`status`, `message`, `timestamp`, `fieldErrors`)
- Блокировка пользователя проверяется на каждом запросе, а не только при входе —
  выданный ранее токен немедленно теряет силу после блокировки

## Запуск

Нужен Docker и Docker Compose.

### 1. Настрой секреты

Создай `.env` в корне проекта:

```
POSTGRES_DB=hotel_booking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<свой пароль>
JWT_SECRET=<сгенерируй через: openssl rand -base64 32>
```

### 2. Подними всё одной командой

```bash
docker-compose up --build
```

Поднимутся три сервиса: PostgreSQL, backend (Spring Boot), frontend (React, раздаётся через nginx).

- Фронтенд: http://localhost:5173
- API напрямую: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Тестовый администратор

Создаётся автоматически через Flyway-миграцию:

```
username: admin
password: Admin123!
```

## Локальная разработка без Docker

Если нужно запускать backend напрямую из IDE (например, для отладки):

1. Подними только базу: `docker-compose up postgres`
2. Задай переменные окружения в конфигурации запуска IDE: `DB_PASSWORD`, `JWT_SECRET`
   (значения — как в `.env`)
3. Запусти `HotelBookingServiceApplication`

Для фронтенда — `cd hotel-booking-frontend && npm install && npm run dev`
(подробности и обязательная настройка CORS — в `hotel-booking-frontend/README.md`).

## Тесты

```bash
mvn test
```

Юнит-тесты сервисного слоя (`RoomService`, `UserService`, `BookingService`) через
JUnit 5 и Mockito — без БД, полностью изолированные.

## Что дальше (осознанно отложено)

- Redis + Redisson — распределённая блокировка против race condition при
  одновременном бронировании одной комнаты
- Интеграционные тесты репозиториев (Testcontainers/H2) — проверка реальных
  JPQL-запросов против настоящей БД
- Пагинация для списков номеров/бронирований/пользователей
- Refresh-токены (сейчас один access-токен на час)
- CI (GitHub Actions)

## Структура репозитория

```
.
├── src/                          — backend (Spring Boot)
├── hotel-booking-frontend/       — frontend (React + Vite)
├── docker-compose.yml
├── Dockerfile                    — backend
├── .env                          — секреты (не в git)
└── pom.xml
```