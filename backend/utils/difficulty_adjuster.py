from typing import List
from schemas.game_session import GameSessionResponse # Your existing schema

# This definition remains the same
DIFFICULTY_LEVELS = {
    1: {"pattern_size": 3, "grid_size": 9, "display_time_ms": 3000},
    2: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 3000},
    3: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 2500},
    4: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 3000},
    5: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 2500},
}
MAX_LEVEL = len(DIFFICULTY_LEVELS)

def calculate_next_difficulty_level(previous_sessions: List[GameSessionResponse]) -> int:
    """
    Calculates the next difficulty level based on heuristic rules
    using the existing 'score' and 'duration_seconds' fields.
    """
    if not previous_sessions:
        return 1

    last_level = previous_sessions[0].difficulty_level

    recent_sessions = previous_sessions[:3]
    
    success_count = 0
    for session in recent_sessions:
        if session.score >= 9 and session.duration_seconds <= 20:
            success_count += 1
            
    struggle_count = 0
    for session in recent_sessions:
        if session.score <= 5 or session.duration_seconds > 45:
            struggle_count += 1

    if success_count >= 2:
        next_level = min(last_level + 1, MAX_LEVEL)
    elif struggle_count >= 1:
        next_level = max(last_level - 1, 1)
    else:
        next_level = last_level
        
    return next_level