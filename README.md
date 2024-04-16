## Проект BankServer

Тестовое задание INFINITE SYNERGY
https://docs.google.com/document/d/1JMjPzsGxOSsDkHqbkFCzG9KmuoBjGnV1/edit

### Запуск приложения

Чтобы запустить приложение, выполните следующие шаги:

1. **Клонирование репозитория**: Склонируйте репозиторий с помощью команды:

    ```bash
    git clone https://github.com/vladilshk/bank-server-test-task.git
    ```

2. **Запуск контейнеров Docker**: Выполните следующую команду для запуска приложения с помощью Docker Compose:

    ```bash
    docker-compose up
    ```

3. **Запуск BankServer**: После запуска контейнеров перейдите в каталог `bankserver` и запустите приложение.


4. **Подключение к BankServer**: Страндартно приложение запускается на [http://localhost:8080](http://localhost:8080).

### Описание API

BankServer предоставляет следующие точки входа API:

1. **Регистрация пользователя**:

    - **Метод**: POST
    - **Путь**: /signup
    - **Тело запроса**: JSON с полями `login` и `password`
    - **Пример запроса**:
      ```json
      {
        "login": "example_user",
        "password": "password123"
      }
      ```
    - **Пример ответа**:
      ```json
      {
        "message": "Registration is successful"
      }
      ```

2. **Аутентификация пользователя**:

    - **Метод**: POST
    - **Путь**: /signin
    - **Тело запроса**: JSON с полями `login` и `password`
    - **Пример запроса**:
      ```json
      {
        "login": "example_user",
        "password": "password123"
      }
      ```
    - **Пример ответа**:
      ```json
      {
        "token": "token"
      }
      ```

3. **Получение баланса пользователя**:

    - **Метод**: GET
    - **Путь**: /money
    - **Требуемый заголовок**: Authorization: Bearer {token}
    - **Пример запроса**: GET /money
    - **Пример ответа**:
      ```json
      {
        "balance": 1000
      }
      ```

4. **Перевод средств между пользователями**:

    - **Метод**: POST
    - **Путь**: /money
    - **Требуемый заголовок**: Authorization: Bearer {token}
    - **Тело запроса**: JSON с полями `to` и `amount`
    - **Пример запроса**:
      ```json
      {
        "to": "recipient_user",
        "amount": 500
      }
      ```
    - **Пример ответа**:
      ```json
      {
        "message": "Transaction complete!"
      }
      ```
