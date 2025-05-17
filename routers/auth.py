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

@router.post("/register", response_model=schemas.UserCreate, status_code=status.HTTP_201_CREATED)
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
    # Используем схему UserCreate для ответа, так как она соответствует структуре
    # return user # Возвращать объект user может быть не совсем правильно, так как он содержит пароль в Pydantic модели.
    # Лучше вернуть только username или ID, или использовать отдельную схему ответа.
    # Например, просто подтверждение регистрации:
    return {"message": "Пользователь успешно зарегистрирован", "username": db_user.username}


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
    # sub (subject) в токене - обычно уникальный идентификатор пользователя (можно использовать username или id)
    # Рекомендуется использовать ID пользователя, так как username может потенциально измениться
    access_token = security.create_access_token(
        data={"sub": str(db_user.id)} # Передаем ID как строку
    )

    # Возвращаем токен
    return {"access_token": access_token, "token_type": "bearer"}

@router.put("/change_password", status_code=status.HTTP_200_OK)
def change_password(
    passwords: schemas.ChangePassword,
    db: Session = Depends(get_db),
    # Защищаем эндпоинт: только аутентифицированный пользователь может сменить пароль
    current_user: models.User = Depends(security.get_current_user)
):
    """Смена пароля текущего пользователя."""
    # Проверяем текущий пароль
    if not security.verify_password(passwords.current_password, current_user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Неверный текущий пароль"
        )

    # Хешируем новый пароль
    new_hashed_password = security.get_password_hash(passwords.new_password)

    # Обновляем пароль в базе данных
    current_user.hashed_password = new_hashed_password
    db.commit()
    # db.refresh(current_user) # Не обязательно для PUT, если не возвращаем объект

    return {"message": "Пароль успешно изменен"}

# --- НОВЫЙ ЭНДПОИНТ ДЛЯ ПРОВЕРКИ СТАТУСА ---
@router.get("/status", response_model=schemas.UserResponse) # Можно использовать схему для ответа, например UserResponse
def check_auth_status(
    # Используем зависимость get_current_user.
    # Если токен невалиден, эта зависимость автоматически вызовет 401 Unauthorized.
    # Если токен валиден, current_user будет содержать объект пользователя из БД.
    current_user: models.User = Depends(security.get_current_user)
):
    """Проверка валидности JWT токена и получение информации о текущем пользователе."""
    # Если мы дошли до этой строки, значит get_current_user успешно выполнился,
    # то есть токен валиден и пользователь найден.
    # Возвращаем информацию о пользователе, исключая хешированный пароль.
    # Схема UserResponse должна быть определена в schemas.py и не включать hashed_password.
    return current_user