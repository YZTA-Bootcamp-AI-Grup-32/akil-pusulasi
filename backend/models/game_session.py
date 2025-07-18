from sqlalchemy import Column, String, Integer, ForeignKey, TIMESTAMP, text
from sqlalchemy.dialects.postgresql import UUID
import uuid
from database.connection import Base 

class GameSession(Base):
    __tablename__ = "game_sessions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(String(128), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    game_name = Column(String(50), nullable=False, default="Hafiza Kartlari")
    score = Column(Integer, nullable=False)
    duration_seconds = Column(Integer, nullable=False)
    difficulty_level = Column(Integer, nullable=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=text("NOW()"))
