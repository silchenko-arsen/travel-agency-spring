INSERT INTO app_users (
    email, password, first_name, last_name, phone,
    role, verified, blocked, balance, created_at, updated_at
)
VALUES
    ('ivan.petrenko@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ivan', 'Petrenko', '+380671234567', 'ROLE_USER', true, false, 15000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('olena.koval@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Olena', 'Koval', '+380931112233', 'ROLE_USER', true, false, 22000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('andrii.shevchenko@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Andrii', 'Shevchenko', '+380991234321', 'ROLE_USER', true, false, 8000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('maria.bondar@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maria', 'Bondar', '+380631010101', 'ROLE_USER', true, false, 30000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dmytro.melnyk@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dmytro', 'Melnyk', '+380501234999', 'ROLE_USER', true, false, 12000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tours (
    title, description, country, city, start_date, end_date,
    price, available_places, hot, created_at, updated_at
)
VALUES
    ('Weekend in Lviv', 'Three days in the old city with guided excursions.', 'Ukraine', 'Lviv', DATE '2026-08-10', DATE '2026-08-13', 3000.00, 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Greek Islands', 'Sea, beaches and island hopping.', 'Greece', 'Crete', DATE '2026-09-01', DATE '2026-09-10', 25000.00, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carpathian Adventure', 'Hiking, mountains and local food.', 'Ukraine', 'Yaremche', DATE '2026-07-15', DATE '2026-07-20', 7000.00, 12, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Paris Classic', 'Museums, city walks and famous landmarks.', 'France', 'Paris', DATE '2026-10-05', DATE '2026-10-12', 32000.00, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Rome Weekend', 'Ancient ruins, Italian food and city walks.', 'Italy', 'Rome', DATE '2026-08-20', DATE '2026-08-24', 18000.00, 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Barcelona Sun', 'Beach holiday with sightseeing in Barcelona.', 'Spain', 'Barcelona', DATE '2026-09-12', DATE '2026-09-19', 27000.00, 9, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Prague Budget Tour', 'Affordable trip with walking tours and free time.', 'Czech Republic', 'Prague', DATE '2026-07-25', DATE '2026-07-30', 12000.00, 15, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Vienna Culture Trip', 'Museums, palaces and classical music.', 'Austria', 'Vienna', DATE '2026-11-03', DATE '2026-11-08', 21000.00, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Egypt All Inclusive', 'Sea resort with all inclusive hotel package.', 'Egypt', 'Hurghada', DATE '2026-12-01', DATE '2026-12-08', 29000.00, 14, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Turkey Family Holiday', 'Family-friendly resort near the sea.', 'Turkey', 'Antalya', DATE '2026-08-05', DATE '2026-08-12', 23000.00, 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('London Explorer', 'Famous landmarks, museums and city transport pass.', 'United Kingdom', 'London', DATE '2026-10-18', DATE '2026-10-25', 36000.00, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Swiss Alps Tour', 'Mountains, lakes and scenic train routes.', 'Switzerland', 'Interlaken', DATE '2026-09-22', DATE '2026-09-29', 45000.00, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO bookings (
    price_at_booking, status, tour_id, user_id, created_at, updated_at
)
VALUES
    (3000.00, 'REGISTERED', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (25000.00, 'PAID', 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7000.00, 'REGISTERED', 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (32000.00, 'CANCELED', 4, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18000.00, 'PAID', 5, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (27000.00, 'REGISTERED', 6, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12000.00, 'PAID', 7, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21000.00, 'REGISTERED', 8, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (29000.00, 'PAID', 9, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23000.00, 'REGISTERED', 10, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (36000.00, 'CANCELED', 11, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (45000.00, 'PAID', 12, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7000.00, 'PAID', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3000.00, 'CANCELED', 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23000.00, 'PAID', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);