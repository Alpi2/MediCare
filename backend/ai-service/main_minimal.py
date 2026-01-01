"""
Minimal Hospital AI Service - FastAPI Application
Basic version for testing startup
"""

import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# Setup basic logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI application
app = FastAPI(
    title="Hospital AI Service",
    description="AI/ML microservice for hospital management system",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Root endpoint with service information"""
    return {
        "service": "Hospital AI Service",
        "version": "1.0.0",
        "status": "healthy",
        "environment": "development",
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
    return {
        "status": "healthy",
        "timestamp": "2023-12-01T00:00:00Z",
        "services": {
            "redis": "not_connected",
            "models": "not_loaded",
            "kafka": "not_connected"
        },
        "models_loaded": []
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main_minimal:app",
        host="0.0.0.0",
        port=8001,
        reload=True,
        log_level="info"
    )