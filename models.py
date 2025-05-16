# models.py
from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Float, Date, Time
from sqlalchemy.orm import relationship

# Импортируем базовый класс из database.py
from database import Base

# Модель пользователя
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)

    # Отношение к записям о погоде (опционально, но удобно)
    observations = relationship("WeatherObservation", back_populates="owner")

# Модель записи о погоде
class WeatherObservation(Base):
    __tablename__ = "weather_observations"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id")) # Внешний ключ на таблицу users
    city = Column(String, index=True)
    observation_date = Column(Date, index=True)
    observation_time = Column(Time)
    temperature = Column(Float)
    precipitation_type = Column(String) # Будем хранить как строку ('Ясно', 'Дождь', 'Снег', 'Град')

    # Отношение к пользователю
    owner = relationship("User", back_populates="observations")