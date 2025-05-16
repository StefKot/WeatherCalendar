# database.py
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL для подключения к базе данных SQLite
# `./weather_calendar.db` означает файл в той же директории
SQLALCHEMY_DATABASE_URL = "sqlite:///./weather_calendar.db"

# Создаем движок SQLAlchemy
# connect_args={"check_same_thread": False} нужен только для SQLite
# по умолчанию SQLite разрешает только один поток взаимодействовать с ним
# если вы используете другой СУБД (PostgreSQL, MySQL), то это не нужно
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)

# Создаем фабрику сессий
# autocommit=False: мы будем явно вызывать commit()
# autoflush=False: изменения не будут сбрасываться в БД автоматически
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