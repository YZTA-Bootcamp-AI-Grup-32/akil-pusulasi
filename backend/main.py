from fastapi import FastAPI, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from Database.connection import get_db
from models import user, game_session, daily_journal

app = FastAPI(
    title="Akıl Pusulası Backend",
    description="Bilişsel Sağlık Asistanı için FastAPI altyapısı",
    version="0.1.0"
)

@app.get("/")
async def root():
    return {"message": "Akıl Pusulası Backend is running."}

from routers import game
app.include_router(game.router)
