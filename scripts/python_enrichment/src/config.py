from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    GEMINI_API_KEY: str = ""
    GOOGLE_SHEETS_API_KEY: str = ""
    DEEPSEEK_API_KEY: str = ""  # Your DeepSeek API key

    class Config:
        env_file = ".env"

settings = Settings()

