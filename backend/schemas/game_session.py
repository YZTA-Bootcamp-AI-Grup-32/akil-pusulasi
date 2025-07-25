from pydantic import BaseModel, ConfigDict
from typing import Optional
from datetime import datetime
from uuid import UUID

# Request model
class GameSessionCreate(BaseModel):
    game_name: Optional[str] = "Hafiza Kartlari"
    score: int
    duration_seconds: int
    difficulty_level: int

# Response model
class GameSessionResponse(BaseModel):
    id: UUID  # IMPORTANT: Use UUID type
    user_id: str
    game_name: str
    score: int
    duration_seconds: int
    difficulty_level: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)  # Pydantic v2 required

# Request model for updating a GameSession
class GameSessionUpdate(BaseModel):
    game_name: Optional[str] = None
    score: Optional[int] = None
    duration_seconds: Optional[int] = None
    difficulty_level: Optional[int] = None


class GameParametersResponse(BaseModel):
    difficulty_level: int
    pattern_size: int
    grid_size: int
    display_time_ms: int