# routers/auth.py
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

import schemas, models, security
from database import get_db

# Создаем роутер с префиксом "/auth" и тегами для документации
router = APIRouter(
    prefix="/auth",
    tags=["auth"],
)

@router.post("/register", response_model=schemas.UserRegisterResponse, status_code=status.HTTP_201_CREATED)
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    """Регистрация нового пользователя."""
    # Проверяем, существует ли пользователь с таким именем
    db_user = db.query(models.User).filter(models.User.username == user.username).first()
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Пользователь с таким именем уже зарегистрирован"
        )

    # Хешируем пароль
    hashed_password = security.get_password_hash(user.password)

    # Создаем нового пользователя в базе данных
    db_user = models.User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user) # Обновляем объект db_user, чтобы получить сгенерированный ID

    # Возвращаем данные пользователя (без хешированного пароля)
    return {
        "username": db_user.username,
        "message": "Пользователь успешно зарегистрирован"
    }


@router.post("/login", response_model=schemas.Token)
def login_for_access_token(user: schemas.UserLogin, db: Session = Depends(get_db)):
    """Вход пользователя и получение JWT токена."""
    # Находим пользователя по имени
    db_user = db.query(models.User).filter(models.User.username == user.username).first()
    if not db_user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверное имя пользователя или пароль",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Проверяем пароль
    if not security.verify_password(user.password, db_user.hashed_password):
         raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверное имя пользователя или пароль",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Если логин и пароль верны, создаем токен
    access_token = security.create_access_token(
        data={"sub": str(db_user.id)} # Передаем ID как строку
    )

    # Возвращаем токен
    return {"access_token": access_token, "token_type": "bearer"}

@router.put("/reset_password", status_code=status.HTTP_200_OK)
def reset_password(
    data: schemas.ResetPassword,
    db: Session = Depends(get_db)
):
    """Сброс пароля по логину без знания старого пароля."""
    # Ищем пользователя по username
    db_user = db.query(models.User).filter(models.User.username == data.username).first()
    if not db_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Пользователь с таким именем не найден"
        )

    # Хешируем и сохраняем новый пароль
    db_user.hashed_password = security.get_password_hash(data.new_password)
    db.commit()

    return {"message": "Пароль успешно сброшен"}

@router.get("/status", response_model=schemas.UserResponse) # Можно использовать схему для ответа, например UserResponse
def check_auth_status(
    # Используем зависимость get_current_user.
    current_user: models.User = Depends(security.get_current_user)
):
    """Проверка валидности JWT токена и получение информации о текущем пользователе."""
    return current_user