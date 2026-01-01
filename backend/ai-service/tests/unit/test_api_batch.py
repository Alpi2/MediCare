from fastapi.testclient import TestClient
import pandas as pd

from main import app


class DummyModel:
    def predict_batch(self, df: pd.DataFrame):
        # simple predictable behavior: 1 if a column 'x' > 0 else 0
        preds = [1 if row.get('x', 0) > 0 else 0 for _, row in df.to_dict(orient='index').items()]
        probs = [0.9 if p == 1 else 0.1 for p in preds]
        return {"predictions": preds, "probabilities": probs}


def test_batch_predict_validation_error():
    client = TestClient(app)

    # Missing 'data' field should trigger a 422 Unprocessable Entity from FastAPI
    payload = {"model_name": "no_model"}
    resp = client.post("/api/v1/predict/batch", json=payload)
    assert resp.status_code == 422


def test_batch_predict_success():
    # inject a simple model manager with a get_model method
    class DummyManager:
        def get_model(self, name):
            if name == "dummy":
                return DummyModel()
            raise KeyError("model not found")

    # attach to app state so endpoint can find it via get_model_manager
    app.state.model_manager = DummyManager()
    client = TestClient(app)

    payload = {
        "model_name": "dummy",
        "data": [{"x": 1}, {"x": -1}, {"x": 0}]
    }
    resp = client.post("/api/v1/predict/batch", json=payload)
    assert resp.status_code == 200
    body = resp.json()
    assert "predictions" in body
    assert body["predictions"] == [1, 0, 0]
    assert "probabilities" in body
    assert isinstance(body["probabilities"], list)
