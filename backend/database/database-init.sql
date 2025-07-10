CREATE TABLE users (
    id VARCHAR(128) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    birth_year INT,
    interests JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);


CREATE TABLE game_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_name VARCHAR(50) NOT NULL DEFAULT 'Hafiza Kartlari',
    score INT NOT NULL,
    
    duration_seconds INT NOT NULL,
    difficulty_level INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_game_sessions_user_id ON game_sessions(user_id);


CREATE TABLE daily_journals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    journal_content TEXT NOT NULL,
    ai_prompt TEXT,
    ai_response TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_daily_journals_user_id ON daily_journals(user_id);