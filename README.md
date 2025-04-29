# DonChamps API

Серверная часть проекта игровой платформы DonChamps.

## Технологии

- Java 17
- Spring Boot 3.4.4
- Spring Security с JWT-аутентификацией
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI для документации API

## Документация API (Swagger)

Документация API доступна через Swagger UI после запуска приложения по адресу:

```
http://localhost:8080/swagger-ui.html
```

## Настройка и запуск

### Предварительные требования

- JDK 17+
- PostgreSQL

### Настройка базы данных

1. Создайте базу данных PostgreSQL
2. Настройте подключение в файле `application.properties`

### Запуск приложения

```bash
./gradlew bootRun
```

Или через IDE (IntelliJ IDEA, Eclipse и т.д.).

## Основные API-эндпоинты

### Аутентификация и регистрация

- **POST /api/auth/register** - Регистрация нового пользователя
- **POST /api/auth/login** - Аутентификация пользователя

### Пользователи

- **GET /api/users/me** - Получение данных текущего пользователя
- **GET /api/users** - Получение списка пользователей
- **GET /api/users/{id}** - Получение пользователя по ID
- **PUT /api/users/attributes** - Обновление атрибутов пользователя

### Турниры

- **GET /api/tournaments** - Получение списка всех турниров

### Матчи (пример API)

- **GET /api/matches** - Получение списка всех матчей
- **GET /api/matches/{id}** - Получение матча по ID
- **POST /api/matches** - Создание нового матча (только для админов)
- **PUT /api/matches/{id}** - Обновление существующего матча (только для админов)
- **DELETE /api/matches/{id}** - Удаление матча (только для админов)
- **GET /api/matches/tournament/{tournamentId}** - Получение матчей указанного турнира

## Безопасность

Для доступа к защищенным эндпоинтам необходимо передать JWT-токен в заголовке запроса:

```
Authorization: Bearer <jwt_token>
```

JWT-токен выдается при успешной регистрации или аутентификации.
