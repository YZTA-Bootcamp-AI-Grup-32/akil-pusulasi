from sqlalchemy.ext.asyncio import AsyncSession
from models.game_session import GameSession
from schemas.game_session import GameSessionCreate, GameSessionResponse, GameSessionUpdate
from typing import Optional, List
from sqlalchemy.future import select
from models.game_session import GameSession
from sqlalchemy import select, delete
from uuid import UUID

# Create and save a new game session to the database
async def create_game_session(db: AsyncSession, game_session_data: GameSessionCreate):
    new_session = GameSession(
        user_id=game_session_data.user_id,
        game_name=game_session_data.game_name,
        score=game_session_data.score,
        duration_seconds=game_session_data.duration_seconds,
        difficulty_level=game_session_data.difficulty_level
    )
    db.add(new_session)
    await db.commit()
    await db.refresh(new_session)
    return new_session

# Retrieve all GameSession records from the database
async def get_all_game_sessions(db: AsyncSession) -> List[GameSession]:
    result = await db.execute(select(GameSession))
    game_sessions = result.scalars().all()
    return game_sessions

# Retrieve a single game session by its ID
async def get_game_session_by_id(db: AsyncSession, session_id: UUID) -> GameSession:
    result = await db.execute(select(GameSession).where(GameSession.id == session_id))
    game_session = result.scalars().first()
    return game_session

# Delete a single game session by its ID
async def delete_game_session_by_id(db: AsyncSession, session_id: UUID) -> bool:
    result = await db.execute(delete(GameSession).where(GameSession.id == session_id))
    await db.commit()
    # If rows were deleted, result.rowcount will be > 0
    return result.rowcount > 0

# Update a single game session by its ID
async def update_game_session_by_id(db: AsyncSession, session_id: UUID, update_data: GameSessionUpdate) -> Optional[GameSession]:
    result = await db.execute(select(GameSession).where(GameSession.id == session_id))
    game_session = result.scalars().first()
    if game_session is None:
        return None

    # Update only the fields provided in the request
    if update_data.game_name is not None:
        game_session.game_name = update_data.game_name
    if update_data.score is not None:
        game_session.score = update_data.score
    if update_data.duration_seconds is not None:
        game_session.duration_seconds = update_data.duration_seconds
    if update_data.difficulty_level is not None:
        game_session.difficulty_level = update_data.difficulty_level

    await db.commit()
    await db.refresh(game_session)
    return game_session

