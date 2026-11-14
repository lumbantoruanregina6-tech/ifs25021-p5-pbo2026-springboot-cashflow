# 11S25021-p5-pbo2026-springboot-cashflow

Aplikasi Cashflow (Catatan Keuangan) - Spring Boot 4.1.2, Vaadin 25.3.4, PostgreSQL (JDBC).

## Menjalankan
1. Buat database: `CREATE DATABASE pbo2026_cashflow;`
2. Cek `.env` (salin dari `.env.example`), sesuaikan DB_USERNAME / DB_PASSWORD.
3. `.\mvnw.cmd spring-boot:run` lalu buka http://localhost:8080
4. Login awal: ROOT_USERNAME / ROOT_PASSWORD dari `.env` (default admin / admin).

## Build
`.\mvnw.cmd clean package -Pproduction` lalu `java -jar target\11S25021-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar`

## Test
`.\mvnw.cmd clean verify`
