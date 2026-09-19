# HumanBeing

Краткая инструкция по запуску и API лабораторной работы.

## Стек

- Java 17
- Jakarta EE 10: CDI, JAX-RS, JTA
- WildFly 35.0.1
- Hibernate / JPA
- PostgreSQL 16
- Flyway
- React, TypeScript, Vite
- Gradle
- Docker Compose

## Быстрый локальный запуск

Требуются JDK 17, Docker Desktop и Node.js 22.12+.

Из корня проекта:

```bash
npm --prefix frontend ci
npm --prefix frontend run build
./gradlew test assemble
docker compose up -d --build --wait
```

Открыть приложение:

```text
http://localhost:8080
```

Проверка backend:

```text
http://localhost:8080/api/health
```

Вход по умолчанию:

```text
alice / alice-lab-2026
bob   / bob-lab-2026
```

Остановить контейнеры:

```bash
docker compose down
```

Данные PostgreSQL сохраняются в Docker volume. Для полного удаления базы:

```bash
docker compose down -v
```

## Frontend в режиме разработки

Backend должен быть запущен отдельно.

```bash
npm --prefix frontend ci
npm --prefix frontend run dev
```

Frontend будет доступен по адресу:

```text
http://localhost:5173
```

Vite проксирует запросы `/api` на backend `http://localhost:8080`.

## Деплой на helios

Сборка, загрузка WAR в WildFly и проверка API выполняются одной командой:

```bash
./gradlew deployHelios
```

Задача выполняет следующие действия:

1. Собирает React frontend.
2. Собирает `build/libs/ROOT.war`.
3. Загружает WAR на сервер через SSH/SCP.
4. Заменяет deployment в WildFly.
5. Проверяет `/api/health`.

Параметры сервера можно переопределить:

```bash
./gradlew deployHelios \
  -PheliosUser=s466113 \
  -PheliosHost=se.ifmo.ru \
  -PheliosPort=2222 \
  -PwildflyHome=/home/studs/s466113/wildfly-35.0.1.Final
```

Пароли не хранятся в проекте. Используется настроенная SSH-аутентификация.

## API

Все адреса начинаются с `/api`.

### Авторизация

```text
POST /auth/login
GET  /auth/session
POST /auth/logout
```

Пример входа:

```json
{
  "username": "alice",
  "password": "alice-lab-2026"
}
```

Для изменений нужны cookie сессии и заголовки:

```text
X-Requested-With: HumanBeing
X-CSRF-Token: <csrfToken из login/session>
```

### Персонажи

```text
GET    /humans
POST   /humans
GET    /humans/{id}
PUT    /humans/{id}
DELETE /humans/{id}?version={version}
```

`GET /humans` поддерживает пагинацию, фильтрацию и сортировку:

```text
page, size, sort, direction
name, soundtrackName, carName, carColor, mood, weaponType
```

### Автомобили и координаты

```text
GET    /cars
POST   /cars
GET    /cars/{id}
PUT    /cars/{id}
DELETE /cars/{id}?version={version}&replacementId={id}

GET    /coordinates
POST   /coordinates
GET    /coordinates/{id}
PUT    /coordinates/{id}
DELETE /coordinates/{id}?version={version}&replacementId={id}
```

`replacementId` нужен при удалении автомобиля или координат, которые используются персонажами.

### Специальные операции

```text
DELETE /operations/by-weapon?weaponType=HAMMER
GET    /operations/minimum-waiting
GET    /operations/soundtrack?substring=text
POST   /operations/sadden
POST   /operations/give-cars
```

### Синхронизация

```text
GET /events
```

`/events` — SSE-поток. События:

```text
ready
changed
heartbeat
```

### Служебный endpoint

```text
GET /health
```

Ожидаемый ответ:

```json
{
  "status": "up"
}
```

## Проверки

Java-тесты и сборка:

```bash
./gradlew test assemble
```

Интеграционные тесты запускаются на отдельной тестовой базе:

```bash
APP_PORT=18080 DB_PORT=55433 \
  docker compose -p humanbeing-test up -d --build --wait

TEST_URL=http://localhost:18080 python3 tests/integration.py

docker compose -p humanbeing-test down
```

E2E-тесты frontend:

```bash
cd frontend
npx playwright install chromium
npm run test:e2e
```

Проверка ограничений PostgreSQL:

```bash
docker compose exec -T postgres \
  psql -U humanity -d humanity -v ON_ERROR_STOP=1 \
  < tests/constraints.sql
```

## Структура проекта

```text
src/main/java/.../api         REST endpoints и DTO
src/main/java/.../service     бизнес-логика
src/main/java/.../repository  Hibernate/JPA доступ к данным
src/main/java/.../model       сущности базы данных
src/main/java/.../security     сессии, CSRF и вход
frontend/src                   React-интерфейс
db/migration                    SQL-миграции Flyway
 tests                          интеграционные и SQL-тесты
```
