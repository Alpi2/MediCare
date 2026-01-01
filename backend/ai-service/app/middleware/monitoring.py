"""Monitoring middleware exposing Prometheus metrics and timing requests."""

import logging
import time
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from prometheus_client import Counter, Histogram, Gauge, Summary

logger = logging.getLogger(__name__)

# Metrics
HTTP_REQUESTS_TOTAL = Counter("http_requests_total", "Total HTTP requests", ["method", "endpoint", "status"])
HTTP_REQUEST_DURATION = Histogram("http_request_duration_seconds", "HTTP request duration seconds", ["method", "endpoint"])
HTTP_IN_PROGRESS = Gauge("http_requests_in_progress", "HTTP requests in progress")

# ML metrics
# Total predictions by model and outcome (e.g. success, error)
ml_predictions_total = Counter(
	"ml_predictions_total",
	"Total ML predictions",
	["model_name", "prediction_outcome"],
)

# Prediction latency histogram (seconds) with explicit buckets for SLA tracking
ml_prediction_latency = Histogram(
	"ml_prediction_latency_seconds",
	"ML prediction latency seconds",
	["model_name"],
	buckets=(0.1, 0.5, 1.0, 2.0, 5.0, 10.0, 30.0),
)

# Model loaded status (1 = loaded, 0 = not loaded)
ml_model_loaded = Gauge(
	"ml_model_loaded",
	"Model loaded status (1=loaded, 0=not_loaded)",
	["model_name"],
)

# Prediction confidence summary (observe probability/confidence values)
ml_prediction_confidence = Summary(
	"ml_prediction_confidence",
	"Prediction confidence scores",
	["model_name"],
)

# Batch prediction sizes
ml_batch_size = Histogram(
	"ml_batch_size",
	"Batch prediction sizes",
	buckets=(1, 5, 10, 20, 50, 100),
)

# Kafka metrics
kafka_events_consumed = Counter(
	"kafka_events_consumed",
	"Kafka events consumed",
	["topic", "event_type"],
)

kafka_events_produced = Counter(
	"kafka_events_produced",
	"Kafka events produced",
	["topic"],
)

# Export metrics for use in API endpoints and other modules
__all__ = [
	"ml_predictions_total",
	"ml_prediction_latency",
	"ml_model_loaded",
	"ml_prediction_confidence",
	"ml_batch_size",
	"kafka_events_consumed",
	"kafka_events_produced",
	# keep existing HTTP metrics available too
	"HTTP_REQUESTS_TOTAL",
	"HTTP_REQUEST_DURATION",
	"HTTP_IN_PROGRESS",
]


class MonitoringMiddleware(BaseHTTPMiddleware):
	async def dispatch(self, request: Request, call_next):
		# Ensure response is defined even if call_next raises
		response = None
		HTTP_IN_PROGRESS.inc()
		start = time.time()
		try:
			response = await call_next(request)
		except Exception:
			# Let the exception propagate after ensuring metrics are recorded in finally
			raise
		finally:
			duration = time.time() - start
			# Record metrics; be defensive if response is None due to an exception
			try:
				HTTP_REQUEST_DURATION.labels(method=request.method, endpoint=request.url.path).observe(duration)
				status = response.status_code if response is not None else 500
				HTTP_REQUESTS_TOTAL.labels(method=request.method, endpoint=request.url.path, status=status).inc()
			except Exception:
				# Don't let metrics failures break request handling
				pass
			HTTP_IN_PROGRESS.dec()
			# add response time header if we have a response object
			if response is not None:
				try:
					response.headers["X-Response-Time"] = f"{int(duration*1000)}ms"
				except Exception:
					pass
		# Log slow requests regardless of response availability
		if duration > 1.0:
			logger.warning("Slow request %s %s: %.3fs", request.method, request.url.path, duration)
		return response

