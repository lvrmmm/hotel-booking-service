# Meridian — Hotel Booking Service

Учебный full-stack проект системы управления бронированием отеля. Проект демонстрирует разработку REST API на Spring Boot, работу с PostgreSQL и Redis, JWT-аутентификацию, ролевую модель доступа, миграции базы данных и интеграционное тестирование через Testcontainers.

## Возможности

### Для гостя и пользователя

- регистрация и вход по JWT;
- просмотр каталога и карточек номеров;
- поиск свободных номеров по датам;
- создание бронирования с автоматическим расчётом стоимости;
- просмотр и отмена собственных бронирований;
- просмотр и редактирование профиля, смена пароля.

### Для персонала

- создание и редактирование номеров;
- активация, деактивация и удаление номеров;
- просмотр бронирований с пагинацией и фильтрацией по статусу;
- подтверждение, отмена и завершение бронирований;
- управление пользователями, ролями и блокировкой учётных записей для `ADMIN`.

Администратора нельзя заблокировать или понизить в роли — в том числе другого администратора или самого себя.

## Технологии

**Backend:** Java 21, Spring Boot 4.1, Spring Web MVC, Spring Security, Spring Data JPA, Hibernate, PostgreSQL, Flyway, Redis, Redisson, JJWT, OpenAPI/Swagger, Maven.

**Frontend:** React 18, Vite, React Router, CSS.

**Тестирование:** JUnit 5, Mockito, AssertJ, MockMvc, Spring Boot Test, Testcontainers (PostgreSQL и Redis).

**Инфраструктура:** Docker, Docker Compose, nginx.

## Архитектура backend

Backend организован по предметным областям (`package by feature`):

```text
ru.lvrmmm.hotelbookingservice
├── auth/           — регистрация и аутентификация
├── room/           — номера, каталог и управление номерным фондом
├── booking/        — создание и жизненный цикл бронирований
├── availability/   — поиск свободных номеров
├── user/           — профиль, роли и блокировка пользователей
├── security/       — JWT-фильтр и интеграция со Spring Security
└── common/         — конфигурация и единая обработка ошибок
```

Основные сущности:

- `Room` — номер, категория комфорта, тип размещения, вместимость и цена;
- `User` — пользователь с ролью `USER`, `MANAGER` или `ADMIN`;
- `Booking` — бронь со сроком проживания, стоимостью и статусом.

Схема PostgreSQL создаётся и обновляется миграциями из `src/main/resources/db/migration`.

## Важные технические решения

- stateless-аутентификация через JWT;
- хеширование паролей с BCrypt;
- ролевая авторизация через Spring Security и `@PreAuthorize`;
- проверка владения бронированием на уровне бизнес-логики;
- проверка пересечения дат перед созданием бронирования;
- распределённая блокировка Redisson для защиты от одновременного бронирования одного номера;
- кеширование каталога номеров в Redis;
- пагинация и сортировка списков номеров, пользователей и бронирований;
- валидация структуры БД через Flyway и `ddl-auto=validate`;
- единый формат ошибок API с HTTP-статусом, сообщением и ошибками полей.

## Запуск через Docker Compose

Понадобятся Docker и Docker Compose.

### 1. Создать `.env`

В корне `HotelBookingService` создай файл `.env`:

```dotenv
POSTGRES_DB=hotel_booking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_me
JWT_SECRET=replace_with_a_secure_base64_secret
```

Секрет можно сгенерировать командой:

```bash
openssl rand -base64 64
```

Файл `.env` содержит секреты и не должен попадать в Git.

### 2. Запустить приложение

```bash
docker compose up --build
```

Будут запущены четыре сервиса: PostgreSQL, Redis, Spring Boot backend и React frontend под nginx.

| Сервис | Адрес |
|---|---|
| Frontend | http://localhost:5173 |
| REST API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Остановка:

```bash
docker compose down
```

Чтобы также удалить локальный том PostgreSQL:

```bash
docker compose down -v
```

### Тестовый администратор

Учётная запись создаётся миграцией Flyway:

```text
username: admin
password: Admin123!
```

## Локальная разработка

### Backend

Запусти PostgreSQL и Redis:

```bash
docker compose up -d postgres redis
```

Задай переменные окружения:

```text
DB_NAME=hotel_booking
DB_USERNAME=postgres
DB_PASSWORD=<пароль из .env>
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=<секрет из .env>
```

После этого запусти `HotelBookingServiceApplication` из IDE либо выполни:

```bash
mvn spring-boot:run
```

### Frontend

```bash
cd hotel-booking-frontend
npm install
npm run dev
```

Frontend обращается к API по адресу `http://localhost:8080/api/v1`. Backend разрешает CORS-запросы с `http://localhost:5173`.

## Тесты

Полный набор запускается из каталога `HotelBookingService`:

```bash
mvn test
```

Для интеграционных тестов должен работать Docker. Testcontainers самостоятельно поднимает изолированные PostgreSQL 16 и Redis 7; локальные тестовые базы создавать не требуется.

В проекте 60 тестов:

- unit-тесты сервисов номеров, пользователей, бронирований и доступности;
- тесты авторизации, `UserDetailsService` и создания/валидации JWT;
- интеграционные тесты JPA-запроса поиска свободных номеров;
- MockMvc-тесты REST API и обработки некорректных параметров;
- проверка применения миграций Flyway к настоящей PostgreSQL;
- проверка запуска полного Spring-контекста с PostgreSQL и Redis.

Тестовая инфраструктура находится в `src/test/java/ru/lvrmmm/hotelbookingservice/integration/AbstractIntegrationTest.java`.

## Структура репозитория

```text
HotelBookingService/
├── src/main/java/                 — backend-код
├── src/main/resources/            — конфигурация и Flyway-миграции
├── src/test/java/                 — unit- и интеграционные тесты
├── hotel-booking-frontend/        — React-приложение
├── Dockerfile                     — образ backend
├── docker-compose.yaml            — PostgreSQL, Redis, backend и frontend
├── pom.xml                        — Maven-конфигурация
└── README.md
```

## Возможные следующие шаги

- refresh-токены и принудительный отзыв access-токенов;
- end-to-end тесты пользовательских сценариев frontend;
- CI-пайплайн с автоматическим запуском тестов и сборкой Docker-образов;
- метрики и мониторинг через Spring Boot Actuator, Prometheus и Grafana;
- стабильные DTO для пагинированных ответов вместо прямой сериализации `Page`.
