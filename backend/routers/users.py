from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from database.connection import get_db
from schemas.user import UserCreate, UserResponse
from database.crud import user_crud
from utils.dependencies import get_current_user

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