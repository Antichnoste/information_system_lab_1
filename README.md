# HumanBeing — лабораторная работа № 1

Java 17, Jakarta EE 10 (CDI Managed Beans, JAX-RS, JTA), Hibernate,
PostgreSQL, WildFly 35.0.1, React и TypeScript.

## Запуск

Нужны JDK 17, Node.js 22.12+ и Docker Compose.

```bash
npm --prefix frontend ci
./gradlew assemble
docker compose up -d --build
```

В Windows используйте `gradlew.bat`. После запуска WildFly приложение
доступно по адресу http://localhost:8080.

Учебные пользователи: `alice / alice-lab-2026`, `bob / bob-lab-2026`.
Список задаётся переменной `APP_USERS` в формате `логин:пароль,логин:пароль`.
Вход использует обычную HTTP-сессию сервера. Отдельные токены не создаются.
Логин и пароль напрямую сравниваются с учебными записями из `APP_USERS`.
Регистрации и хеширования нет. Приложение не создаёт и не использует таблицу пользователей.
Все пользователи могут просматривать и изменять общие данные.

```bash
docker compose down
```

Данные сохраняются в Docker volume. Hibernate создаёт таблицы при первом
запуске. Ограничения полей описаны в моделях: Bean Validation проверяет значения на
уровне ORM, `nullable=false` задаёт NOT NULL, а Hibernate `@Check` — проверки PostgreSQL.
`columnDefinition` и `@Positive` на генерируемом ID не используются.
В Compose нет миграций: только PostgreSQL и backend.

**Для существующей базы:** `hbm2ddl.auto=update` не гарантирует добавление новых
CHECK к уже созданным колонкам. Перед использованием старой БД её схему нужно
отдельно привести в соответствие с моделями. Автоматического удаления данных
или пересоздания таблиц приложение не выполняет. Новые таблицы создаются
с описанными в моделях ограничениями.

Для кафедрального сервера задайте `DB_URL=jdbc:postgresql://pg:5432/studs`,
`DB_USER` и `DB_PASSWORD` — данные учётной записи кафедрального сервера.
Источник данных WildFly настраивается в `deploy/configure.cli`.

Сборка и загрузка на уже настроенный Helios:

```bash
./gradlew deployHelios
```

Параметры задачи: `-PheliosUser`, `-PheliosHost`, `-PheliosPort`,
`-PheliosHome`, `-PwildflyHome`. Используется настроенная SSH-аутентификация.

## Интерфейс

В меню доступны персонажи, автомобили, координаты и специальные операции.
Создание, просмотр, редактирование и удаление открываются в отдельных диалогах.
Для персонажа можно выбрать существующие координаты и автомобиль либо создать
новые. Автомобиль необязателен. При удалении используемого автомобиля или
координат пользователь выбирает замену, и сервер переносит ссылки.

Таблица персонажей содержит все атрибуты, пагинацию, сортировку и поиск по
подстроке в строковых колонках. Открытые таблицы, карточки и результаты
операций автоматически обновляются каждые две секунды запросами к тем же API.
Введённые данные в диалоге редактирования при этом сохраняются.

Специальные операции выполняются на сервере:

- удаление одного персонажа по оружию;
- поиск минимального непустого `minutesOfWaiting`;
- поиск по подстроке `soundtrackName`;
- установка `SORROW` всем персонажам с `realHero=true`;
- выдача красной `Lada Kalina` персонажам с `realHero=true` без автомобиля.

## API

Все адреса начинаются с `/api`. Кроме входа, для запросов нужна cookie сессии.

```text
POST   /auth/login
GET    /auth/session
POST   /auth/logout

GET    /humans
POST   /humans
GET    /humans/{id}
PUT    /humans/{id}
DELETE /humans/{id}

GET    /cars
POST   /cars
GET    /cars/{id}
PUT    /cars/{id}
DELETE /cars/{id}?replacementId={id}

GET    /coordinates
POST   /coordinates
GET    /coordinates/{id}
PUT    /coordinates/{id}
DELETE /coordinates/{id}?replacementId={id}

DELETE /operations/by-weapon?weaponType=HAMMER
GET    /operations/minimum-waiting
GET    /operations/soundtrack?substring=text
POST   /operations/sadden
POST   /operations/give-cars
```

`replacementId` обязателен только при наличии ссылок на удаляемый объект.
`GET /humans` принимает `page` (от 0), `size` (от 1 до 100), `sort`, `direction` (`asc`/`desc`)
и фильтры `name`, `soundtrackName`, `carName`, `carColor`, `mood`, `weaponType`.
Ответ содержит `items`, `total`, `page`, `size`. `total` — число объектов после
фильтрации. Страница за пределами списка возвращает пустой `items`.
Сортировка доступна по этим же полям и `id`. Поиск регистронезависимый,
символы `%` и `_` трактуются как обычные символы подстроки.

Поля `x`, `impactSpeed`, `minutesOfWaiting` передаются в JSON строками,
чтобы JavaScript не терял точность Java `long`.
`creationDate` и `id` формируются автоматически.

### Почему оставлены эти ручки

ТЗ не задаёт количество URL. В текущем варианте 23 сочетания HTTP-метода и адреса:

- 15 для трёх видов объектов: список, чтение по ID, создание, изменение и удаление;
- 5 для специальных операций — по одной на каждый пункт ТЗ;
- 3 для входа, получения текущей сессии и выхода.

