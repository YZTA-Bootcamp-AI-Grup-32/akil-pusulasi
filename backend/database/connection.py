from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv
import os

# .env dosyasını yükle
load_dotenv()

# .env'den bağlantı URL'sini çek
DATABASE_URL = os.getenv("DATABASE_URL")

# Engine oluştur (veritabanına bağlanmak için)
engine = create_async_engine(DATABASE_URL, echo=True)

# Session factory (her istekte yeni bağlantı açmak için)
AsyncSessionLocal = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False
)

# SQLAlchemy Base sınıfı
Base = declarative_base()

# Dependency olarak kullanılacak fonksiyon
async def get_db():
    async with AsyncSessionLocal() as session:
        yield session