"""Logging configuration for Hospital AI Service.

Sets up console (rich) and rotating file handlers and configures
third-party loggers to reduce noise.
"""

import logging
import sys
from pathlib import Path
from logging.handlers import RotatingFileHandler

try:
	from rich.logging import RichHandler
	RICH_AVAILABLE = True
except Exception:
	RICH_AVAILABLE = False

from app.core.config import settings


def setup_logging():
	logs_dir = Path("logs")
	logs_dir.mkdir(exist_ok=True)

	root = logging.getLogger()
	root.setLevel(getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))

	formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")

	# Console handler
	if RICH_AVAILABLE:
		console_handler = RichHandler(rich_tracebacks=True)
		console_handler.setLevel(getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))
		console_handler.setFormatter(formatter)
		root.addHandler(console_handler)
	else:
		ch = logging.StreamHandler(sys.stdout)
		ch.setLevel(getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))
		ch.setFormatter(formatter)
		root.addHandler(ch)

	# Rotating file handler
	file_handler = RotatingFileHandler(logs_dir / "ai-service.log", maxBytes=10 * 1024 * 1024, backupCount=5)
	file_handler.setLevel(getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))
	file_handler.setFormatter(formatter)
	root.addHandler(file_handler)

	# Reduce verbosity from third-party libs
	logging.getLogger("uvicorn").setLevel(logging.WARNING)
	logging.getLogger("sqlalchemy").setLevel(logging.WARNING)
	return root


def log_exception(exc: Exception, context: dict | None = None):
	"""Log an exception with stack trace and optional context using the configured root logger.

	The function intentionally logs full exception details for observability (stack trace,
	exception type) while callers should return sanitized messages to API clients.
	"""
	logger = logging.getLogger("ai-service")
	if context is None:
		context = {}
	try:
		# Use logger.exception to include the stack trace
		logger.exception("Exception occurred: %s | context: %s", exc, context)
	except Exception:
		# In the unlikely case logging itself fails, fallback to a simple stderr write
		print("Failed to log exception", exc, context, file=sys.stderr)
