from typing import List
from schemas.game_session import GameSessionResponse
import math

DIFFICULTY_LEVELS = {
    1: {"pattern_size": 3, "grid_size": 9, "display_time_ms": 3000},
    2: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 3000},
    3: {"pattern_size": 4, "grid_size": 9, "display_time_ms": 2500},
    4: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 3000},
    5: {"pattern_size": 5, "grid_size": 16, "display_time_ms": 2500},
    6: {"pattern_size": 6, "grid_size": 16, "display_time_ms": 2800},
    7: {"pattern_size": 7, "grid_size": 25, "display_time_ms": 3500},
    8: {"pattern_size": 7, "grid_size": 25, "display_time_ms": 3000},
    9: {"pattern_size": 8, "grid_size": 25, "display_time_ms": 3500},
}
MAX_LEVEL = len(DIFFICULTY_LEVELS)
MIN_LEVEL = 1

GAMES_TO_CONSIDER_FOR_PROMOTION = 3

PROMOTION_THRESHOLD_MULTIPLIER = 1.05


def _calculate_performance_score(session: GameSessionResponse) -> float:
    """
    Calculates a performance score for a single SUCCESSFUL game session.
    A higher score is better.
    The score is based on the level completed and how quickly it was done.
    """
    if session.score == 0:
        return 0.0

    level = session.difficulty_level
    level_params = DIFFICULTY_LEVELS.get(level)

    if not level_params:
        return float(level)

    target_time_ms = level_params["display_time_ms"]
    
    reaction_time_seconds = session.duration_seconds

    time_factor = target_time_ms / (reaction_time_seconds * 1000)

    time_bonus = math.log10(time_factor + 0.1) * 0.5 

    performance = level + time_bonus
    
    return max(level - 0.5, performance)


def calculate_next_difficulty_level(previous_sessions: List[GameSessionResponse]) -> int:
    """
    Calculates the best STARTING difficulty level for a new game session
    based on a more robust analysis of user's historical performance.
    """
    if not previous_sessions:
        return MIN_LEVEL

    last_session = previous_sessions[0]
    last_level_played = last_session.difficulty_level
    was_last_game_a_success = last_session.score > 0

    if not was_last_game_a_success:
        return max(MIN_LEVEL, last_level_played - 1)

    relevant_sessions = [
        s for s in previous_sessions
        if s.difficulty_level == last_level_played and s.score > 0
    ][:GAMES_TO_CONSIDER_FOR_PROMOTION]

    if len(relevant_sessions) < GAMES_TO_CONSIDER_FOR_PROMOTION:
        return last_level_played

    performance_scores = [_calculate_performance_score(s) for s in relevant_sessions]
    average_performance = sum(performance_scores) / len(performance_scores)

    promotion_threshold = last_level_played * PROMOTION_THRESHOLD_MULTIPLIER

    print(f"User at level {last_level_played}. Avg Performance: {average_performance:.2f}. Threshold: {promotion_threshold:.2f}")

    if average_performance >= promotion_threshold:
        print(f"PROMOTING user to level {last_level_played + 1}")
        return min(MAX_LEVEL, last_level_played + 1)
    else:
        print(f"KEEPING user at level {last_level_played}")
        return last_level_played