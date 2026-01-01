"""
Hospital AI Service - FastAPI Application
Main entry point for AI/ML microservice

Features:
- Patient risk prediction
- No-show prediction
- Medical imaging analysis
- Clinical decision support
- Real-time vital signs monitoring
- Federated learning coordination
"""

import logging
import sys
import os
from contextlib import asynccontextmanager
from typing import Dict, Any

import uvicorn
from fastapi import FastAPI, Request, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
from fastapi.responses import JSONResponse
from prometheus_client import make_asgi_app
import redis.asyncio as redis

from app.core.config import settings
from app.core.logging import setup_logging
from app.core.database import init_db
from app.api.v1.api import api_router
from app.core.ml_models import ModelManager
from app.core.kafka_client import KafkaClient
from app.middleware.security import SecurityMiddleware
from app.middleware.monitoring import MonitoringMiddleware
# Import metric objects to initialize model status on startup
from app.middleware.monitoring import (
    ml_model_loaded,
    ml_predictions_total,
    ml_prediction_latency,
)

# Setup logging
setup_logging()
logger = logging.getLogger(__name__)

# Datetime utilities for timestamps in responses
from datetime import datetime, timezone

# Global instances
model_manager: ModelManager = None
kafka_client: KafkaClient = None
redis_client: redis.Redis = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan management"""
    global model_manager, kafka_client, redis_client
    
    logger.info("🚀 Starting Hospital AI Service...")
    
    try:
        # Initialize database
        await init_db()
        logger.info("✅ Database initialized")

        # Initialize Redis
        redis_client = redis.from_url(
            settings.REDIS_URL,
            encoding="utf-8",
            decode_responses=True,
        )
        await redis_client.ping()
        logger.info("✅ Redis connected")

        # Initialize ML Model Manager (optional)
        skip_models = os.getenv("SKIP_MODEL_LOADING", "false").lower() in ("1", "true", "yes")
        try:
            if skip_models:
                logger.info("⏭️ SKIP_MODEL_LOADING is set — skipping model initialization")
                model_manager = None
            else:
                model_manager = ModelManager()
                await model_manager.initialize()
                logger.info("✅ ML Models loaded")
                # Initialize model-loaded gauge for any already-loaded models so Prometheus
                # /metrics reflects the current model inventory on startup.
                try:
                    loaded_models = await model_manager.get_loaded_models()
                    for model_name in loaded_models:
                        ml_model_loaded.labels(model_name=model_name).set(1)
                    logger.debug(f"Initialized ml_model_loaded for models: {loaded_models}")
                except Exception as _:
                    logger.debug("No models available to initialize ml_model_loaded gauge")
        except Exception as e:
            logger.warning(f"⚠️ ML Models failed to load: {e}")
            logger.warning("Service will start without ML models")
            model_manager = None

        # Initialize Kafka client
        try:
            kafka_client = KafkaClient()
            await kafka_client.start()
            logger.info("✅ Kafka client started")
        except Exception as e:
            logger.warning(f"⚠️ Kafka client failed to start: {e}")
            kafka_client = None

        # Store in app state
        app.state.model_manager = model_manager
        app.state.kafka_client = kafka_client
        app.state.redis_client = redis_client

        logger.info("🎉 Hospital AI Service started successfully!")

        yield

    except Exception as e:
        logger.error(f"❌ Failed to start AI Service: {e}")
        sys.exit(1)

    finally:
        # Cleanup
        logger.info("🛑 Shutting down Hospital AI Service...")

        if kafka_client:
            try:
                await kafka_client.stop()
                logger.info("✅ Kafka client stopped")
            except Exception:
                logger.warning("Failed to stop Kafka client cleanly")

        if redis_client:
            try:
                await redis_client.close()
                logger.info("✅ Redis connection closed")
            except Exception:
                logger.warning("Failed to close Redis client cleanly")

        if model_manager:
            try:
                await model_manager.cleanup()
                logger.info("✅ ML Models cleaned up")
            except Exception:
                logger.warning("Failed to cleanup model manager cleanly")

        logger.info("👋 Hospital AI Service shutdown complete")


# API tags metadata
tags_metadata = [
    {"name": "Predictions", "description": "ML model prediction endpoints for no-show and risk scoring"},
    {"name": "Models", "description": "Model management, metadata, and versioning"},
    {"name": "Health", "description": "Service health checks and readiness probes"},
]

# Create FastAPI application with rich OpenAPI metadata
app = FastAPI(
    title="Hospital AI Service API",
    description=(
        "AI/ML prediction endpoints for healthcare analytics including no-show prediction (logistic regression "
        "model with 85%+ accuracy), patient risk scoring (composite risk assessment using vital signs and "
        "medical history), and clinical decision support. Models are trained on anonymized hospital data and "
        "updated monthly."
    ),
    version="1.0.0",
    contact={
        "name": "AI Team",
        "email": os.getenv('CONTACT_EMAIL', 'ai-team@hospital.com'),
        "url": "https://hospital.com/ai-docs",
    },
    license_info={"name": "Proprietary", "url": "https://hospital.com/license"},
    servers=[
        {"url": "http://localhost:9090", "description": "Development"},
        {"url": os.getenv('STAGING_URL', 'https://staging-ai.hospital.com'), "description": "Staging"},
        {"url": os.getenv('PRODUCTION_URL', 'https://ai.hospital.com'), "description": "Production"},
    ],
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    tags_metadata=tags_metadata,
    lifespan=lifespan,
)

# Security middleware
app.add_middleware(SecurityMiddleware)

# Monitoring middleware
app.add_middleware(MonitoringMiddleware)

# CORS middleware
# Configure CORS origins and credentials carefully. If ALLOWED_HOSTS contains a
# wildcard ("*") we MUST NOT allow credentials per browser security rules.
origins = list(settings.ALLOWED_HOSTS or [])
allow_credentials = True
if len(origins) == 1 and origins[0] == "*":
    allow_credentials = False
else:
    # Normalize entries that look like hostnames (add http:// by default).
    normalized = []
    for o in origins:
        if o == "*":
            normalized.append(o)
            continue
        if o.startswith("http://") or o.startswith("https://"):
            normalized.append(o)
        else:
            # Assume http for plain host:port entries used in development
            normalized.append(f"http://{o}")
    origins = normalized

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=allow_credentials,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

# Trusted host middleware
app.add_middleware(
    TrustedHostMiddleware,
    allowed_hosts=settings.ALLOWED_HOSTS
)

# Include API routes
app.include_router(api_router, prefix="/api/v1")

# Prometheus metrics endpoint
metrics_app = make_asgi_app()
app.mount("/metrics", metrics_app)


@app.get("/")
async def root():
    """Root endpoint with service information"""
    return {
        "service": "Hospital AI Service",
        "version": "1.0.0",
        "status": "healthy",
        "environment": settings.ENVIRONMENT,
        "features": [
            "Patient Risk Prediction",
            "No-Show Prediction", 
            "Medical Imaging Analysis",
            "Clinical Decision Support",
            "Vital Signs Monitoring",
            "Federated Learning"
        ]
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    try:
        # Check Redis connection
        await app.state.redis_client.ping()

        # Check model availability (optional)
        model_manager = getattr(app.state, "model_manager", None)
        if model_manager is None:
            model_status = False
            models_loaded = []
        else:
            model_status = await model_manager.health_check()
            models_loaded = await model_manager.get_loaded_models()

        # Check Kafka connection (optional)
        kafka_client = getattr(app.state, "kafka_client", None)
        kafka_status = kafka_client.is_connected() if kafka_client is not None else False

        return {
            "status": "healthy",
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "services": {
                "redis": "healthy",
                "models": "healthy" if model_status else "unhealthy",
                "kafka": "healthy" if kafka_status else "unhealthy",
            },
            "models_loaded": models_loaded,
        }
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(status_code=503, detail="Service unhealthy")


@app.get("/ready")
async def readiness_check():
    """Readiness check for Kubernetes"""
    try:
        # Verify critical components are available; allow models to be optional
        if not hasattr(app.state, 'redis_client'):
            raise HTTPException(status_code=503, detail="Redis client not initialized")

        # Kafka and models are optional; warn but do not block readiness
        if not hasattr(app.state, 'kafka_client') or getattr(app.state, 'kafka_client') is None:
            logger.warning("Kafka client not initialized — continuing (optional)")

        return {"status": "ready"}
    except Exception as e:
        logger.error(f"Readiness check failed: {e}")
        raise HTTPException(status_code=503, detail="Service not ready")


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """Global HTTP exception handler"""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": {
                "code": exc.status_code,
                "message": exc.detail,
                "path": str(request.url.path),
                "timestamp": datetime.now(timezone.utc).isoformat()
            }
        }
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """Global exception handler"""
    logger.error(f"Unhandled exception: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "error": {
                "code": 500,
                "message": "Internal server error",
                "path": str(request.url.path),
                "timestamp": datetime.now(timezone.utc).isoformat()
            }
        }
    )


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.ENVIRONMENT == "development",
        log_config=None,  # Use our custom logging
        access_log=False,  # Handled by middleware
        workers=1 if settings.ENVIRONMENT == "development" else settings.WORKERS
    )