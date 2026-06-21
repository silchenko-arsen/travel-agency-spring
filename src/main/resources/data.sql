INSERT INTO app_users (
    email, password, first_name, last_name, phone,
    role, verified, blocked, balance, created_at, updated_at
)
VALUES
    ('admin@travel.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Travel', '+380501111111', 'ROLE_ADMIN', true, false, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('manager@travel.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Manager', 'Travel', '+380502222222', 'ROLE_MANAGER', true, false, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user@travel.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'User', 'Travel', '+380503333333', 'ROLE_USER', true, false, 1000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tours (title, description, country, city, start_date, end_date, price, available_places, hot, created_at, updated_at)
VALUES
('Weekend in Lviv', 'Three days in the old city with guided excursions.', 'Ukraine', 'Lviv', DATE '2026-08-10', DATE '2026-08-13', 3000.00, 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
( 'Greek Islands', 'Sea, beaches and island hopping.', 'Greece', 'Crete', DATE '2026-09-01', DATE '2026-09-10', 25000.00, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
( 'Carpathian Adventure', 'Hiking, mountains and local food.', 'Ukraine', 'Yaremche', DATE '2026-07-15', DATE '2026-07-20', 7000.00, 12, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
( 'Paris Classic', 'Museums, city walks and famous landmarks.', 'France', 'Paris', DATE '2026-10-05', DATE '2026-10-12', 32000.00, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
