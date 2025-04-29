-- Таблица tournaments
CREATE TABLE IF NOT EXISTS tournaments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    max_participants INTEGER NOT NULL,
    start_date TIMESTAMP,
    modified_at TIMESTAMP,
    current_round INTEGER DEFAULT 0
);

-- Таблица tournament_participants
CREATE TABLE IF NOT EXISTS tournament_participants (
    id SERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT unique_tournament_user UNIQUE (tournament_id, user_id),
    CONSTRAINT fk_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Изменение схемы таблицы matches, чтобы user_id_2 допускал NULL значения
ALTER TABLE matches ALTER COLUMN user_id_2 DROP NOT NULL;
