CREATE TABLE activities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_activities_user_created ON activities(user_id, created_at DESC);
