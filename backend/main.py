from fastapi import FastAPI

app = FastAPI(
    title="Akıl Pusulası Backend",
    description="Bilişsel Sağlık Asistanı için FastAPI altyapısı",
    version="0.1.0"
)

@app.get("/")
async def root():
    return {"message": "Akıl Pusulası Backend is running."}
