-- V3: add plant rated capacity (kW) so generation-analytics endpoints can
-- compute genuine CUF% / performance-ratio metrics instead of fabricated
-- numbers. Only PLANT-type org units carry a capacity; higher-level nodes
-- (STATE/DISCOM/DIVISION) stay NULL since they have no single rated capacity.

ALTER TABLE org_unit ADD COLUMN capacity_kw DECIMAL(10,2) NULL;

-- Plausible rated capacities per seeded plant (roughly sized to the number
-- and type of devices/generation curves already seeded for each plant in
-- V2, so CUF comes out in a believable 25-55% range for solar/wind).
UPDATE org_unit SET capacity_kw = 1200.00 WHERE id = 8;  -- Rajkot Solar Plant 1
UPDATE org_unit SET capacity_kw = 900.00  WHERE id = 9;  -- Rajkot Solar Plant 2
UPDATE org_unit SET capacity_kw = 1000.00 WHERE id = 10; -- Junagadh Solar Plant 1
UPDATE org_unit SET capacity_kw = 1500.00 WHERE id = 11; -- Junagadh Wind Plant 1
UPDATE org_unit SET capacity_kw = 1100.00 WHERE id = 12; -- Mehsana Solar Plant 1
UPDATE org_unit SET capacity_kw = 1300.00 WHERE id = 13; -- Mehsana Hybrid Plant 1
UPDATE org_unit SET capacity_kw = 950.00  WHERE id = 14; -- Patan Solar Plant 1
UPDATE org_unit SET capacity_kw = 1600.00 WHERE id = 15; -- Patan Wind Plant 1
UPDATE org_unit SET capacity_kw = 980.00  WHERE id = 16; -- Patan Solar Plant 2
