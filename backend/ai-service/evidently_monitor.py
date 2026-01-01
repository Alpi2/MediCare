"""
Basic Evidently Data Drift dashboard example.
Requires `evidently` package.
"""
import pandas as pd
from evidently.dashboard import Dashboard
from evidently.tabs import DataDriftTab


def run_drift_dashboard(reference_df: pd.DataFrame, current_df: pd.DataFrame, out_html: str = "drift_report.html"):
    dashboard = Dashboard(tabs=[DataDriftTab()])
    dashboard.calculate(reference_df, current_df)
    dashboard.save(out_html)
    print(f"Saved drift dashboard to {out_html}")


if __name__ == "__main__":
    # Example: small synthetic data
    ref = pd.DataFrame({"feature1": [0,1,1,0], "feature2": [0.1, 0.2, 0.3, 0.2]})
    cur = pd.DataFrame({"feature1": [1,1,1,0], "feature2": [0.2, 0.2, 0.4, 0.2]})
    run_drift_dashboard(ref, cur)