Чтение автомобилей и координат по ID используется карточками и их автообновлением.
Список нужен для таблиц и выбора существующих связей. Отдельная ручка
синхронизации не нужна: текущий фронтенд периодически перечитывает данные.

## Разработка

```bash
npm --prefix frontend run dev
./gradlew assemble
```

Vite доступен на http://localhost:5173 и проксирует `/api` на порт 8080.
Для проверки компиляции только бэкенда, без сборки фронтенда и запуска тестов:

```bash
./gradlew compileJava
```

## Где находится код backend

```text
controller/
  HumanController.java      HTTP-операции с персонажами и таблицей
  CarController.java        HTTP-операции с автомобилями
  CoordinatesController.java HTTP-операции с координатами
  OperationsController.java Пять специальных операций
  AuthController.java       Вход, сессия, выход
dto/                        Отдельные Request/Response-классы с Lombok
mapper/                     HumanMapper, CarMapper, CoordinatesMapper
service/
  HumanService.java         CRUD персонажей, таблица, поиск и сортировка
  CarService.java           CRUD автомобилей и перенос ссылок при удалении
  CoordinatesService.java   CRUD координат и перенос ссылок при удалении
  OperationsService.java    Пять специальных операций из ТЗ
  AuthService.java          Проверка логина и пароля
repository/
  HumanRepository.java      Работа с персонажами через EntityManager
  CarRepository.java        Работа с автомобилями через EntityManager
  CoordinatesRepository.java Работа с координатами через EntityManager
exception/
  GlobalExceptionHandler.java Единый формат ошибок
  ConstraintViolationHandler.java Ошибки автоматической проверки DTO
security/
  AuthFilter.java           Проверка, что пользователь вошёл
model/                      Сущности и ограничения полей
```

Структура следует [проекту web4](https://github.com/Antichnoste/web4/tree/master/src/main/java/org/example/web4):
контроллер принимает запрос, сервис выполняет операцию, репозиторий работает с БД,
маппер превращает сущность в DTO. Здесь используются Jakarta EE и Gradle.
Модели сохраняют существующие поля; Lombok применяется в DTO.

Каждый DTO находится в отдельном файле. `@Getter` и `@Setter` генерируют методы
доступа, `@NoArgsConstructor` нужен для чтения JSON, `@AllArgsConstructor` упрощает
создание ответов. Lombok работает при компиляции и не попадает в WAR.
Мапперы — небольшие обычные Java-классы. JSON и адреса API сохранены.
Входные DTO проверяются автоматически через `@NotNull @Valid` в контроллерах.
`ConstraintViolationHandler` возвращает ошибки проверки в общем формате
`message` и `fields`, с именами полей DTO.

Методы в `OperationsService` идут в порядке задания:

1. `deleteByWeapon` — удалить одного персонажа по оружию.
2. `minimumWaiting` — найти минимальное время ожидания.
3. `soundtrack` — найти по подстроке саундтрека.
4. `sadden` — сделать настроение героев максимально печальным.
5. `giveCars` — выдать красные Lada Kalina героям без автомобиля.

В каждом сервисе обычные Java-методы. `@Transactional` объединяет изменения
в одну транзакцию: при ошибке изменения откатываются.

### Как работает репозиторий

`EntityManager` — объект, через который Hibernate работает с БД. Его предоставляет
сервер через `@PersistenceContext`.

- `findAll` читает список сущностей своего типа. В запросе
  `select e from HumanBeing e` указано имя Java-класса, а Hibernate сам
  определяет таблицу и столбцы.
- `findById` находит объект по ID или возвращает 404.
- `save` вызывается для **новых** объектов:
  `persist` сохраняет их, БД назначает ID.
- Для редактирования достаточно найти объект и изменить его поля в сервисе.
  Hibernate отслеживает изменения и записывает их при завершении транзакции.
- `delete` удаляет загруженную сущность.
- `flush` применяется при переносе связей: новые ссылки записываются
  перед удалением старого объекта. Это не завершает транзакцию.

У каждого типа сущности свой репозиторий с одинаковыми простыми методами.
В Jakarta EE нет Spring Data `JpaRepository`, поэтому методы явно вызывают
`EntityManager`. Общий абстрактный репозиторий не используется.
Специальные операции написаны обычными циклами `for` в `OperationsService`.
Поиск, сортировка и пагинация тоже выполняются на сервере, в `HumanService`:
читается список, применяются фильтры, сортировка, затем выбирается страница.
Это простая реализация для небольшого объёма данных лабораторной; весь список
загружается в память сервера.

### Зачем нужны аннотации ограничений

- `@NotNull`, `@NotEmpty`, `@DecimalMin` проверяют значения средствами Bean Validation.
- `@Column(nullable=false)` запрещает NULL в столбце БД. Это нужно и для
  примитивов: Java запрещает `null` в `boolean`/`long`, но не запрещает его
  в SQL-таблице. Поэтому NOT NULL для `cool`, `hasToothpick` и `impactSpeed` сохранён.
- `@GeneratedValue(IDENTITY)` передаёт генерацию ID базе данных. Клиент не задаёт
  ID при создании; `@Positive` для этого поля не нужен.
- `@Check` задаёт проверки `id > 0`, непустого имени и допустимой координаты y
  при создании таблиц Hibernate. Это сохраняет требования ТЗ на уровне БД,
  не встраивая SQL-проверки в `columnDefinition`.

Hibernate подключён в Gradle как `compileOnly` для доступа к `@Check`;
во время работы используется Hibernate из WildFly.
