from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv
import os

# Load the .env file
load_dotenv()

# Retrieve the connection URL from .env
DATABASE_URL = os.getenv("DATABASE_URL")

# Engine oluştur (veritabanına bağlanmak için)
engine = create_async_engine(DATABASE_URL, echo=True)

# Session factory (to open a new connection for each request)
AsyncSessionLocal = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False
)

# SQLAlchemy Base class
Base = declarative_base()

# Function to be used as a dependency
async def get_db():
    async with AsyncSessionLocal() as session:
        yield session