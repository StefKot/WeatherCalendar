# Weather Calendar API

Приложение Weather Calendar API позволяет зарегистрированным пользователям сохранять и просматривать записи о погоде по городам и датам.

## Содержание

- [Технологии](#технологии)  
- [Установка](#установка)  
- [Запуск](#запуск)  
- [Структура проекта](#структура-проекта)  
- [API эндпоинты](#api-эндпоинты)  
- [Модели и схемы](#модели-и-схемы)  
- [Лицензия](#лицензия)  

## Технологии

- Python 3.12  
- FastAPI  
- SQLAlchemy  
- SQLite  
- Pydantic  
- python-jose (JWT)  
- passlib (bcrypt)

## Установка

1. Склонируйте репозиторий и перейдите в папку проекта:

   ```bash
   git clone <repo_url>
   cd WeatherCalendar
   ```

2. Создайте и активируйте виртуальное окружение:  
   Windows:

   ```bash
   python -m venv .venv
   .venv\Scripts\activate
   ```  

   Linux/macOS:

   ```bash
   python -m venv .venv
   source .venv/bin/activate
   ```

3. Установите зависимости:

   ```bash
   pip install -r requirements.txt
   ```

## Запуск

Запустите сервер:

```bash
uvicorn main:app --reload
```

После запуска:

- Документация Swagger UI доступна по адресу <http://127.0.0.1:8000/docs>  
- Redoc по адресу <http://127.0.0.1:8000/redoc>  

## Структура проекта

```text

├── database.py           # [database.py](database.py) – настройка SQLAlchemy
├── main.py               # [main.py](main.py) – создание FastAPI приложения
├── models.py             # [models.py](models.py) – ORM модели
├── schemas.py            # [schemas.py](schemas.py) – Pydantic схемы
├── security.py           # [security.py](security.py) – JWT и хеширование
├── routers/
│   ├── auth.py           # [routers/auth.py](routers/auth.py) – регистрация и аутентификация
│   └── observations.py   # [routers/observations.py](routers/observations.py) – CRUD записей о погоде
├── requirements.txt      # [requirements.txt](requirements.txt) – зависимости
└── weather_calendar.db   # sqlite база данных (игнорируется .gitignore)
```

## API эндпоинты

### Аутентификация (`/auth`)

- `POST /auth/register`  
  Регистрация пользователя.  
  Вход: [`UserCreate`](schemas.py#L5)  
  Ответ: данные созданного пользователя.

- `POST /auth/login`  
  Логин и получение JWT-токена.  
  Вход: [`UserLogin`](schemas.py#L11)  
  Ответ: [`Token`](schemas.py#L15)

- `PUT /auth/change_password`  
  Смена пароля текущего пользователя (Bearer).  
  Вход: [`ChangePassword`](schemas.py#L19)

- `GET /auth/status`  
  Проверка валидности JWT-токена и получение информации о текущем пользователе (Bearer).  
  Ответ: [`UserResponse`](schemas.py#L17)

### Записи о погоде (`/observations`)

Все эндпоинты `/observations` защищены JWT (Bearer).

- `POST /observations/`  
  Создать запись.  
  Вход: [`WeatherObservationCreate`](schemas.py#L24)  
  Ответ: [`WeatherObservation`](schemas.py#L31)

- `GET /observations/`  
  Получить список записей с фильтрацией:
  - `obs_date` – конкретная дата
  - `start_date`/`end_date` – диапазон дат
  - `city` – фильтр по городу

## Модели и схемы

- ORM модели в [models.py](models.py)  
- Pydantic схемы в [schemas.py](schemas.py)  
- Сессия БД и базовый класс в [database.py](database.py)  
- Безопасность и JWT в [security.py](security.py)

## Лицензия

Проект распространяется под MIT License.
