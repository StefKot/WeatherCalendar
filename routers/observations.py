# routers/observations.py
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_, between
from datetime import date, time # Импортируем date и time
from typing import List, Optional

import schemas, models, security
from database import get_db

router = APIRouter(
    prefix="/observations",
    tags=["observations"],
    dependencies=[Depends(security.get_current_user)]
)

@router.post("/", response_model=schemas.WeatherObservation, status_code=status.HTTP_201_CREATED)
def create_observation(
    observation: schemas.WeatherObservationCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(security.get_current_user)
):
    """Создать новую запись о погоде."""
    db_observation = models.WeatherObservation(
        **observation.model_dump(), # Используем model_dump для получения словаря
        user_id=current_user.id
    )
    db.add(db_observation)
    db.commit()
    db.refresh(db_observation)
    return db_observation

@router.get("/", response_model=List[schemas.WeatherObservation])
def read_observations(
    # Параметры запроса. Optional означает, что параметр необязательный.
    obs_date: Optional[date] = Query(None, description="Дата наблюдения (YYYY-MM-DD)"),
    start_date: Optional[date] = Query(None, description="Начало периода (YYYY-MM-DD)"),
    end_date: Optional[date] = Query(None, description="Конец периода (YYYY-MM-DD)"),
    city: Optional[str] = Query(None, description="Фильтр по городу"),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(security.get_current_user)
):
    """
    Получить записи о погоде для текущего пользователя.
    Можно фильтровать по конкретной дате ИЛИ по диапазону дат.
    Можно также фильтровать по городу.
    """
    # Проверка на некорректную комбинацию параметров даты
    if obs_date and (start_date or end_date):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя указывать 'date' одновременно с 'start_date'/'end_date'."
        )
    if (start_date and not end_date) or (not start_date and end_date):
         raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Для диапазона дат необходимо указать и 'start_date', и 'end_date'."
        )
    if start_date and end_date and start_date > end_date:
         raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="'start_date' не может быть позднее 'end_date'."
        )

    # Базовый запрос: получить все записи текущего пользователя
    query = db.query(models.WeatherObservation).filter(
        models.WeatherObservation.user_id == current_user.id
    )

    # Добавляем фильтры в зависимости от параметров
    if obs_date:
        # Фильтр по конкретной дате
        query = query.filter(models.WeatherObservation.observation_date == obs_date)
    elif start_date and end_date:
        # Фильтр по диапазону дат
        query = query.filter(
            models.WeatherObservation.observation_date.between(start_date, end_date)
        )

    if city:
        # Фильтр по городу (применяется вместе с фильтром по дате/периоду или отдельно)
        query = query.filter(models.WeatherObservation.city == city)

    # Выполняем запрос и возвращаем результат
    observations = query.all()

    return observations