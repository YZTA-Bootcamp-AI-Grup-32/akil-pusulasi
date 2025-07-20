from pydantic import BaseModel, ConfigDict, Field
from typing import List,Any
from datetime import datetime

class UserBase(BaseModel):
    """Base schema for user data"""
    full_name:str = Field(..., example="Jhon Doe")
    birth_year: int = Field(..., example=1990)
    interests: List[str]= Field(..., example=["Reading","Hiking", "Technology"])
    
class UserCreate(UserBase):
    """Schema for creating a new user profile"""
    pass

class UserResponse(UserBase):
    """Schema for responding with user profile data."""
    id:str #This will be the firebase UID
    created_at: datetime
    
    model_config = ConfigDict(from_attributes=True)