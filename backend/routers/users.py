from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from database.connection import get_db
from schemas.user import UserCreate, UserResponse
from schemas.stats import UserStatsResponse, GameScoreData
from database.crud import user_crud, daily_journal_crud, game_session_crud 
from utils.dependencies import get_current_user
from datetime import datetime, timedelta, timezone

router = APIRouter(tags=["Users"])

@router.post(
    "/create",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create user profile",
    description="Creates a user profile in the database after Firebase sign-up. Requires a valid Firebase ID token."
)
async def create_user_profile(
    user_data: UserCreate,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """
    Creates a new user profile linked to the authenticated Firebase user.
    This endpoint should be called once, typically after the user signs up for the first time.

    - **user_data**: The user's profile information.
    - **current_user**: Injected by the `get_current_user` dependency, contains decoded token data.
    """
    user_uid = current_user.get("uid")

    # Check if a user profile already exists for this UID
    db_user = await user_crud.get_user_by_id(db=db, user_id=user_uid)
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="User profile already exists for this user."
        )

    # If not, create the new user profile
    new_user = await user_crud.create_user(db=db, user_data=user_data, user_id=user_uid)
    return new_user

@router.get(
    "/me",
    response_model=UserResponse,
    summary="Get current user's profile",
    description="Fetches the profile of the currently authenticated user from the database. Requires a valid Firebase ID token."
)
async def get_current_user_profile(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    """
    Retrieves the profile of the user corresponding to the provided Firebase ID token.
    If the user has authenticated but has not created a profile via POST /users/create,
    this will result in a 404 Not Found error.
    """
    user_uid = current_user.get("uid")
    user_profile = await user_crud.get_user_by_id(db=db, user_id=user_uid)

    if user_profile is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User profile not found. Please create a profile first."
        )

    return user_profile


@router.get(
    "/me/stats",
    response_model=UserStatsResponse,
    summary="Get user's statistics",
    description="Retrieves journal streak, last five scores, and last ten game stats for the graph."
)
async def get_user_stats(
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    user_uid = current_user.get("uid")

    # --- 1. Calculate Journal Streak ---
    journals = await daily_journal_crud.get_journals_by_user_id(db, user_id=user_uid)
    journal_streak = 0
    if journals:
        # Get unique dates of journal entries
        journal_dates = sorted(list(set([j.created_at.date() for j in journals])), reverse=True)

        today = datetime.now(timezone.utc).date()
        yesterday = today - timedelta(days=1)

        # Check if the most recent journal is from today or yesterday
        if journal_dates[0] == today or journal_dates[0] == yesterday:
            journal_streak = 1
            current_day = journal_dates[0]
            for i in range(1, len(journal_dates)):
                if journal_dates[i] == current_day - timedelta(days=1):
                    journal_streak += 1
                    current_day = journal_dates[i]
                else:
                    break # Streak is broken

    # --- 2. Get Game Stats ---
    game_sessions = await game_session_crud.get_game_sessions_by_user_id(db, user_id=user_uid)

    # Get last 5 scores for the top display
    last_five_scores = [session.score for session in game_sessions[:5]]

    # Get stats for the last 10 games for the graph
    # Reverse them so the graph shows progression over time (oldest to newest)
    last_ten_sessions = game_sessions[:10]
    last_ten_sessions.reverse()

    last_ten_game_stats = [
        GameScoreData(score=session.score, level=session.difficulty_level)
        for session in last_ten_sessions
    ]

    return UserStatsResponse(
        journal_streak=journal_streak,
        last_five_scores=last_five_scores,
        last_ten_game_stats=last_ten_game_stats
    )