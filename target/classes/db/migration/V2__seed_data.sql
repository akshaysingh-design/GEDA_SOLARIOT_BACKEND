-- V2: seed data for GEDA IIoT SCADA SaaS demo slice (PostgreSQL / Supabase)

-- ---------------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------------
INSERT INTO role (id, name) VALUES
    (1, 'SUPER_ADMIN'),
    (2, 'DISCOM_ADMIN'),
    (3, 'PLANT_OPERATOR'),
    (4, 'VIEWER');

-- ---------------------------------------------------------------------------
-- Org tree: 1 STATE -> 2 DISCOM -> 2 DIVISION each -> 2-3 PLANT each
-- ---------------------------------------------------------------------------
-- STATE
INSERT INTO org_unit (id, name, type, parent_id, code) VALUES
    (1, 'Gujarat', 'STATE', NULL, 'GJ');

-- DISCOM (2 under the state)
INSERT INTO org_unit (id, name, type, parent_id, code) VALUES
    (2, 'PGVCL', 'DISCOM', 1, 'PGVCL'),
    (3, 'UGVCL', 'DISCOM', 1, 'UGVCL');

-- DIVISION (2 under each DISCOM)
INSERT INTO org_unit (id, name, type, parent_id, code) VALUES
    (4, 'PGVCL Rajkot Division', 'DIVISION', 2, 'PGVCL-RJK'),
    (5, 'PGVCL Junagadh Division', 'DIVISION', 2, 'PGVCL-JND'),
    (6, 'UGVCL Mehsana Division', 'DIVISION', 3, 'UGVCL-MSN'),
    (7, 'UGVCL Patan Division', 'DIVISION', 3, 'UGVCL-PTN');

-- PLANT (2-3 under each division)
INSERT INTO org_unit (id, name, type, parent_id, code) VALUES
    (8, 'Rajkot Solar Plant 1', 'PLANT', 4, 'PLANT-RJK-01'),
    (9, 'Rajkot Solar Plant 2', 'PLANT', 4, 'PLANT-RJK-02'),
    (10, 'Junagadh Solar Plant 1', 'PLANT', 5, 'PLANT-JND-01'),
    (11, 'Junagadh Wind Plant 1', 'PLANT', 5, 'PLANT-JND-02'),
    (12, 'Mehsana Solar Plant 1', 'PLANT', 6, 'PLANT-MSN-01'),
    (13, 'Mehsana Hybrid Plant 1', 'PLANT', 6, 'PLANT-MSN-02'),
    (14, 'Patan Solar Plant 1', 'PLANT', 7, 'PLANT-PTN-01'),
    (15, 'Patan Wind Plant 1', 'PLANT', 7, 'PLANT-PTN-02'),
    (16, 'Patan Solar Plant 2', 'PLANT', 7, 'PLANT-PTN-03');

-- ---------------------------------------------------------------------------
-- Admin user (SUPER_ADMIN, MFA required). Password: Admin@123
-- BCrypt hash generated via Spring Security's BCryptPasswordEncoder (strength 10).
-- ---------------------------------------------------------------------------
INSERT INTO app_user (id, username, password_hash, full_name, email, phone, role_id, org_unit_id, enabled, mfa_required, created_at) VALUES
    (1, 'admin', '$2a$10$uDp1ZOv4Dz4v3El4X3pfgOzcAD.YVmb2Cc.vm9ENDUGkoBnIVS8oy', 'System Administrator', 'admin@geda.gujarat.gov.in', '9999900000', 1, NULL, TRUE, TRUE, NOW());

