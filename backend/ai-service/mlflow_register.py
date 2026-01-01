"""
Example MLflow model logging and registration script for `ai-service`.
"""
from pathlib import Path
import mlflow
import mlflow.sklearn
from sklearn.ensemble import RandomForestClassifier

mlflow.set_tracking_uri("http://mlflow:5000")

def register_example_model():
    X = [[0,0],[1,1],[1,0],[0,1]]
    y = [0,1,1,0]
    clf = RandomForestClassifier(n_estimators=10)
    clf.fit(X, y)

    with mlflow.start_run(run_name="no-show-trial"):
        mlflow.sklearn.log_model(clf, "no-show-predictor", registered_model_name="NoShowPredictor")
        mlflow.log_param("n_estimators", 10)
        mlflow.log_metric("accuracy", clf.score(X,y))

if __name__ == "__main__":
    register_example_model()
