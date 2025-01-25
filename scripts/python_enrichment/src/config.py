from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    GEMINI_API_KEY: str = "AIzaSyC6HHYMQ16-fgzys6SCR1a516SP8WZ65c4"
    GOOGLE_SHEETS_API_KEY: str = "AIzaSyCtS59szVnUfBtRCzN4loK_JfPtpP5t5sA"
    DEEPSEEK_API_KEY: str = ""  # Your DeepSeek API key

    class Config:
        env_file = ".env"

settings = Settings()

