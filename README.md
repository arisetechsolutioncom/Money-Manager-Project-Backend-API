# Money Manager - Backend API

A complete Spring Boot backend for a money management application with user authentication, transaction management, budget tracking, and expense categorization.

## 🚀 Tech Stack

- **Java 17** - Programming Language
- **Spring Boot 4.0.1** - Web Framework
- **Spring Data JPA** - Database ORM
- **Spring Security** - Authentication & Authorization
- **JWT** - Token-based Authentication
- **MySQL 8** - Database
- **Lombok** - Code Generation
- **ModelMapper** - Object Mapping
- **Maven** - Build Tool

## 📁 Project Structure

```
src/main/java/com/money/money_manager/
├── controller/          # REST API Endpoints
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   ├── CategoryController.java
│   └── HealthController.java
├── service/             # Business Logic
│   ├── UserService.java
│   ├── TransactionService.java
│   └── CategoryService.java
├── repository/          # Database Access
│   ├── UserRepository.java
│   ├── TransactionRepository.java
│   ├── CategoryRepository.java
│   └── BudgetRepository.java
├── entity/              # Database Models
│   ├── User.java
│   ├── Transaction.java
│   ├── Category.java
│   ├── Budget.java
│   └── Role.java
├── dto/                 # Data Transfer Objects
│   ├── UserDTO.java
│   ├── TransactionDTO.java
│   ├── CategoryDTO.java
│   └── BudgetDTO.java
├── config/              # Configuration Classes
│   ├── JwtTokenProvider.java
│   └── CorsConfig.java
├── exception/           # Exception Handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ErrorDetails.java
└── MoneyManagerApplication.java  # Main Class
```

## 🔧 Prerequisites

- Java 17 or higher
- MySQL 8 or higher
- Maven 3.6+
- Git

## 📋 Setup Instructions

### 1. Clone Repository
```bash
git clone <repository-url>
cd Backend
```

### 2. MySQL Database Setup
```sql
CREATE DATABASE money_manager_db;
USE money_manager_db;
```

### 3. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/money_manager_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Configure JWT Secret
```properties
jwt.secret=your_super_secret_key_at_least_256bits_long
jwt.expiration=86400000
```

### 5. Build & Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run with IDE
```

The API will be available at `http://localhost:8080`

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-token` - Verify JWT token
- `POST /api/auth/refresh-token` - Refresh JWT token

### Users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users` - Get all users (admin)
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/{id}/profile` - Get user profile

### Transactions
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions` - Get user's transactions
- `GET /api/transactions/range?startDate=&endDate=` - Get transactions in date range
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Categories
- `POST /api/categories` - Create category
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories` - Get user's categories
- `GET /api/categories/type/{type}` - Get categories by type
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Health Check
- `GET /api/health` - API health status
- `GET /api/info` - API information

## 🔐 Authentication

All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <token>
```

User ID should be passed in custom header:
```
userId: <user-id>
```

## 📊 Database Schema

### Users Table
- id (Primary Key)
- email (Unique)
- username (Unique)
- password (Encrypted)
- firstName
- lastName
- profileImage
- phoneNumber
- role
- isActive
- isEmailVerified
- createdAt
- updatedAt

### Transactions Table
- id (Primary Key)
- title
- description
- amount
- type (INCOME/EXPENSE)
- categoryId (Foreign Key)
- userId (Foreign Key)
- transactionDate
- paymentMethod
- receiptUrl
- createdAt
- updatedAt

### Categories Table
- id (Primary Key)
- name
- description
- type (INCOME/EXPENSE)
- icon
- color
- userId (Foreign Key)
- createdAt
- updatedAt

### Budgets Table
- id (Primary Key)
- name
- description
- limitAmount
- spentAmount
- categoryId (Foreign Key)
- userId (Foreign Key)
- startDate
- endDate
- status (ACTIVE/INACTIVE/EXCEEDED)
- createdAt
- updatedAt

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn jacoco:report
```

## 📝 API Request Examples

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "john_doe",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Create Category
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -H "userId: 1" \
  -d '{
    "name": "Groceries",
    "description": "Daily groceries",
    "type": "EXPENSE",
    "icon": "🛒",
    "color": "#FF6B6B"
  }'
```

### Create Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -H "userId: 1" \
  -d '{
    "title": "Grocery Shopping",
    "description": "Weekly shopping",
    "amount": 100.00,
    "type": "EXPENSE",
    "categoryId": 1,
    "transactionDate": "2025-01-18",
    "paymentMethod": "CARD"
  }'
```

## 🛠️ Development Guidelines

1. **Code Style**: Follow Google Java Style Guide
2. **Logging**: Use Slf4j for logging
3. **Error Handling**: All exceptions are caught by GlobalExceptionHandler
4. **Validation**: Use Jakarta Validation annotations
5. **Security**: JWT tokens expire after 24 hours

## 📦 Build & Deployment

### Build JAR
```bash
mvn clean package -DskipTests
```

### Run JAR
```bash
java -jar target/money-manager-0.0.1-SNAPSHOT.jar
```

### Docker Build (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/money-manager-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🚀 Production Checklist

- [ ] Update JWT secret key
- [ ] Configure production database
- [ ] Enable HTTPS
- [ ] Set up environment variables
- [ ] Configure CORS properly
- [ ] Set up logging
- [ ] Configure database backups
- [ ] Set up monitoring
- [ ] Run security tests
- [ ] Performance testing

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT Documentation](https://jwt.io/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Write tests
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Happy Coding!** 🎉
