from fastapi import FastAPI
from core.firebase_config import initialize_firebase
from routers import game, users

initialize_firebase()

app = FastAPI(
    title="Akıl Pusulası Backend",
    description="Bilişsel Sağlık Asistanı için FastAPI altyapısı",
    version="0.1.0"
)

@app.get("/")
async def root():
    return {"message": "Akıl Pusulası Backend is running."}

# Router'ları uygulamaya dahil et
app.include_router(game.router, prefix="/api/v1")

app.include_router(
    users.router,
    prefix="/api/v1/users"
)
