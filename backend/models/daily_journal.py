from sqlalchemy import Column, String, Text, ForeignKey, TIMESTAMP, text
from sqlalchemy.dialects.postgresql import UUID
import uuid
from database.connection import Base

class DailyJournal(Base):
    __tablename__ = "daily_journals"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(String(128), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    journal_content = Column(Text, nullable=False)
    ai_prompt = Column(Text, nullable=True)
    ai_response = Column(Text, nullable=True)
    created_at = Column(TIMESTAMP(timezone=True), server_default=text("NOW()"))