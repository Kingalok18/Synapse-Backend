CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY,
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    timezone VARCHAR(100) NOT NULL DEFAULT 'UTC',
    push_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    audio_chimes_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
