# Travel Agency Spring Boot Project

Фінальний проєкт Travel Agency з ролями `USER`, `MANAGER`, `ADMIN`.

## Stack

- Java 17
- Spring Boot 3
- Spring MVC + Thymeleaf
- Spring Security
- Stateless JWT authentication через HttpOnly cookie
- Spring Data JPA
- H2 для test/dev profile
- PostgreSQL для prod profile
- BCryptPasswordEncoder
- Lombok
- ModelMapper
- Validation
- Global exception handler + одна сторінка помилки
- i18n: English + Ukrainian
- AOP logging

## Ролі

### USER

- реєстрація з email-кодом;
- логін через JWT cookie;
- перегляд турів;
- пошук, пагінація, сортування;
- бронювання туру;
- оплата туру з балансу;
- відмова від туру, якщо тур ще не почався;
- reset password через email token;
- оновлення профілю, крім email;
- поповнення балансу.

### MANAGER

- весь перегляд;
- позначення туру як hot;
- зміна статусу бронювання з REGISTERED на PAID або CANCELED.

### ADMIN

- усе, що може manager;
- додавання/видалення туру;
- зміна всіх полів туру;
- блокування/розблокування користувача.

## Запуск in-memory H2

```bash
mvn spring-boot:run
```

Профіль за замовчуванням: `test`.

H2 console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:travel_agency
User: sa
Password:
```

## Демо-акаунти

Пароль для всіх демо-акаунтів: `password`

```text
admin@travel.com    ROLE_ADMIN
manager@travel.com  ROLE_MANAGER
user@travel.com     ROLE_USER
```

## Production PostgreSQL

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://localhost:5432/travel_agency \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
JWT_SECRET=VGhpcyBpcyBhIDMyIGJ5dGUgc2VjcmV0IGZvciBKV1QhISE= \
mvn spring-boot:run
```

## Email

За замовчуванням email не відправляється реально, а код логуються в консоль.
Для реальної пошти постав:

```yaml
app:
  mail:
    enabled: true
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

Не зберігай пароль Gmail у GitHub. Використовуй env variables.
