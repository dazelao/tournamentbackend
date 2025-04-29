# Швейцарская система турниров

Модуль для проведения турниров по Швейцарской системе. Позволяет организовывать соревнования с оптимальным количеством туров, где каждый участник встречается с соперниками близкого уровня.

## Важное примечание перед использованием

1. Из-за конфликта имен контроллеров, если вы видите пустой файл `MatchController.java` в директории `controller`, пожалуйста, удалите его. Вместо него используйте класс `SwissMatchController.java`.

2. Из-за конфликта имен сервисов, если вы видите пустой файл `TournamentService.java` в директории `service`, пожалуйста, удалите его. Вместо него используйте класс `SwissTournamentService.java`.

## Принципы Швейцарской системы

1. Количество туров рассчитывается по формуле `R = ⌈log₂(N)⌉`, где:
   - R - количество туров
   - N - количество участников

2. В первом туре участники распределяются случайным образом.

3. В последующих турах участники с одинаковым или близким количеством очков играют друг с другом.

4. Каждый игрок играет с новым соперником в каждом туре (при возможности).

5. По окончании всех туров определяется победитель по количеству набранных очков.

## Структура модуля

### Модели (JPA Entity)

- `SwissTournament` - основная модель турнира
- `SwissMatch` - модель матча между игроками
- `SwissRegistration` - модель регистрации игрока на турнир
- `PlayerResult` - модель результатов игрока

### DTO (объекты передачи данных)

- `TournamentCreateDto` - данные для создания нового турнира
- `TournamentResponseDto` - данные о турнире для передачи клиенту
- `MatchUpdateDto` - данные для обновления результата матча
- `MatchResponseDto` - данные о матче для передачи клиенту
- `PlayerResultDto` - данные о результатах игрока для передачи клиенту

### Репозитории (Spring Data JPA)

- `SwissTournamentRepository` - репозиторий для работы с турнирами
- `SwissMatchRepository` - репозиторий для работы с матчами
- `SwissRegistrationRepository` - репозиторий для работы с регистрациями
- `PlayerResultRepository` - репозиторий для работы с результатами игроков

### Сервисы

- `SwissTournamentService` - основной сервис для управления турниром
- `FirstRoundService` - сервис для работы с первым туром
- `SwissSystemService` - сервис для генерации пар и обработки результатов

### Контроллеры

- `SwissTournamentController` - контроллер для управления турнирами
- `SwissMatchController` - контроллер для управления матчами

## REST API Endpoints

### Турниры

- `POST /api/swiss/tournaments` - создание нового турнира
- `GET /api/swiss/tournaments` - получение списка всех турниров
- `GET /api/swiss/tournaments/{tournamentId}` - получение информации о турнире
- `GET /api/swiss/tournaments/active` - получение списка активных турниров
- `GET /api/swiss/tournaments/registration` - получение списка турниров с открытой регистрацией
- `POST /api/swiss/tournaments/{tournamentId}/open` - открытие регистрации на турнир
- `POST /api/swiss/tournaments/{tournamentId}/close` - закрытие регистрации на турнир
- `POST /api/swiss/tournaments/{tournamentId}/start` - запуск турнира
- `POST /api/swiss/tournaments/{tournamentId}/cancel` - отмена турнира
- `POST /api/swiss/tournaments/{tournamentId}/next-round` - переход к следующему раунду
- `POST /api/swiss/tournaments/{tournamentId}/register` - регистрация текущего пользователя на турнир
- `POST /api/swiss/tournaments/{tournamentId}/unregister` - отмена регистрации текущего пользователя
- `GET /api/swiss/tournaments/{tournamentId}/matches/current` - получение списка матчей текущего раунда
- `POST /api/swiss/tournaments/{tournamentId}/matches/update` - обновление результата матча
- `GET /api/swiss/tournaments/{tournamentId}/results` - получение итоговых результатов турнира

### Матчи

- `GET /api/swiss/matches/{matchId}` - получение информации о матче
- `GET /api/swiss/matches/my` - получение списка матчей текущего пользователя
- `GET /api/swiss/matches/my/upcoming` - получение списка предстоящих матчей текущего пользователя
- `POST /api/swiss/matches/{matchId}/start` - начало матча
- `POST /api/swiss/matches/{matchId}/update` - обновление результата матча
- `POST /api/swiss/matches/{matchId}/cancel` - отмена матча

## База данных

Модуль использует следующие таблицы в базе данных:

- `swiss_tournaments` - информация о турнирах
- `swiss_matches` - информация о матчах
- `swiss_registrations` - информация о регистрациях на турниры
- `swiss_player_results` - информация о результатах игроков

## Использование модуля

1. Создание турнира:
   ```java
   TournamentCreateDto dto = new TournamentCreateDto();
   dto.setName("FIFA Tournament");
   dto.setDescription("Annual FIFA tournament");
   dto.setMaxPlayers(16);
   dto.setStartDate(LocalDateTime.now());
   dto.setEndDate(LocalDateTime.now().plusDays(7));
   
   SwissTournament tournament = swissTournamentService.createTournament(dto);
   ```

2. Открытие регистрации:
   ```java
   swissTournamentService.openRegistration(tournamentId);
   ```

3. Регистрация игроков:
   ```java
   swissTournamentService.registerPlayer(tournamentId, user);
   ```

4. Закрытие регистрации:
   ```java
   swissTournamentService.closeRegistration(tournamentId);
   ```

5. Запуск турнира:
   ```java
   swissTournamentService.startTournament(tournamentId);
   ```

6. Обновление результатов матчей:
   ```java
   swissTournamentService.updateMatchResult(tournamentId, matchId, 3, 1);
   ```

7. Переход к следующему туру:
   ```java
   swissTournamentService.proceedToNextRound(tournamentId);
   ```

8. Получение итоговых результатов:
   ```java
   List<PlayerResult> results = swissTournamentService.getTournamentResults(tournamentId);
   ```
