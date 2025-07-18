from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer
from firebase_admin import auth

# Bu, "Authorization: Bearer <token>" başlığını okumak için bir şemadır.
security = HTTPBearer()

async def get_current_user(creds = Depends(security)) -> dict:
    """
    A reusable dependency to verify Firebase ID token and return user data (decoded token).
    It ensures that every protected endpoint gets a valid, authenticated user.
    """
    if creds is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Bearer token not found",
        )
    try:
        # Gelen token'ı doğrula
        decoded_token = auth.verify_id_token(creds.credentials)
        return decoded_token
    except auth.InvalidIdTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Firebase ID token",
        )
    except auth.ExpiredIdTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Expired Firebase ID token. Please log in again.",
        )
    except Exception as e:
        # Genel bir hata durumunda loglama yapmak iyi bir pratiktir.
        # import logging
        # logging.error(f"Token verification failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Could not validate credentials.",
        )