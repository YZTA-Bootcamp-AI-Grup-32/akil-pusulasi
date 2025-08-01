from pydantic import BaseModel
from typing import List

class UserStatsResponse(BaseModel):
    """
    A consolidated response model for key user statistics.
    """
    journal_streak: int
    last_five_scores: List[int]