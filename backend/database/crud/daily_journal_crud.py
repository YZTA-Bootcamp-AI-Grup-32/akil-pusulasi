from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from models.daily_journal import DailyJournal
from schemas.daily_journal import DailyJournalCreate
from uuid import UUID
from typing import List

async def create_journal_entry(db: AsyncSession, journal_data: DailyJournalCreate, user_id: str) -> DailyJournal:
    new_entry = DailyJournal(
        user_id=user_id,
        journal_content=journal_data.journal_content
    )
    db.add(new_entry)
    await db.commit()
    await db.refresh(new_entry)
    return new_entry

async def get_journals_by_user_id(db: AsyncSession, user_id: str) -> List[DailyJournal]:
    result = await db.execute(
        select(DailyJournal)
        .where(DailyJournal.user_id == user_id)
        .order_by(DailyJournal.created_at.desc())
    )
    return result.scalars().all()

async def get_journal_by_id(db: AsyncSession, journal_id: UUID) -> DailyJournal | None:
    result = await db.execute(select(DailyJournal).where(DailyJournal.id == journal_id))
    return result.scalars().first()

async def update_journal_with_ai_data(db: AsyncSession, journal_id: UUID, prompt: str, response: str) -> DailyJournal | None:
    """
    Updates a journal entry with the prompt sent to and the response received from the AI.
    """
    result = await db.execute(
        update(DailyJournal)
        .where(DailyJournal.id == journal_id)
        .values(ai_prompt=prompt, ai_response=response)
        .returning(DailyJournal)
    )
    await db.commit()
    return result.scalar_one_or_none()