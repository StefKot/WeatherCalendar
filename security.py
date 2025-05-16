# security.py
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt

# Импортируем модели и схемы
import schemas, models
from database import get_db
from sqlalchemy.orm import Session

# Настройки для хеширования паролей
# Используем bcrypt - рекомендуемый алгоритм
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Функции для хеширования и проверки паролей
def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверяет соответствие открытого пароля хешированному."""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    """Хеширует открытый пароль."""
    return pwd_context.hash(password)

# Настройки для JWT токенов
# В ПРОДАКШЕНЕ СЕКРЕТНЫЙ КЛЮЧ НУЖНО ХРАНИТЬ В ПЕРЕМЕННЫХ ОКРУЖЕНИЯ!
SECRET_KEY = "ВАШ_СЕКРЕТНЫЙ_КЛЮЧ" # <-- ЗАМЕНИТЕ ЭТОТ КЛЮЧ НА СВОЙ В ПРОДАКШЕНЕ
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30 # Срок действия токена в минутах

# Создаем схему OAuth2 для извлечения токена из заголовка Authorization
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

# Функция для создания JWT токена
def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    """Создает JWT токен доступа."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        # По умолчанию токен действует ACCESS_TOKEN_EXPIRE_MINUTES минут
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# Функция для получения текущего аутентифицированного пользователя по токену
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    """
    Извлекает пользователя из токена доступа.
    Является зависимостью FastAPI.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Не удалось проверить учетные данные",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # Декодируем токен
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        # Извлекаем имя пользователя из полезной нагрузки токена (subject)
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
        # Создаем модель TokenData для валидации
        token_data = schemas.TokenData(username=username)
    except JWTError:
        raise credentials_exception # Ошибка при декодировании токена

    # Находим пользователя в базе данных по имени пользователя
    user = db.query(models.User).filter(models.User.username == token_data.username).first()
    if user is None:
        raise credentials_exception # Пользователь не найден (хотя токен валиден)

    return user # Возвращаем объект пользователя SQLAlchemy