-- ---------------------------------------------------------------------------
-- Devices (~15) spread across plants with varied types/statuses/uptime
-- ---------------------------------------------------------------------------
INSERT INTO device (id, device_code, name, type, org_unit_id, status, uptime_percent, tls_cert_valid_until, tls_cert_status, created_at, last_seen_at) VALUES
    (1,  'DEV-RJK01-001', 'Rajkot Plant 1 RMS A', 'SOLAR_RMS',  8,  'ONLINE',  99.80, CURRENT_DATE + INTERVAL '300 days', 'VALID',    NOW() - INTERVAL '400 days', NOW()),
    (2,  'DEV-RJK01-002', 'Rajkot Plant 1 RMS B', 'SOLAR_RMS',  8,  'ONLINE',  98.40, CURRENT_DATE + INTERVAL '250 days', 'VALID',    NOW() - INTERVAL '380 days', NOW()),
    (3,  'DEV-RJK02-001', 'Rajkot Plant 2 RMS A', 'SOLAR_RMS',  9,  'WARNING', 87.10, CURRENT_DATE + INTERVAL '20 days',  'EXPIRING', NOW() - INTERVAL '360 days', NOW() - INTERVAL '2 hours'),
    (4,  'DEV-RJK02-002', 'Rajkot Plant 2 RMS B', 'SOLAR_RMS',  9,  'ONLINE',  95.60, CURRENT_DATE + INTERVAL '200 days', 'VALID',    NOW() - INTERVAL '300 days', NOW()),
    (5,  'DEV-JND01-001', 'Junagadh Plant 1 RMS A', 'SOLAR_RMS', 10, 'ONLINE',  99.10, CURRENT_DATE + INTERVAL '280 days', 'VALID',    NOW() - INTERVAL '340 days', NOW()),
    (6,  'DEV-JND02-001', 'Junagadh Wind Plant RMS A', 'WIND_RMS', 11, 'OFFLINE', 42.30, CURRENT_DATE - INTERVAL '5 days', 'EXPIRED', NOW() - INTERVAL '500 days', NOW() - INTERVAL '3 days'),
    (7,  'DEV-JND02-002', 'Junagadh Wind Plant RMS B', 'WIND_RMS', 11, 'WARNING', 76.50, CURRENT_DATE + INTERVAL '15 days',  'EXPIRING', NOW() - INTERVAL '250 days', NOW() - INTERVAL '1 hour'),
    (8,  'DEV-MSN01-001', 'Mehsana Plant 1 RMS A', 'SOLAR_RMS', 12, 'ONLINE',  97.90, CURRENT_DATE + INTERVAL '320 days', 'VALID',    NOW() - INTERVAL '410 days', NOW()),
    (9,  'DEV-MSN02-001', 'Mehsana Hybrid Plant RMS A', 'HYBRID_RMS', 13, 'ONLINE', 96.20, CURRENT_DATE + INTERVAL '180 days', 'VALID', NOW() - INTERVAL '200 days', NOW()),
    (10, 'DEV-MSN02-002', 'Mehsana Hybrid Plant RMS B', 'HYBRID_RMS', 13, 'WARNING', 81.70, CURRENT_DATE + INTERVAL '10 days', 'EXPIRING', NOW() - INTERVAL '190 days', NOW() - INTERVAL '30 minutes'),
    (11, 'DEV-PTN01-001', 'Patan Plant 1 RMS A', 'SOLAR_RMS', 14, 'ONLINE',  99.50, CURRENT_DATE + INTERVAL '350 days', 'VALID',    NOW() - INTERVAL '420 days', NOW()),
    (12, 'DEV-PTN02-001', 'Patan Wind Plant RMS A', 'WIND_RMS', 15, 'OFFLINE', 55.00, CURRENT_DATE - INTERVAL '10 days', 'EXPIRED', NOW() - INTERVAL '450 days', NOW() - INTERVAL '5 days'),
    (13, 'DEV-PTN03-001', 'Patan Plant 2 RMS A', 'SOLAR_RMS', 16, 'ONLINE',  94.30, CURRENT_DATE + INTERVAL '270 days', 'VALID',    NOW() - INTERVAL '310 days', NOW()),
    (14, 'DEV-PTN03-002', 'Patan Plant 2 RMS B', 'SOLAR_RMS', 16, 'WARNING', 88.80, CURRENT_DATE + INTERVAL '25 days',  'EXPIRING', NOW() - INTERVAL '150 days', NOW() - INTERVAL '45 minutes'),
    (15, 'DEV-RJK01-003', 'Rajkot Plant 1 RMS C', 'SOLAR_RMS',  8,  'ONLINE',  99.95, CURRENT_DATE + INTERVAL '330 days', 'VALID',    NOW() - INTERVAL '100 days', NOW());

-- ---------------------------------------------------------------------------
-- Generation readings: last 24 hours across a few plants, plausible solar
-- generation curve (zero at night, peak midday). Hour buckets are computed
-- relative to the current hour so the trend always looks fresh in a demo,
-- regardless of when this migration runs.
-- Plants used: Rajkot Plant 1 (8), Rajkot Plant 2 (9), Junagadh Plant 1 (10), Mehsana Plant 1 (12).
-- ---------------------------------------------------------------------------
CREATE TEMPORARY TABLE tmp_hour_curve (
    hours_ago INT NOT NULL,
    kwh_base DECIMAL(8,2) NOT NULL
);

