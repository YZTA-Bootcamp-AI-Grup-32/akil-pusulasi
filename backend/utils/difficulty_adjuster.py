from typing import List
from schemas.game_session import GameSessionResponse

DIFFICULTY_LEVELS = {
    1: {"pattern_size": 3, "grid_size": 9, "display_time_ms": 3000},
    2: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 3000},
    3: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 2500},
    4: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 3000},
    5: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 2500},
    6: {"pattern_size": 6, "grid_size": 16, "display_time_ms": 2800},
    7: {"pattern_size": 6, "grid_size": 25, "display_time_ms": 3500},
}
MAX_LEVEL = len(DIFFICULTY_LEVELS)

def calculate_next_difficulty_level(previous_sessions: List[GameSessionResponse]) -> int:
    """
    Calculates the best STARTING difficulty level for a new game session based on
    the user's historical performance.
    """
    if not previous_sessions:
        return 1

    last_session = previous_sessions[0]
    last_level_played = last_session.difficulty_level

    was_last_game_a_success = last_session.score > 0

    if was_last_game_a_success:
        return min(last_level_played + 1, MAX_LEVEL)
    else:
        return last_level_played