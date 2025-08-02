from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from database.connection import get_db
from schemas.game_session import GameSessionCreate, GameSessionResponse, GameSessionUpdate
from schemas.game_session import GameParametersResponse
from utils.difficulty_adjuster import calculate_next_difficulty_level, DIFFICULTY_LEVELS, MAX_LEVEL
from database.crud import game_session_crud
from typing import List, Optional
from uuid import UUID
from fastapi import HTTPException
from utils.dependencies import get_current_user

router = APIRouter(
    tags=["Game Sessions"]
)

# API endpoint to create and store a new game session
@router.post("/game-sessions", status_code=status.HTTP_201_CREATED)
async def create_game_session_endpoint(
    game_session_data: GameSessionCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")
    
    new_session = await game_session_crud.create_game_session(
        db=db, 
        game_session_data=game_session_data, 
        user_id=user_uid
    )
    return {
        "message": "Game session created successfully",
        "game_session_id": str(new_session.id)
    }

# GET endpoint to list all game sessions
@router.get("/game-sessions", response_model=List[GameSessionResponse])
async def get_all_game_sessions_endpoint(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")
    game_sessions = await game_session_crud.get_game_sessions_by_user_id(db, user_id=user_uid)
    return game_sessions

# GET endpoint to retrieve a single game session by ID
@router.get("/game-sessions/{session_id}", response_model=GameSessionResponse)
async def get_game_session_by_id_endpoint(
    session_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")
    
    game_session = await game_session_crud.get_game_session_by_id(db, session_id)
    if game_session is None:
        raise HTTPException(status_code=404, detail="Game session not found")
    
    if game_session.user_id != user_uid:
        raise HTTPException(status_code=403, detail="Not authorized to access this resource")
    
    return game_session

# DELETE endpoint to delete a game session by ID
@router.delete("/game-sessions/{session_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_game_session_by_id_endpoint(
    session_id: UUID, 
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")
    
    game_session = await game_session_crud.get_game_session_by_id(db, session_id)
    if game_session is None:
        raise HTTPException(status_code=404, detail="Game session not found")
    
    if game_session.user_id != user_uid:
        raise HTTPException(status_code=403, detail="Not authorized to access this resource")
    
    deleted = await game_session_crud.delete_game_session_by_id(db, session_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Game session not found")

# PUT endpoint to update a game session by ID
@router.put("/game-sessions/{session_id}", response_model=GameSessionResponse)
async def update_game_session_by_id_endpoint(
    session_id: UUID,
    update_data: GameSessionUpdate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")
    
    game_session = await game_session_crud.get_game_session_by_id(db, session_id)
    if game_session is None:
        raise HTTPException(status_code=404, detail="Game session not found")
    
    if game_session.user_id != user_uid:
        raise HTTPException(status_code=403, detail="Not authorized to access this resource")
    
    updated_session = await game_session_crud.update_game_session_by_id(db, session_id, update_data)
    return updated_session

@router.get("/new-session-parameters", response_model=GameParametersResponse)
async def get_new_game_parameters(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user),
    from_level: Optional[int] = Query(None, description="The level the user just completed. If provided, returns parameters for the next level.")
):
    """
    Calculates and returns parameters for a game round.
    - If 'from_level' is provided, it returns parameters for the next sequential level.
    - If 'from_level' is not provided, it calculates the best starting level based on user history.
    """
    user_uid = current_user.get("uid")
    next_level: int

    if from_level is not None:
        next_level = from_level + 1
    else:
        previous_sessions = await game_session_crud.get_game_sessions_by_user_id(db, user_id=user_uid)
        next_level = calculate_next_difficulty_level(previous_sessions)

    if next_level > MAX_LEVEL:
        raise HTTPException(
            status_code=404,
            detail=f"Congratulations! You have completed all {MAX_LEVEL} levels."
        )

    parameters = DIFFICULTY_LEVELS.get(next_level)
    if not parameters:
        raise HTTPException(status_code=500, detail="Could not determine difficulty parameters for level {next_level}.")

    return GameParametersResponse(difficulty_level=next_level, **parameters)