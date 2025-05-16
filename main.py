# main.py
from fastapi import FastAPI

# Импортируем роутеры, базу данных и модели
from routers import auth, observations
import models
from database import engine

# Создаем все таблицы в базе данных, если они еще не созданы
# Это должно происходить при запуске приложения
models.Base.metadata.create_all(bind=engine)

# Создаем экземпляр приложения FastAPI
app = FastAPI(
    title="Weather Calendar API", # Название для документации Swagger UI
    description="Backend API for the Weather Calendar application",
    version="1.0.0",
)

# Подключаем роутеры к основному приложению
app.include_router(auth.router)
app.include_router(observations.router)

# Опциональный корневой эндпоинт для проверки работы сервера
@app.get("/")
def read_root():
    return {"message": "Weather Calendar API is running"}