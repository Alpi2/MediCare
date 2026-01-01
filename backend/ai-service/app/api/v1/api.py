"""API router for AI model endpoints (v1).

This module defines strongly-typed Pydantic request/response models and
well-documented endpoints for prediction services (no-show prediction,
risk scoring and batch predictions). See `app.core.ml_models` for model
implementation details and expected behaviour.
"""

from fastapi import APIRouter, Request, HTTPException
from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field
import pandas as pd

api_router = APIRouter()

from app.core.logging import log_exception


def get_model_manager(request: Request):
	return getattr(request.app.state, "model_manager", None)


# ----------------------------- Pydantic Models -----------------------------


class AppointmentFeatures(BaseModel):
	"""Features extracted from an appointment record used by prediction models.

	Fields are intentionally generic to match the feature set expected by the
	underlying models (see `app.core.ml_models.ModelManager` documentation).
	"""

	patient_age: Optional[int] = Field(None, description="Patient age in years", example=54)
	previous_no_shows: Optional[int] = Field(0, description="Number of previous missed appointments", example=2)
	appointment_lead_time: Optional[int] = Field(None, description="Lead time in days between booking and appointment", example=3)
	day_of_week: Optional[str] = Field(None, description="Day of week of appointment (Mon, Tue, ...)", example="Mon")
	is_weekend: Optional[bool] = Field(None, description="Whether appointment is on a weekend", example=False)
	referral_source: Optional[str] = Field(None, description="Referral source code or description", example="GP")
	# Additional model-specific features may be present; kept as free-form
	extra: Optional[Dict[str, Any]] = Field(None, description="Additional model features")

	class Config:
		schema_extra = {
			"example": {
				"patient_age": 54,
				"previous_no_shows": 2,
				"appointment_lead_time": 3,
				"day_of_week": "Mon",
				"is_weekend": False,
				"referral_source": "GP",
				"extra": {"comorbidity_count": 1}
			}
		}


class NoShowPredictionRequest(BaseModel):
	"""Request payload for no-show prediction.

	The service expects either `appointment_id` (if available) or the
	explicit `appointment_data` features. `patient_id` helps enrich
	predictions with patient-level features when available.
	"""

	appointment_id: Optional[int] = Field(None, description="Optional appointment identifier", example=12345)
	patient_id: int = Field(..., description="Patient identifier", example=2001)
	appointment_data: AppointmentFeatures = Field(..., description="Feature set describing the appointment and patient context")

	class Config:
		schema_extra = {
			"example": {
				"appointment_id": 12345,
				"patient_id": 2001,
				"appointment_data": AppointmentFeatures.Config.schema_extra["example"]
			}
		}


class NoShowPredictionResponse(BaseModel):
	"""Prediction result for no-show model.

	- `prediction`: binary label (0 = will attend, 1 = will no-show)
	- `probability`: model probability estimate in [0.0, 1.0]
	- `risk_level`: human-friendly bucket (LOW, MEDIUM, HIGH)
	- `confidence`: model-reported confidence metric (if available)
	- `factors`: list of contributing factors or feature names influencing the prediction
	"""

	prediction: int = Field(..., ge=0, le=1, description="Binary prediction label: 0 attend, 1 no-show", example=1)
	probability: float = Field(..., ge=0.0, le=1.0, description="Probability of no-show", example=0.78)
	risk_level: str = Field(..., description="Risk bucket: LOW/MEDIUM/HIGH", example="HIGH")
	confidence: Optional[float] = Field(None, description="Optional confidence metric from model", example=0.85)
	factors: Optional[List[str]] = Field(None, description="Top contributing factors for interpretability", example=["High previous no-shows", "Short lead time"]) 

	class Config:
		schema_extra = {
			"example": {
				"prediction": 1,
				"probability": 0.78,
				"risk_level": "HIGH",
				"confidence": 0.85,
				"factors": ["High previous no-shows", "Short lead time"]
			}
		}


class RiskScoreRequest(BaseModel):
	"""Request payload for patient risk scoring.

	Provide `patient_id` and a `patient_data` dictionary containing vitals,
	history and other features required by the composite risk scorer.
	"""

	patient_id: int = Field(..., description="Patient identifier", example=2001)
	patient_data: Dict[str, Any] = Field(..., description="Patient-level data used to compute risk (vitals, labs, history)")

	class Config:
		schema_extra = {
			"example": {
				"patient_id": 2001,
				"patient_data": {"systolic_bp": 140, "diastolic_bp": 90, "bmi": 32.5, "diabetes": True}
			}
		}


