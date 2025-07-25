from pydantic import BaseModel, ConfigDict, Field
from typing import Optional
from datetime import datetime
from uuid import UUID

class DailyJournalCreate(BaseModel):
    journal_content: str = Field(..., min_length=10, description="The user's journal entry for the day.")

class DailyJournalResponse(BaseModel):
    id: UUID
    user_id: str
    journal_content: str
    ai_prompt: Optional[str] = None 
    ai_response: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)