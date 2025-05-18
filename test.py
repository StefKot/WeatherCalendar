import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from database import Base, get_db
from main import app

# Используем in-memory SQLite для тестов
SQLALCHEMY_DATABASE_URL = "sqlite:///./weather_calendar.db"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(
    autocommit=False, autoflush=False, bind=engine
)

# Создаем и удаляем схемы перед/после тестового модуля
@pytest.fixture(scope="module", autouse=True)
def prepare_database():
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)

@pytest.fixture(scope="module")
def db_session():
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()

@pytest.fixture(scope="module")
def client(db_session):
    # Переопределяем зависимость get_db для приложения
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    return TestClient(app)


def test_register_login_and_status(client):
    # Регистрация пользователя
    resp = client.post(
        "/auth/register",
        json={"username": "alice", "password": "secret123"}
    )
    assert resp.status_code == 201
    data = resp.json()
    assert data["username"] == "alice"
    assert "message" in data

    # Логин пользователя
    resp = client.post(
        "/auth/login",
        json={"username": "alice", "password": "secret123"}
    )
    assert resp.status_code == 200
    token_data = resp.json()
    assert "access_token" in token_data
    assert token_data["token_type"] == "bearer"

    access_token = token_data["access_token"]
    headers = {"Authorization": f"Bearer {access_token}"}

    # Проверка статуса аутентификации
    resp = client.get("/auth/status", headers=headers)
    assert resp.status_code == 200
    user_info = resp.json()
    assert user_info["username"] == "alice"
    assert "id" in user_info

    # Смена пароля
    resp = client.put(
        "/auth/reset_password",
        headers=headers,
        json={"username": "alice", "new_password": "newpass456"}
    )
    assert resp.status_code == 200
    assert resp.json()["message"] == "Пароль успешно сброшен"

    # Логин с новым паролем
    resp = client.post(
        "/auth/login",
        json={"username": "alice", "password": "newpass456"}
    )
    assert resp.status_code == 200

from datetime import date, time

def get_token(client):
    # Вспомогательная ф-я для получения токена тестового пользователя
    client.post("/auth/register", json={"username": "bob", "password": "hunter2"})
    resp = client.post("/auth/login", json={"username": "bob", "password": "hunter2"})
    return resp.json()["access_token"]

def test_observations_crud_and_filters(client):
    token = get_token(client)
    headers = {"Authorization": f"Bearer {token}"}

    # Без токена доступ к /observations запрещен
    resp = client.get("/observations/")
    assert resp.status_code == 401

    # Создаем новую запись
    obs_payload = {
        "city": "Moscow",
        "observation_date": "2024-01-01",
        "observation_time": "12:30:00",
        "temperature": -5.0,
        "precipitation_type": "Снег"
    }
    resp = client.post("/observations/", headers=headers, json=obs_payload)
    assert resp.status_code == 201
    obs = resp.json()
    assert obs["city"] == "Moscow"
    assert obs["temperature"] == -5.0
    assert "id" in obs and "user_id" in obs

    # Получаем все записи пользователя
    resp = client.get("/observations/", headers=headers)
    assert resp.status_code == 200
    all_obs = resp.json()
    assert isinstance(all_obs, list) and len(all_obs) >= 1

    # Фильтрация по дате
    resp = client.get("/observations/", headers=headers, params={"obs_date": "2024-01-01"})
    assert resp.status_code == 200
    assert all(o["observation_date"] == "2024-01-01" for o in resp.json())

    # Некорректная комбинация параметров дат
    resp = client.get(
        "/observations/",
        headers=headers,
        params={"obs_date": "2024-01-01", "start_date": "2024-01-01"}
    )
    assert resp.status_code == 400

    # Диапазон дат
    resp = client.get(
        "/observations/",
        headers=headers,
        params={"start_date": "2024-01-01", "end_date": "2024-01-02"}
    )
    assert resp.status_code == 200