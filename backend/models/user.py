from sqlalchemy import Column, String, Integer, JSON, TIMESTAMP
from sqlalchemy.sql import func
from database.connection import Base

# User table model
class User(Base):
    __tablename__ = "users"

    id = Column(String(128), primary_key=True, index=True)
    full_name = Column(String(100), nullable=False)
    birth_year = Column(Integer)
    interests = Column(JSON)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())