class RiskScoreResponse(BaseModel):
	"""Response for composite patient risk scoring.

	- `risk_score`: numeric composite score (higher = more risk)
	- `risk_level`: categorical label
	- `confidence`: optional confidence metric
	- `factors`: list of contributing conditions/features
	"""

	risk_score: float = Field(..., description="Numeric risk score", example=7.3)
	risk_level: str = Field(..., description="Risk category (LOW/MEDIUM/HIGH)", example="MEDIUM")
	confidence: Optional[float] = Field(None, description="Optional model confidence", example=0.9)
	factors: Optional[List[str]] = Field(None, description="Top contributing factors", example=["High BMI", "Hypertension"]) 

	class Config:
		schema_extra = {
			"example": {"risk_score": 7.3, "risk_level": "MEDIUM", "confidence": 0.9, "factors": ["High BMI", "Hypertension"]}
		}


# ----------------------------- End Models ---------------------------------


@api_router.post(
	"/predict/no-show",
	summary="Predict patient no-show",
	description=(
		"Run the no-show prediction model for a single appointment. The model is a logistic-"
		"style classifier trained on historical appointment and patient features. Typical accuracy "
		"is reported at ~85% (see model metadata). Use this endpoint to triage outreach and "
		"reminder workflows."
	),
	response_model=NoShowPredictionResponse,
	responses={
		200: {"description": "Successful prediction"},
		400: {"description": "Invalid input"},
		404: {"description": "Model not found"},
		500: {"description": "Prediction error"},
		503: {"description": "Service unavailable"},
	},
	tags=["Predictions"],
)
async def predict_no_show(payload: NoShowPredictionRequest, request: Request) -> NoShowPredictionResponse:
	"""Predict whether a patient will miss (no-show) an appointment.

	The endpoint accepts a features object describing the appointment and patient context.
	If `appointment_id` is supplied the service may enrich features from historical records.
	The response contains a binary label, probability, a human-friendly risk bucket and
	optional interpretability factors.

	See `app.core.ml_models` for details on the underlying model and feature requirements.
	"""
	manager = get_model_manager(request)
	if manager is None:
		raise HTTPException(status_code=503, detail="Model manager not available")
	try:
		model = manager.get_model("no_show_predictor")
	except KeyError:
		raise HTTPException(status_code=404, detail="Model not found")

	data = payload.appointment_data.dict()
	# Optionally enrich with patient_id or appointment_id if model manager supports it
	if payload.patient_id:
		data.setdefault("patient_id", payload.patient_id)
	if payload.appointment_id:
		data.setdefault("appointment_id", payload.appointment_id)

	try:
		prob = (
			model.predict_proba_single(data)
			if hasattr(model, "predict_proba_single")
			else float(model.predict_proba(pd.DataFrame([data]))[0])
		)
		label = int(prob >= 0.5)
		risk = "HIGH" if prob >= 0.7 else ("MEDIUM" if prob >= 0.4 else "LOW")
		confidence = getattr(model, "last_confidence", None)
		factors = getattr(model, "explain_factors", lambda d: [])(data) if hasattr(model, "explain_factors") else None
		return NoShowPredictionResponse(prediction=label, probability=float(prob), risk_level=risk, confidence=confidence, factors=factors)
	except (ValueError, TypeError) as e:
		# Input/validation errors - log details, return a sanitized message
		log_exception(e, {"endpoint": "predict_no_show", "model": "no_show_predictor", "patient_id": getattr(payload, 'patient_id', None), "appointment_id": getattr(payload, 'appointment_id', None)})
		raise HTTPException(status_code=400, detail="Invalid input")
	except Exception as e:
		# Unexpected internal error - log full details but return a generic message
		log_exception(e, {"endpoint": "predict_no_show", "model": "no_show_predictor", "patient_id": getattr(payload, 'patient_id', None)})
		raise HTTPException(status_code=500, detail="Prediction failed")


@api_router.post(
	"/predict/risk-score",
	summary="Compute patient risk score",
	description=(
		"Compute a composite patient risk score using vitals, labs and historical data. The scorer "
		"aggregates multiple submodels and returns a normalized score and category useful for care "
		"prioritization and resource allocation."
	),
	response_model=RiskScoreResponse,
	responses={
		200: {"description": "Successful scoring"},
		400: {"description": "Invalid input"},
		404: {"description": "Model not found"},
		500: {"description": "Scoring error"},
		503: {"description": "Service unavailable"},
	},
	tags=["Predictions"],
)
async def predict_risk(payload: RiskScoreRequest, request: Request) -> RiskScoreResponse:
	"""Compute a composite risk score for a patient.

	Input should contain `patient_id` and a `patient_data` mapping with required clinical features.
	The response includes a numeric score, categorical risk level and optional contributing factors.
	"""
	manager = get_model_manager(request)
	if manager is None:
		raise HTTPException(status_code=503, detail="Model manager not available")
	try:
		model = manager.get_model("risk_scorer")
	except KeyError:
		raise HTTPException(status_code=404, detail="Model not found")

	try:
		df = pd.DataFrame([payload.patient_data])
		score = float(model.calculate_composite_risk(df).iloc[0])
		level = model.categorize_risk_levels([score])[0] if hasattr(model, "categorize_risk_levels") else "UNKNOWN"
		factors = getattr(model, "explain_factors", lambda d: [])(payload.patient_data) if hasattr(model, "explain_factors") else None
		confidence = getattr(model, "last_confidence", None)
		return RiskScoreResponse(risk_score=score, risk_level=level, confidence=confidence, factors=factors)
	except (ValueError, TypeError) as e:
		log_exception(e, {"endpoint": "predict_risk", "model": "risk_scorer", "patient_id": getattr(payload, 'patient_id', None)})
		raise HTTPException(status_code=400, detail="Invalid input")
	except Exception as e:
		log_exception(e, {"endpoint": "predict_risk", "model": "risk_scorer", "patient_id": getattr(payload, 'patient_id', None)})
		raise HTTPException(status_code=500, detail="Prediction failed")


