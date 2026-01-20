## 🔗 Database Integration Complete!

### ✅ MySQL Configuration Done:

```
Database Name: money_manager
Username: root
Password: 123456
Host: localhost:3306
```

---

## 📋 शुरुआत कैसे करें (Step by Step):

### **Step 1: SQL Schema को MySQL में Import करो**

**Option A: MySQL Workbench से (आसान)**
1. MySQL Workbench खोलो (तुम्हारे पास पहले से है)
2. `File` → `Open SQL Script` चुनो
3. `Backend/schema.sql` फाइल select करो
4. यह SQL window में open हो जाएगी
5. **Execute करो** (⚡ बटन दबाओ या Ctrl+Enter)
6. Database tables automatically बन जाएंगी

**Option B: Command Line से**
```bash
mysql -u root -p123456 < Backend/schema.sql
```

---

### **Step 2: Backend Application Run करो**

```bash
cd Backend
mvn clean install
mvn spring-boot:run
```

**Output:**
```
Started MoneyManagerApplication in 5.234 seconds
```

---

### **Step 3: API को Test करो**

```bash
# Health Check
curl http://localhost:8080/api/health

# Success Response:
{
  "success": true,
  "message": "Service is running",
  "data": {
    "status": "UP",
    "timestamp": "2025-01-18T...",
    "service": "Money Manager API",
    "version": "1.0.0"
  }
}
```

---

## 🗄️ Database Tables Created:

```sql
✅ roles         - User roles (ADMIN, USER, PREMIUM_USER)
✅ users         - User accounts with authentication
✅ categories    - Expense/Income categories
✅ transactions  - All financial transactions
✅ budgets       - Budget tracking
```

---

## 🔐 Test करने के लिए First API Call:

### **Register एक नया User:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "username": "demo_user",
    "password": "Demo@123",
    "firstName": "Demo",
    "lastName": "User"
  }'
```

**Expected Response:**
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
    "createdAt": "2025-01-18T..."
  }
}
```

---

### **Login करो:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "password": "Demo@123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "email": "demo@example.com",
      ...
    }
  }
}
```

---

## 📊 MySQL Workbench में Database Verify करो:

1. **Left panel** में `money_manager` database दिखेगी
2. Expand करो, सभी tables दिखेंगी:
   - `roles`
   - `users`
   - `categories`
   - `transactions`
   - `budgets`

3. किसी भी table पर right-click करके data देख सकते हो:
   ```sql
   SELECT * FROM roles;
   SELECT * FROM users;
   ```

---

## 🚀 अगला Step: Frontend को Connect करो

Frontend (`React` project) से Backend API को call करने के लिए:

### Backend Base URL:
```javascript
const API_BASE_URL = 'http://localhost:8080/api'
```

### Example: Register करना
```javascript
const response = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    username: 'john_doe',
    password: 'password123',
    firstName: 'John',
    lastName: 'Doe'
  })
})

const data = await response.json()
console.log(data)
```

---

## ⚙️ Configuration Summary:

| Item | Value |
|------|-------|
| **Database** | MySQL 8 |
| **Database Name** | money_manager |
| **Username** | root |
| **Password** | 123456 |
| **Host** | localhost |
| **Port** | 3306 |
| **Backend Port** | 8080 |
| **Frontend Port** | 5173 |

---

## 🛠️ Troubleshooting:

### **Error: "Can't connect to MySQL"**
```
Check:
1. MySQL service चल रहा है? (mysql-8.0 service)
2. Username/Password सही है?
3. Port 3306 accessible है?
4. Firewall ने block नहीं किया?
```

### **Error: "Database doesn't exist"**
```
Solution:
1. schema.sql को फिर से run करो
2. या manually:
   CREATE DATABASE money_manager;
```

### **Error: "Table already exists"**
```
Solution:
1. DROP DATABASE money_manager; करो
2. फिर schema.sql run करो
```

---

## 📚 Important Notes:

✅ **JWT Token** valid है 24 घंटे के लिए  
✅ **Password** BCrypt से encrypt है  
✅ **All APIs** require `Content-Type: application/json`  
✅ **Transactions API** को `userId` header चाहिए  
✅ **Protected endpoints** को Bearer token चाहिए  

---

## 🎯 Success Checklist:

- [x] MySQL connected
- [x] Database `money_manager` created
- [x] All tables created
- [x] Backend application.properties updated
- [ ] Run `mvn spring-boot:run`
- [ ] Test `/api/health` endpoint
- [ ] Register a test user
- [ ] Login & get JWT token
- [ ] Create categories & transactions
- [ ] Connect React frontend

---

**सब कुछ Setup है! अब Backend को run करो और test करो! 🚀**

अगर कोई error आए तो screenshot share करना! 💪
