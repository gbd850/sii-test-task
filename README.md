# sii-test-task
## About

Spring Boot application to manage discount codes for sales and promotions (called promo codes), created as a submission for test task for Java Developer internship in Sii Polska.

The application runs on Java 21 with Spring Boot 3.2.5. It implements concurrency via virtual threads, query optimization via pagination, and optimistic locking to ensure safety of concurrent database operations.
## Run the application
```bash
$ git clone https://github.com/gbd850/sii-test-task.git

$ cd sii-test-task

$ mvnw spring-boot:run

```
The main application runs on `localhost:8080`.

## Specification

#### `POST v1/api/products` endpoint

To create new product.

##### Example Request body

```json

{
    "name": "Product 1",
    "description": "Product 1 description",
    "price": 10.99,
    "currency": "eur"
}

```

##### Example Response

```json

{
    "id": 1,
    "name": "Product 1",
    "description": "Product 1 description",
    "price": 10.99,
    "currency": "EUR"
}

```
-----

#### `PATCH v1/api/products/{productId}` endpoint

To edit existing product.

##### Example Request body

```json

{
    "name": "Product 2",
    "description": "Product 2 description",
    "currency": "USD"
}

```
##### Example Response

```json

{
    "id": 1,
    "name": "Product 2",
    "description": "Product 2 description",
    "price": 10.99,
    "currency": "USD"
}

```
-----

#### `POST v1/api/promo-codes/monetary` endpoint

To create new monetary promo code.

##### Example Request body

```json

{
    "code": "aAa12345gjJggdjhMMfsms5",
    "expirationDate": "2024-10-10",
    "maxUsages": 10,
    "amount": 100,
    "currency": "eur"
}

```

##### Example Response

```json

{
    "code": "aAa12345gjJggdjhMMfsms5",
    "expirationDate": "2024-10-10",
    "amount": 100,
    "currency": "EUR",
    "discountMethod": "MONETARY"
}

```

-----

#### `POST v1/api/promo-codes/percentage` endpoint

To create new percentage promo code.

##### Example Request body

```json

{
    "code": "aAa12345gjJggdjhMMfsms7",
    "expirationDate": "2024-10-10",
    "maxUsages": 10,
    "amount": 10,
    "currency": "uSd"
}

```

##### Example Response

```json

{
    "code": "aAa12345gjJggdjhMMfsms7",
    "expirationDate": "2024-10-10",
    "amount": 10,
    "currency": "USD",
    "discountMethod": "PERCENTAGE"
}

```

-----

#### `POST v1/api/purchases` endpoint

To simulate new purchase.

##### Example Request body

```json

{
    "productId": "1",
    "promoCode": "aAa12345gjJggdjhMMfsms7"
}

```

##### Example Response

```json

{
    "regularPrice": 10.99,
    "discountAmount": 1.10,
    "date": "2024-05-13",
    "product": {
        "id": 1,
        "name": "Product 2",
        "description": "Product 2 description",
        "price": 10.99,
        "currency": "USD"
    },
    "warning": null
}

```

-----

#### `GET v1/api/sales/report` endpoint

To generate sales report.

##### Example Response

```json

[
    {
        "currency": "EUR",
        "totalAmount": 10.99,
        "totalDiscount": 10.99,
        "numberOfPurchases": 1
    },
    {
        "currency": "USD",
        "totalAmount": 10.99,
        "totalDiscount": 1.10,
        "numberOfPurchases": 1
    }
]

```

-----

### Tech Stack

* Java 21
* Spring Boot 3
* Spring Web
* Spring Data
* Hibernate Validator
* H2
* Lombok
* JUnit 5
* Mockito
* AssertJ
* Rest Assured
* Hamcrest
