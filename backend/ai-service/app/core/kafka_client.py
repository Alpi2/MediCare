import asyncio
import json
import logging
from typing import Dict, Any, Callable, Optional
from aiokafka import AIOKafkaProducer, AIOKafkaConsumer
from aiokafka.errors import KafkaError

logger = logging.getLogger(__name__)

class KafkaClient:
	"""Async Kafka client for AI service"""
    
	def __init__(self, bootstrap_servers: str = "hospital-kafka:9092", group_id: str = "ai-service-group"):
		self.bootstrap_servers = bootstrap_servers
		self.group_id = group_id
		self.producer: Optional[AIOKafkaProducer] = None
		self.consumers: Dict[str, AIOKafkaConsumer] = {}
		self._running = False
    
	async def start(self):
		"""Initialize Kafka producer and consumers"""
		try:
			# Initialize producer
			self.producer = AIOKafkaProducer(
				bootstrap_servers=self.bootstrap_servers,
				value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
				key_serializer=lambda k: k.encode('utf-8') if k else None,
				acks='all',
				retries=3,
				enable_idempotence=True,
				compression_type='gzip'
			)
			await self.producer.start()
			logger.info(f"Kafka producer started: {self.bootstrap_servers}")
            
			self._running = True
            
		except KafkaError as e:
			logger.error(f"Failed to start Kafka client: {e}")
			raise
    
	async def stop(self):
		"""Stop Kafka producer and consumers"""
		self._running = False
        
		if self.producer:
			await self.producer.stop()
			logger.info("Kafka producer stopped")
        
		for topic, consumer in self.consumers.items():
			await consumer.stop()
			logger.info(f"Kafka consumer stopped for topic: {topic}")
    
	async def send_event(self, topic: str, key: str, event: Dict[str, Any]) -> bool:
		"""Send event to Kafka topic"""
		try:
			if not self.producer:
				raise RuntimeError("Kafka producer not initialized")
            
			result = await self.producer.send_and_wait(topic, value=event, key=key)
			logger.info(f"Event sent to {topic}: partition={result.partition}, offset={result.offset}")
			return True
            
		except KafkaError as e:
			logger.error(f"Failed to send event to {topic}: {e}")
			return False
    
	async def consume_events(self, topic: str, callback: Callable):
		"""Consume events from Kafka topic"""
		try:
			consumer = AIOKafkaConsumer(
				topic,
				bootstrap_servers=self.bootstrap_servers,
				group_id=self.group_id,
				value_deserializer=lambda m: json.loads(m.decode('utf-8')),
				key_deserializer=lambda k: k.decode('utf-8') if k else None,
				auto_offset_reset='earliest',
				enable_auto_commit=False
			)
            
			await consumer.start()
			self.consumers[topic] = consumer
			logger.info(f"Started consuming from topic: {topic}")
            
			try:
				async for message in consumer:
					try:
						# Process message with callback
						await callback(message.key, message.value)
                        
						# Commit offset after successful processing
						await consumer.commit()
                        
					except Exception as e:
						logger.error(f"Error processing message from {topic}: {e}")
						# Don't commit on error - message will be reprocessed
                        
			finally:
				await consumer.stop()
                
		except KafkaError as e:
			logger.error(f"Failed to consume from {topic}: {e}")
    
	def is_connected(self) -> bool:
		"""Check if Kafka client is connected"""
		return self._running and self.producer is not None

