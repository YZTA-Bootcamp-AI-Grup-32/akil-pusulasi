from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from models.user import User
from schemas.user import UserCreate
from typing import Optional


async def get_user_by_id(db: AsyncSession, user_id: str) -> Optional[User]:
    """
    Retrieves a user from the database by their Firebase UID.
    """
    result = await db.execute(select(User).where(User.id == user_id))
    return result.scalars().first()

async def create_user(db: AsyncSession, user_data: UserCreate, user_id: str) -> User:
    """
    Creates a new user record in the database.
    The user_id is the Firebase UID.
    """
    new_user = User(
        id=user_id,
        full_name= user_data.full_name,
        birth_year= user_data.birth_year,
        interests=user_data.interests
    )
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)
    return new_user