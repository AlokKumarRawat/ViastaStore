# Viasta Store

A full-stack e-commerce web application built using **Java Spring Boot**, **Thymeleaf**, **MySQL**, and **Tailwind CSS**. The application provides a seamless shopping experience for customers along with a powerful administrative dashboard for managing products, categories, and orders.

---

## Features

### Customer Module

* User registration and login
* Session-based authentication
* Browse products by category
* Search products
* View detailed product information
* Add products to cart
* Manage delivery addresses
* Secure checkout process
* Multiple payment methods

  * UPI Payment
  * Cash on Delivery (COD)
* Place orders
* View order history
* Cancel orders (request-based)

---

### Admin Module

* Admin authentication
* Dashboard
* Category Management
* Product Management
* Order Management
* Manage product visibility
* View customer orders
* Update order status

---

## Technology Stack

### Frontend

* HTML5
* CSS3
* Tailwind CSS
* Thymeleaf
* JavaScript

### Backend

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA (Hibernate)

### Database

* MySQL

### Build Tool

* Maven

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   ├── Controller
│   │   ├── Model
│   │   ├── Repository
│   │   ├── Service
│   │   └── Config
│   │
│   ├── resources
│   │   ├── templates
│   │   ├── static
│   │   └── application.properties
│   │
│   └── Public
│       └── ProductImages
```

---

## Installation

### Clone the repository

```bash
git clone https://github.com/your-username/ViastaStore.git
```

### Navigate to the project

```bash
cd ViastaStore
```

### Configure MySQL

Create a MySQL database and update the database credentials in `application.properties`.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/viastastore
spring.datasource.username=root
spring.datasource.password=your_password
```

---

### Run the application

Using Maven Wrapper

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

## Future Improvements

* Wishlist
* Forgot Password
* Product Reviews & Ratings
* Coupons & Discounts
* Email Notifications
* React Frontend
* REST API Integration
* JWT Authentication
* Payment Gateway Enhancements
* Analytics Dashboard

---

## Learning Outcomes

This project helped strengthen practical knowledge of:

* Spring Boot
* MVC Architecture
* JPA & Hibernate
* MySQL Database Design
* Thymeleaf
* Tailwind CSS
* Session Management
* CRUD Operations
* E-commerce Workflow
* Order Processing

---

## Author

**Alok Kumar Rawat**

Computer Science Student passionate about Full Stack Web Development.

* GitHub: https://github.com/AlokKumarRawat

---

## License

This project is created for educational and portfolio purposes.
