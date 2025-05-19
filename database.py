# database.py
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL для подключения к базе данных SQLite
SQLALCHEMY_DATABASE_URL = "sqlite:///./weather_calendar.db"

# Создаем движок SQLAlchemy

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)

# Создаем фабрику сессий
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Базовый класс для декларативного определения моделей
Base = declarative_base()

# Dependency для получения сессии БД
def get_db():
    """
    Предоставляет сессию базы данных для каждого запроса.
    Закрывает сессию после завершения запроса.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()