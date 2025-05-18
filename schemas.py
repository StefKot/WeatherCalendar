# schemas.py
from typing import List, Optional, Literal
from datetime import date, time
from pydantic import BaseModel, Field

# Определение типов осадков (используем Literal для строгой валидации)
PrecipitationType = Literal['Ясно', 'Дождь', 'Снег', 'Град']

# --- Схемы для пользователя и аутентификации ---

# Схема для создания пользователя (регистрация)
class UserCreate(BaseModel):
    username: str = Field(..., min_length=3, max_length=30)
    password: str = Field(..., min_length=6) # Требования к паролю можно усилить

# Схема для входа пользователя
class UserLogin(BaseModel):
    username: str
    password: str

class UserRegisterResponse(BaseModel):
    username: str
    message: str

# Схема для токена доступа (ответ после входа)
class Token(BaseModel):
    access_token: str
    token_type: str

# Схема данных, извлекаемых из токена
class TokenData(BaseModel):
    id: Optional[int] = None

# Схема для смены пароля
class ChangePassword(BaseModel):
    current_password: str
    new_password: str = Field(..., min_length=6)

class UserResponse(BaseModel):
    id: int
    username: str
    class Config:
        from_attributes = True

class ResetPassword(BaseModel):
    username: str
    new_password: str = Field(..., min_length=6)

# --- Схемы для записей о погоде ---

# Схема для создания записи о погоде (входящие данные)
class WeatherObservationCreate(BaseModel):
    city: str = Field(..., min_length=1)
    observation_date: date # Pydantic автоматически преобразует строку в date, если формат корректный
    observation_time: time # Pydantic автоматически преобразует строку в time
    temperature: float
    precipitation_type: PrecipitationType # Используем Literal для валидации

# Схема для ответа (исходящие данные)
class WeatherObservation(WeatherObservationCreate):
    id: int
    user_id: int

    # Конфигурация для работы с ORM (SQLAlchemy)
    # В Pydantic v2 используется from_attributes=True вместо orm_mode=True
    class Config:
        from_attributes = True