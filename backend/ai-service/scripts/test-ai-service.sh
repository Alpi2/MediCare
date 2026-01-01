#!/bin/bash
# AI Service Integration Test Script

set -e

AI_SERVICE_URL="http://localhost:8002"

echo "🤖 Testing AI Service"
echo "==================="

echo "1️⃣  Checking AI service health..."
HEALTH=$(curl -s ${AI_SERVICE_URL}/health || true)
if echo "$HEALTH" | grep -q '"status"'; then
    echo "   ✅ AI service responded"
else
    echo "   ❌ AI service did not respond"
    exit 1
fi

echo ""
echo "2️⃣  Checking readiness..."
READY=$(curl -s ${AI_SERVICE_URL}/ready || true)
if echo "$READY" | grep -q '"status"'; then
    echo "   ✅ AI service readiness endpoint responded"
else
    echo "   ⚠️  AI service readiness may not be ready"
fi

echo ""
echo "3️⃣  Listing available models..."
MODELS=$(curl -s ${AI_SERVICE_URL}/api/v1/models || true)
if echo "$MODELS" | grep -q '"models"'; then
    echo "   ✅ Models endpoint working"
    echo "   Models: $MODELS"
else
    echo "   ❌ Models endpoint failed"
fi

# No-show prediction
echo ""
echo "4️⃣  Testing no-show prediction..."
PREDICTION=$(curl -s -X POST ${AI_SERVICE_URL}/api/v1/predict/no-show \
  -H "Content-Type: application/json" \
  -d '{"patient_id":1,"appointment_data":{"patient_age":45,"previous_no_shows":2,"days_since_scheduled":7,"appointment_hour":10}}' || true)

if echo "$PREDICTION" | grep -q '"probability"'; then
    echo "   ✅ No-show prediction working"
    echo "   Result: $PREDICTION"
else
    echo "   ❌ No-show prediction failed"
fi

# Risk scoring
echo ""
echo "5️⃣  Testing risk scoring..."
RISK=$(curl -s -X POST ${AI_SERVICE_URL}/api/v1/predict/risk-score \
  -H "Content-Type: application/json" \
  -d '{"patient_id":1,"patient_data":{"age":65,"cholesterol":240,"glucose_level":120,"previous_admissions":2}}' || true)

if echo "$RISK" | grep -q '"risk_score"'; then
    echo "   ✅ Risk scoring working"
    echo "   Result: $RISK"
else
    echo "   ❌ Risk scoring failed"
fi

# Prometheus metrics
echo ""
echo "6️⃣  Checking Prometheus metrics..."
METRICS=$(curl -s ${AI_SERVICE_URL}/metrics || true)
if echo "$METRICS" | grep -q 'http_requests_total'; then
    echo "   ✅ Prometheus metrics available"
else
    echo "   ⚠️  Prometheus metrics not available"
fi

# API docs
echo ""
echo "7️⃣  Checking API documentation..."
DOCS=$(curl -s ${AI_SERVICE_URL}/docs || true)
if echo "$DOCS" | grep -q 'Swagger'; then
    echo "   ✅ Swagger UI available"
else
    echo "   ⚠️  Swagger UI not available"
fi

echo ""
echo "==================="
echo "✅ AI Service tests completed!"
