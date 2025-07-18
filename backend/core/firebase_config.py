import firebase_admin
from firebase_admin import credentials
import os
from dotenv import load_dotenv

def initialize_firebase():
    """
    Initializes the Firebase Admin SDK using credentials from environment variables.
    Should be called once when the application starts.
    """
    # .env dosyasını yükle
    load_dotenv()
    
    service_account_key_path = os.getenv("FIREBASE_SERVICE_ACCOUNT_KEY_PATH")

    if not service_account_key_path:
        raise ValueError("CRITICAL: FIREBASE_SERVICE_ACCOUNT_KEY_PATH environment variable not set.")
    
    # Firebase'in zaten başlatılıp başlatılmadığını kontrol et
    if not firebase_admin._apps:
        cred = credentials.Certificate(service_account_key_path)
        firebase_admin.initialize_app(cred)
        print("Firebase Admin SDK Initialized Successfully.")
    else:
        print("Firebase Admin SDK already initialized.")