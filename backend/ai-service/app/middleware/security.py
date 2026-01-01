"""Basic security middleware for request hardening and throttling."""

import time
import logging
from typing import Dict
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response, JSONResponse

logger = logging.getLogger(__name__)

# Very small in-memory rate limiter for dev/testing. For production use Redis.
_RATE_LIMIT_STORE: Dict[str, Dict[str, any]] = {}


class SecurityMiddleware(BaseHTTPMiddleware):
	def __init__(self, app, max_requests_per_minute: int = 100):
		super().__init__(app)
		self.max_requests = max_requests_per_minute

	async def dispatch(self, request: Request, call_next):
		client_ip = request.client.host if request.client else "unknown"

		# Rate limiting
		now = int(time.time())
		bucket = _RATE_LIMIT_STORE.setdefault(client_ip, {"ts": now, "count": 0})
		if now - bucket["ts"] >= 60:
			bucket["ts"] = now
			bucket["count"] = 0
		bucket["count"] += 1
		if bucket["count"] > self.max_requests:
			logger.warning("Rate limit exceeded for %s", client_ip)
			return JSONResponse(status_code=429, content={"error": "Too many requests"})

		# Size limit (reject > 10MB bodies)
		content_length = request.headers.get("content-length")
		if content_length is not None:
			try:
				if int(content_length) > 10 * 1024 * 1024:
					return JSONResponse(status_code=413, content={"error": "Payload too large"})
			except Exception:
				pass

		# Security headers will be added to the response
		response = await call_next(request)
		response.headers["X-Content-Type-Options"] = "nosniff"
		response.headers["X-Frame-Options"] = "DENY"
		response.headers["X-XSS-Protection"] = "1; mode=block"
		response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
		return response

