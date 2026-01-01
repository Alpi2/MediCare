"""Configuration settings for Hospital AI Service

This module uses Pydantic v2 with the separate pydantic-settings package
for BaseSettings compatibility.
"""

import os
from typing import List, Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import field_validator


class Settings(BaseSettings):
    """Application settings"""

    # Application
    APP_NAME: str = "Hospital AI Service"
    VERSION: str = "1.0.0"
    ENVIRONMENT: str = "development"
    DEBUG: bool = False
    HOST: str = "0.0.0.0"
    PORT: int = 9090
    WORKERS: int = 4

    # Security
    # SECRET_KEY must be provided via environment (do NOT hardcode in source)
    SECRET_KEY: str
    ALLOWED_HOSTS: List[str] = ["*"]

    # Database (must be provided via environment in production)
    DATABASE_URL: str

    # Redis
    REDIS_URL: str = "redis://redis:6379/1"

    # Kafka
    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9093"
    KAFKA_GROUP_ID: str = "ai-service-group"

    KAFKA_TOPICS_APPOINTMENT_EVENTS: str = "appointment-events"
    KAFKA_TOPICS_PATIENT_EVENTS: str = "patient-events"
    KAFKA_TOPICS_AI_PREDICTIONS: str = "ai-predictions"
    KAFKA_TOPICS_VITAL_SIGNS: str = "vital-signs-stream"

    # ML Models
    MODEL_STORAGE_PATH: str = "./models"
    MODEL_CACHE_TTL: int = 3600
    MODEL_AUTO_RELOAD: bool = False
    MLFLOW_TRACKING_URI: str = "http://mlflow:5000"

    # External APIs
    OPENAI_API_KEY: Optional[str] = None
    HUGGINGFACE_API_KEY: Optional[str] = None

    # Monitoring
    PROMETHEUS_METRICS_PORT: int = 9090
    LOG_LEVEL: str = "INFO"

    # AI/ML Configuration
    MAX_BATCH_SIZE: int = 32
    MODEL_CACHE_SIZE: int = 10
    PREDICTION_TIMEOUT: int = 30

    # Federated Learning
    FL_SERVER_ADDRESS: str = "fl-server:8080"
    FL_CLIENT_ID: str = "hospital-ai-client"

    # Medical AI Settings
    RISK_PREDICTION_THRESHOLD: float = 0.7
    NO_SHOW_PREDICTION_THRESHOLD: float = 0.5
    VITAL_SIGNS_ALERT_THRESHOLD: float = 0.8

    @field_validator("ENVIRONMENT")
    @classmethod
    def validate_environment(cls, v):
        if v not in ["development", "staging", "production"]:
            raise ValueError("Environment must be development, staging, or production")
        return v

    @field_validator("LOG_LEVEL")
    @classmethod
    def validate_log_level(cls, v):
        if v not in ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"]:
            raise ValueError("Invalid log level")
        return v

    @field_validator("SECRET_KEY")
    @classmethod
    def validate_secret_key(cls, v):
        if not isinstance(v, str) or len(v) < 32:
            raise ValueError("SECRET_KEY must be at least 32 characters")
        return v

    @field_validator("DATABASE_URL")
    @classmethod
    def validate_database_url(cls, v):
        if not isinstance(v, str) or not v.startswith(("postgresql://", "postgresql+asyncpg://")):
            raise ValueError("DATABASE_URL must be a valid PostgreSQL connection string")
        return v

    @field_validator("ALLOWED_HOSTS")
    @classmethod
    def validate_allowed_hosts(cls, v, info):
        """
        Validate ALLOWED_HOSTS to avoid insecure wildcard configuration in non-development environments.

        - In `development` a wildcard `['*']` is acceptable for convenience.
        - In `staging` or `production` a wildcard is disallowed: raise ValueError to fail fast.
        """
        # v is a list of hosts provided via env or default
        env = os.environ.get("ENVIRONMENT", os.environ.get("APP_ENV", "development"))
        # Normalize env if pydantic has already parsed ENVIRONMENT, use info.data if available
        try:
            configured_env = info.data.get("ENVIRONMENT") if info and info.data else env
        except Exception:
            configured_env = env

        # If configured_env is staging/production and ALLOWED_HOSTS contains a wildcard, fail fast
        if isinstance(configured_env, str) and configured_env in ("staging", "production"):
            if isinstance(v, (list, tuple)) and any(h == "*" for h in v):
                raise ValueError(
                    "ALLOWED_HOSTS must not contain '*' in staging or production. "
                    "Set explicit hostnames or domains via ALLOWED_HOSTS environment variable."
                )
        return v

    # Pydantic v2 settings style: define model_config using SettingsConfigDict
    # - env_file_encoding ensures utf-8 .env parsing
    # - extra='ignore' allows additional env vars (e.g., injected secrets) without error
    model_config: SettingsConfigDict = SettingsConfigDict(
        env_file='.env', env_file_encoding='utf-8', case_sensitive=True, extra='ignore'
    )


settings = Settings()