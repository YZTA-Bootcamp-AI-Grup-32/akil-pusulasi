from pydantic import BaseModel
from typing import List

class GameScoreData(BaseModel):
    """
    Represents a single data point for the game stats graph.
    """
    score: int
    level: int

class UserStatsResponse(BaseModel):
    """
    A consolidated response model for key user statistics.
    """
    journal_streak: int
    last_five_scores: List[int]
    last_ten_game_stats: List[GameScoreData]