# Supermarket Management System (Mini Project)

Full-stack Java project: **Spring Boot + MySQL** backend, plain **HTML / CSS / JavaScript** frontend.

## Features
- Dashboard: today's sales, bills count, low-stock alerts
- Products: add / edit / delete / search
- Billing: cart, GST, discount, payment mode, auto stock reduction, printable invoice
- Bill History: view and re-print any old bill

## Requirements
1. JDK 17 or newer
2. MySQL 8 (running on port 3306)
3. VS Code with the **Extension Pack for Java** and **Spring Boot Extension Pack**

## How to run
1. Open the `supermarket` folder in VS Code (File > Open Folder).
2. Open `src/main/resources/application.properties` and put YOUR MySQL password in `spring.datasource.password`.
3. Open `SupermarketApplication.java` and click **Run** above `main`
   (or in a terminal: `mvn spring-boot:run`).
4. Open http://localhost:8080 in your browser.

The database `supermarket_db` and all tables are created automatically.
Optional: run `database/supermarket.sql` in MySQL Workbench to load 10 sample products.

## Project structure
```
supermarket
├── pom.xml
├── database/supermarket.sql
└── src/main
    ├── java/com/supermarket
    │   ├── SupermarketApplication.java
    │   ├── model/        Product, Bill, BillItem        (database tables)
    │   ├── repository/   ProductRepository, BillRepository
    │   ├── dto/          BillRequest                    (data sent by billing page)
    │   ├── service/      BillService                    (billing logic + GST + stock)
    │   └── controller/   Product, Bill, Dashboard, error handler  (REST APIs)
    └── resources
        ├── application.properties
        └── static/       index, products, billing, bills .html + css/ + js/
```

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET | /api/products?search= | list / search products |
| POST | /api/products | add product |
| PUT | /api/products/{id} | update product |
| DELETE | /api/products/{id} | delete product |
| POST | /api/bills | create bill |
| GET | /api/bills | all bills |
| GET | /api/bills/{id} | one bill |
| GET | /api/dashboard | dashboard numbers |
