import pytest
from fastapi.testclient import TestClient

from main import app


class DummyModelRaises:
    def __init__(self, exc):
        self._exc = exc

    def predict_proba_single(self, data):
        raise self._exc


class DummyManager:
    def __init__(self, model):
        self._model = model

    def get_model(self, name):
        return self._model


@pytest.fixture(autouse=True)
def client():
    client = TestClient(app)
    yield client


def make_payload():
    return {
        "patient_id": 2001,
        "appointment_data": {
            "patient_age": 54,
            "previous_no_shows": 2,
            "appointment_lead_time": 3,
            "day_of_week": "Mon",
            "is_weekend": False,
            "referral_source": "GP"
        }
    }


def test_predict_no_show_internal_exception_returns_sanitized_message(client):
    # Simulate model that raises a generic Exception with internal message
    app.state.model_manager = DummyManager(DummyModelRaises(Exception("internal details: secret")))
    resp = client.post("/api/v1/predict/no-show", json=make_payload())
    assert resp.status_code == 500
    body = resp.json()
    assert "error" in body
    assert body["error"]["message"] == "Prediction failed"


def test_predict_no_show_value_error_returns_400_and_sanitized(client):
    # Simulate model that raises ValueError (mapped to 400)
    app.state.model_manager = DummyManager(DummyModelRaises(ValueError("bad features: leak")))
    resp = client.post("/api/v1/predict/no-show", json=make_payload())
    assert resp.status_code == 400
    body = resp.json()
    assert "error" in body
    assert body["error"]["message"] == "Invalid input"
