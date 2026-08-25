CREATE TABLE users (
  id UUID PRIMARY KEY, normalized_email VARCHAR(320) NOT NULL UNIQUE, first_name VARCHAR(100), last_name VARCHAR(100),
  role VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL, created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE credentials (user_id UUID PRIMARY KEY REFERENCES users(id), password_hash VARCHAR(100) NOT NULL);