INSERT INTO tmp_hour_curve (hours_ago, kwh_base) VALUES
    (0, 0.0), (1, 0.0), (2, 0.0), (3, 0.0), (4, 0.0),
    (5, 5.0), (6, 45.0), (7, 120.0), (8, 220.0), (9, 340.0),
    (10, 460.0), (11, 540.0), (12, 580.0), (13, 560.0), (14, 500.0),
    (15, 410.0), (16, 300.0), (17, 180.0), (18, 80.0), (19, 20.0),
    (20, 0.0), (21, 0.0), (22, 0.0), (23, 0.0);

-- Rajkot Solar Plant 1 (org_unit 8, device 1) - full-strength curve
INSERT INTO generation_reading (org_unit_id, device_id, reading_hour, kwh)
SELECT 8, 1, date_trunc('hour', NOW() - (hours_ago || ' hours')::interval), kwh_base
FROM tmp_hour_curve;

-- Rajkot Solar Plant 2 (org_unit 9, device 3) - slightly reduced (warning-status device)
INSERT INTO generation_reading (org_unit_id, device_id, reading_hour, kwh)
SELECT 9, 3, date_trunc('hour', NOW() - (hours_ago || ' hours')::interval), ROUND(kwh_base * 0.82, 3)
FROM tmp_hour_curve;

-- Junagadh Solar Plant 1 (org_unit 10, device 5) - near full-strength
INSERT INTO generation_reading (org_unit_id, device_id, reading_hour, kwh)
SELECT 10, 5, date_trunc('hour', NOW() - (hours_ago || ' hours')::interval), ROUND(kwh_base * 0.95, 3)
FROM tmp_hour_curve;

-- Mehsana Solar Plant 1 (org_unit 12, device 8) - full-strength curve
INSERT INTO generation_reading (org_unit_id, device_id, reading_hour, kwh)
SELECT 12, 8, date_trunc('hour', NOW() - (hours_ago || ' hours')::interval), ROUND(kwh_base * 1.05, 3)
FROM tmp_hour_curve;

DROP TABLE tmp_hour_curve;

-- ---------------------------------------------------------------------------
-- Alerts (~10) with varied severities referencing seeded devices/plants
-- ---------------------------------------------------------------------------
INSERT INTO alert (device_id, org_unit_id, severity, message, created_at, acknowledged) VALUES
    (6,  11, 'HIGH', 'Device offline for more than 24 hours', NOW() - INTERVAL '2 hours', FALSE),
    (12, 15, 'HIGH', 'TLS certificate expired', NOW() - INTERVAL '5 hours', FALSE),
    (3,  9,  'MED',  'Uptime dropped below 90% in the last 24 hours', NOW() - INTERVAL '8 hours', FALSE),
    (10, 13, 'MED',  'TLS certificate expiring within 10 days', NOW() - INTERVAL '12 hours', FALSE),
    (7,  11, 'MED',  'Intermittent connectivity detected', NOW() - INTERVAL '1 day', TRUE),
    (14, 16, 'MED',  'TLS certificate expiring within 25 days', NOW() - INTERVAL '1 day', FALSE),
    (1,  8,  'LOW',  'Firmware update available', NOW() - INTERVAL '2 days', TRUE),
    (5,  10, 'LOW',  'Scheduled maintenance window completed', NOW() - INTERVAL '3 days', TRUE),
    (9,  13, 'LOW',  'Minor sensor calibration drift detected', NOW() - INTERVAL '3 days', FALSE),
    (13, 16, 'LOW',  'Daily generation report generated', NOW() - INTERVAL '4 days', TRUE);

-- ---------------------------------------------------------------------------
-- Keep the sequences backing GENERATED ALWAYS AS IDENTITY columns in sync
-- with the explicit ids inserted above, since those bypass the sequence.
-- ---------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('role', 'id'), (SELECT MAX(id) FROM role));
SELECT setval(pg_get_serial_sequence('org_unit', 'id'), (SELECT MAX(id) FROM org_unit));
SELECT setval(pg_get_serial_sequence('app_user', 'id'), (SELECT MAX(id) FROM app_user));
SELECT setval(pg_get_serial_sequence('device', 'id'), (SELECT MAX(id) FROM device));
