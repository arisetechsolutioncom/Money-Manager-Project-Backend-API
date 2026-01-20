# 🔗 MySQL Integration - Complete Guide

## मैं यहाँ हूँ तुम्हें सेटअप करने में! 🚀

---

## **Step 1: Database Configuration ✅ DONE**

`application.properties` फाइल update हो गई:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/money_manager
spring.datasource.username=root
spring.datasource.password=123456
```

---

## **Step 2: Schema Import करो (2 तरीके)**

### **तरीका A: MySQL Workbench से (सबसे आसान) 👇**

1. **MySQL Workbench खोलो** (तुम्हारे पास स्क्रीनशॉट में दिख रहा है)

2. **Top Menu में `File` → `Open SQL Script` click करो**

3. **यह फाइल select करो:**
   ```
   Backend/setup_database.sql
   ```

4. **Script खुल जाएगी MySQL Workbench में**

5. **Execute करो:** ⚡ (Ctrl + Enter) या Execute बटन दबाओ

6. **Output दिखेगा:**
   ```
   ✅ CREATE TABLE roles
   ✅ INSERT INTO roles
   ✅ CREATE TABLE users
   ✅ CREATE TABLE categories
   ✅ CREATE TABLE transactions
   ✅ CREATE TABLE budgets
   ```

---

### **तरीका B: Command Line से (Terminal)**

```bash
# अगर तुम Command Prompt/PowerShell use करते हो:
mysql -u root -p123456 money_manager < Backend/setup_database.sql
```

---

## **Step 3: Verify करो Database बन गई** ✅

MySQL Workbench में:

1. **Left Panel में `Schemas` देखो**
2. **`money_manager` database expand करो**
3. **5 tables दिखेंगी:**
   - ✅ `roles`
   - ✅ `users`
   - ✅ `categories`
   - ✅ `transactions`
   - ✅ `budgets`

---

## **Step 4: Backend को Run करो** 🎯

### **Command:**
```bash
cd "C:\Users\adity\Desktop\cal bhai bnate hai\Backend"
mvn clean install
mvn spring-boot:run
```

### **Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v4.0.1)

2025-01-18 14:30:00.000  INFO 12345 --- [           main] c.m.m.MoneyManagerApplication           : Started MoneyManagerApplication in 5.234 seconds (JVM running for 5.567)
```

---

## **Step 5: API को Test करो** 🧪

### **Option 1: Postman से (Best)**

1. **Postman खोलो**
2. **नया Request create करो**
3. **Method:** GET
4. **URL:** `http://localhost:8080/api/health`
5. **Send करो**

**Expected Response:**
```json
{
  "success": true,
  "message": "Service is running",
  "data": {
    "status": "UP",
    "timestamp": "2025-01-18T14:30:25",
    "service": "Money Manager API",
    "version": "1.0.0"
  }
}
```

---

### **Option 2: cURL (Terminal)**

```bash
curl http://localhost:8080/api/health
```

---

### **Option 3: Browser से**

1. **URL में यह type करो:**
   ```
   http://localhost:8080/api/health
   ```

2. **Response JSON में दिखेगा**

---

## **Step 6: पहला User Register करो** 👤

### **Postman में:**

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/auth/register`
3. **Body (JSON):**

```json
{
  "email": "demo@example.com",
  "username": "demo_user",
  "password": "Demo@123",
  "firstName": "Demo",
  "lastName": "User"
}
```

4. **Send करो**

**Success Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "demo@example.com",
    "username": "demo_user",
    "firstName": "Demo",
    "lastName": "User",
    "isActive": true,
    "isEmailVerified": false,
    "createdAt": "2025-01-18T14:31:45"
  }
}
```

---

## **Step 7: Login करो** 🔐

### **Postman में:**

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/auth/login`
3. **Body (JSON):**

```json
{
  "email": "demo@example.com",
  "password": "Demo@123"
}
```

4. **Send करो**

**Success Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZW1vX3VzZXIiLCJpYXQiOjE3MDU1MjU5MDUsImV4cCI6MTcwNTYxMjMwNX0.x-x-x-x-x",
    "type": "Bearer",
    "user": {
      "id": 1,
      "email": "demo@example.com",
      "username": "demo_user",
      "firstName": "Demo",
      "lastName": "User"
    }
  }
}
```

✅ **Token को copy करो, अगली API calls में चाहिए!**

---

## **Step 8: Category Create करो** 📂

