PRAGMA foreign_keys = ON;
CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, role TEXT NOT NULL, active INTEGER NOT NULL DEFAULT 1, created_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS drones (id INTEGER PRIMARY KEY, type TEXT NOT NULL, capacity REAL NOT NULL, battery INTEGER NOT NULL, status TEXT NOT NULL, completed_deliveries INTEGER NOT NULL DEFAULT 0, total_distance REAL NOT NULL DEFAULT 0, total_revenue REAL NOT NULL DEFAULT 0, created_at TEXT NOT NULL, updated_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS packages (id INTEGER PRIMARY KEY, weight REAL NOT NULL, distance REAL NOT NULL, created_at TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS deliveries (id INTEGER PRIMARY KEY AUTOINCREMENT, package_id INTEGER NOT NULL, drone_id INTEGER, status TEXT NOT NULL, estimated_minutes INTEGER NOT NULL, battery_used REAL NOT NULL, charge REAL NOT NULL, failure_reason TEXT, created_at TEXT NOT NULL, completed_at TEXT, FOREIGN KEY(package_id) REFERENCES packages(id), FOREIGN KEY(drone_id) REFERENCES drones(id));
CREATE TABLE IF NOT EXISTS maintenance (id INTEGER PRIMARY KEY AUTOINCREMENT, drone_id INTEGER NOT NULL, action TEXT NOT NULL, note TEXT, created_at TEXT NOT NULL, FOREIGN KEY(drone_id) REFERENCES drones(id));
CREATE TABLE IF NOT EXISTS audit_log (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, action TEXT NOT NULL, entity_type TEXT, entity_id TEXT, details TEXT, created_at TEXT NOT NULL);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
