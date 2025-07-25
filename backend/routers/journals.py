from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from database.connection import get_db
from schemas.daily_journal import DailyJournalCreate, DailyJournalResponse
from database.crud import daily_journal_crud, user_crud, game_session_crud 
from utils.dependencies import get_current_user
from utils.gemini_client import construct_prompt, generate_ai_response 
from typing import List

router = APIRouter(
    tags=["Daily Journals"],
    prefix="/journals",
    dependencies=[Depends(get_current_user)]
)

@router.post("/", response_model=DailyJournalResponse, status_code=status.HTTP_201_CREATED)
async def create_journal_and_get_ai_response(
    journal_data: DailyJournalCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """
    This endpoint does two things:
    1. Saves the user's journal entry to the database.
    2. Gathers context (profile, last game), calls Gemini, and updates the entry with the AI response.
    3. Returns the complete journal entry with the AI response.
    """
    user_uid = current_user.get("uid")

    new_journal_entry = await daily_journal_crud.create_journal_entry(
        db=db, journal_data=journal_data, user_id=user_uid
    )
    user_profile = await user_crud.get_user_by_id(db, user_id=user_uid)
    if not user_profile:
        raise HTTPException(status_code=404, detail="User profile not found, cannot generate AI response.")

    latest_game_sessions = await game_session_crud.get_game_sessions_by_user_id(db, user_id=user_uid)
    last_score = latest_game_sessions[0].score if latest_game_sessions else 5 
    prompt_text = construct_prompt(
        full_name=user_profile.full_name,
        interests=user_profile.interests,
        last_game_score=last_score,
        journal_content=journal_data.journal_content
    )
    
    ai_response_text = await generate_ai_response(prompt_text)

    updated_journal = await daily_journal_crud.update_journal_with_ai_data(
        db=db,
        journal_id=new_journal_entry.id,
        prompt=prompt_text,
        response=ai_response_text
    )

    if not updated_journal:
         raise HTTPException(status_code=500, detail="Failed to update journal with AI response.")

    return updated_journal


@router.get("/", response_model=List[DailyJournalResponse])
async def get_my_journals(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """
    Retrieves all journal entries for the currently authenticated user.
    """
    user_uid = current_user.get("uid")
    journals = await daily_journal_crud.get_journals_by_user_id(db, user_id=user_uid)
    return journals