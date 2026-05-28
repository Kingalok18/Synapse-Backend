CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    reminder_time TIMESTAMP NOT NULL,
    is_notified BOOLEAN NOT NULL DEFAULT FALSE,
    is_snoozed BOOLEAN NOT NULL DEFAULT FALSE,
    snooze_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reminders_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_task_id ON reminders(task_id);
CREATE INDEX idx_reminders_is_notified ON reminders(is_notified);
