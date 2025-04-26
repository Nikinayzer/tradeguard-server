CREATE TABLE IF NOT EXISTS exchange_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    readonly_api_key VARCHAR(255) NOT NULL,
    readonly_api_secret VARCHAR(255) NOT NULL,
    readwrite_api_key VARCHAR(255) NOT NULL,
    readwrite_api_secret VARCHAR(255) NOT NULL,
    UNIQUE (user_id, account_name)
); 