class BatchPredictRequest(BaseModel):
	"""Request model for batch predictions.

	- `model_name`: name of the model registered in the ModelManager
	- `data`: list of feature dicts (one per record)
	"""

	model_name: str = Field(..., description="Model name to use for predictions", example="no_show_predictor")
	data: List[Dict[str, Any]] = Field(..., description="List of feature dictionaries for each record")


class BatchPredictResponse(BaseModel):
	predictions: List[int] = Field(..., description="Predicted labels for each input row")
	probabilities: Optional[List[float]] = Field(None, description="Optional probability estimates for each prediction")
	risk_levels: Optional[List[str]] = Field(None, description="Optional risk level per record")
	explanations: Optional[List[Any]] = Field(None, description="Optional explanations/interpretability artifacts per record")


@api_router.post(
	"/predict/batch",
	summary="Batch prediction for arbitrary model",
	description="Run batch predictions for a specified model. Payload must contain `model_name` and a typed `data` list.",
	response_model=BatchPredictResponse,
	responses={
		200: {"description": "Batch predictions returned"},
		400: {"description": "Invalid input or validation failed"},
		404: {"description": "Model not found"},
		500: {"description": "Prediction error"},
		503: {"description": "Service unavailable"},
	},
	tags=["Predictions"],
)
async def predict_batch(payload: BatchPredictRequest, request: Request) -> BatchPredictResponse:
	manager = get_model_manager(request)
	if manager is None:
		raise HTTPException(status_code=503, detail="Model manager not available")
	model_name = payload.model_name
	try:
		model = manager.get_model(model_name)
	except KeyError:
		raise HTTPException(status_code=404, detail="Model not found")

	try:
		df = pd.DataFrame(payload.data)
		# Prefer a model-specific batch method if implemented
		if hasattr(model, "predict_batch"):
			raw = model.predict_batch(df)
			# Normalize output into BatchPredictResponse fields
			preds = list(raw.get("predictions", []))
			probs = raw.get("probabilities")
			if probs is not None:
				probs = list(probs)
			risks = raw.get("risk_levels") or raw.get("risk_levels")
			explanations = raw.get("explanations") or raw.get("explanations")
			return BatchPredictResponse(predictions=preds, probabilities=probs, risk_levels=risks, explanations=explanations)

		# Fallback to predict / predict_proba
		preds = model.predict(df)
		probs = None
		if hasattr(model, "predict_proba"):
			probs_raw = model.predict_proba(df)
			try:
				probs = list(probs_raw)
			except Exception:
				probs = None
		# Ensure predictions are primitive lists
		preds = list(preds)
		return BatchPredictResponse(predictions=[int(p) for p in preds], probabilities=probs)
	except (ValueError, TypeError) as e:
		log_exception(e, {"endpoint": "predict_batch", "model": model_name})
		raise HTTPException(status_code=400, detail="Invalid input")
	except Exception as e:
		log_exception(e, {"endpoint": "predict_batch", "model": model_name})
		raise HTTPException(status_code=500, detail="Prediction failed")


@api_router.get("/models")
async def list_models(request: Request):
	manager = get_model_manager(request)
	if manager is None:
		return {"models": [], "metadata": {}}
	models = await manager.get_loaded_models()
	return {"models": models, "metadata": manager.model_metadata}


@api_router.get("/models/{model_name}/info")
async def model_info(model_name: str, request: Request):
	manager = get_model_manager(request)
	if manager is None:
		raise HTTPException(status_code=404, detail="Model manager not available")
	try:
		meta = manager.model_metadata.get(model_name, {})
		return {"model": model_name, "metadata": meta}
	except Exception as e:
		log_exception(e, {"endpoint": "model_info", "model": model_name})
		raise HTTPException(status_code=500, detail="Failed to retrieve model info")