### **Postman में:**

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/categories`
3. **Headers:**
   ```
   Authorization: Bearer <paste_token_here>
   userId: 1
   ```
4. **Body (JSON):**

```json
{
  "name": "Groceries",
  "description": "Daily groceries and food",
  "type": "EXPENSE",
  "icon": "🛒",
  "color": "#FF6B6B"
}
```

5. **Send करो**

---

## **Step 9: Transaction Create करो** 💰

### **Postman में:**

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/transactions`
3. **Headers:**
   ```
   Authorization: Bearer <paste_token_here>
   userId: 1
   ```
4. **Body (JSON):**

```json
{
  "title": "Grocery Shopping",
  "description": "Weekly shopping at supermarket",
  "amount": 100.50,
  "type": "EXPENSE",
  "categoryId": 1,
  "transactionDate": "2025-01-18",
  "paymentMethod": "CARD"
}
```

5. **Send करो**

---

## **🎯 Complete API Endpoints Summary**

### **Auth Endpoints**
```
POST   /api/auth/register              - Register नया user
POST   /api/auth/login                 - Login करो
POST   /api/auth/verify-token          - Token verify करो
POST   /api/auth/refresh-token         - Token refresh करो
```

### **User Endpoints**
```
GET    /api/users/{id}                 - User details देखो
GET    /api/users/{id}/profile         - User profile देखो
GET    /api/users                      - सभी users (Admin)
PUT    /api/users/{id}                 - User update करो
DELETE /api/users/{id}                 - User delete करो
```

### **Category Endpoints**
```
POST   /api/categories                 - Category create करो
GET    /api/categories                 - सभी categories देखो
GET    /api/categories/{id}            - एक category देखो
GET    /api/categories/type/{type}     - Type से filter करो
PUT    /api/categories/{id}            - Update करो
DELETE /api/categories/{id}            - Delete करो
```

### **Transaction Endpoints**
```
POST   /api/transactions                - Transaction create करो
GET    /api/transactions                - सभी देखो
GET    /api/transactions/{id}           - एक देखो
GET    /api/transactions/range          - Date range में
PUT    /api/transactions/{id}           - Update करो
DELETE /api/transactions/{id}           - Delete करो
```

### **Budget Endpoints**
```
POST   /api/budgets                     - Budget create करो
GET    /api/budgets                     - सभी budgets
GET    /api/budgets/active              - Active budgets
GET    /api/budgets/exceeded            - Exceeded budgets
PUT    /api/budgets/{id}                - Update करो
DELETE /api/budgets/{id}                - Delete करो
```

---

## **❌ Troubleshooting**

### **Problem: "Connection refused"**
```
✅ Solution:
1. MySQL service चल रहा है? (Task Manager में देखो)
2. Port 3306 सही है?
3. Password (123456) सही है?
```

### **Problem: "Unknown database 'money_manager'"**
```
✅ Solution:
1. Database पहले से create करनी थी (screenshot में दिख रहा है)
2. setup_database.sql run करो
3. SHOW DATABASES; से verify करो
```

### **Problem: "Table doesn't exist"**
```
✅ Solution:
1. setup_database.sql completely run हुई?
2. Errors हैं कोई?
3. फिर से run करो
```

### **Problem: "Connection timeout"**
```
✅ Solution:
1. application.properties में URL check करो
2. localhost:3306 accessibility check करो
3. Firewall check करो
```

---

## **📊 Database Structure Verification**

MySQL Workbench में यह run करो:

```sql
-- Check all tables
SHOW TABLES;

-- Check users table structure
DESCRIBE users;

-- Check if roles data exists
SELECT * FROM roles;

-- Check total records
SELECT 
  'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 
  'roles', COUNT(*) FROM roles
UNION ALL
SELECT 
  'categories', COUNT(*) FROM categories
UNION ALL
SELECT 
  'transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 
  'budgets', COUNT(*) FROM budgets;
```

---

## **✅ Success Checklist**

- [x] MySQL Database "money_manager" created
- [x] application.properties updated with credentials
- [ ] Run setup_database.sql
- [ ] Start Backend: `mvn spring-boot:run`
- [ ] Test `/api/health` endpoint
- [ ] Register first user
- [ ] Login & get JWT token
- [ ] Create category
- [ ] Create transaction
- [ ] Verify in MySQL Workbench

---

## **🎉 Next Step**

अब **Frontend को Backend से connect करना है!**

फिर बताना jab सब काम हो जाए! 🚀

---

**कोई सवाल? पूछ! मैं यहीं हूँ! 💪**
