"""Async database initialization helpers for the AI service."""

import logging
from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from sqlalchemy import text

from app.core.config import settings

logger = logging.getLogger(__name__)

engine = None
async_session_maker = None


async def init_db() -> bool:
	global engine, async_session_maker
	if engine is not None:
		return True

	engine = create_async_engine(settings.DATABASE_URL, echo=False, pool_pre_ping=True)
	async_session_maker = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

	# quick connectivity test
	try:
		async with engine.begin() as conn:
			await conn.execute(text("SELECT 1"))
		logger.info("Database connection test succeeded")
		return True
	except Exception as e:
		logger.error("Database initialization failed: %s", e)
		raise


async def get_db() -> AsyncGenerator[AsyncSession, None]:
	"""Dependency that yields an AsyncSession and ensures close."""
	global async_session_maker
	if async_session_maker is None:
		await init_db()
	async with async_session_maker() as session:
		try:
			yield session
		finally:
			await session.close()
