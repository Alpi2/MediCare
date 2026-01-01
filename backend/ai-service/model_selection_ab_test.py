"""
Example A/B model selection using a simple feature flag check.
In production use a feature flag service (LaunchDarkly, Unleash) or database-backed flags.
"""
from typing import Any

class ModelManager:
    def __init__(self):
        # placeholder: load model versions from MLflow or local cache
        self.models = {
            "no-show-v1": "model_object_v1",
            "no-show-v2": "model_object_v2",
        }

    def get_model(self, name: str) -> Any:
        return self.models.get(name)


# Simple feature flag stub
class FeatureFlags:
    def __init__(self, flags=None):
        self.flags = flags or {}

    def is_enabled(self, flag_name: str) -> bool:
        return self.flags.get(flag_name, False)


def choose_model(feature_flags: FeatureFlags, model_manager: ModelManager):
    if feature_flags.is_enabled("new_no_show_model"):
        return model_manager.get_model("no-show-v2")
    return model_manager.get_model("no-show-v1")


if __name__ == "__main__":
    fm = FeatureFlags({"new_no_show_model": True})
    mm = ModelManager()
    model = choose_model(fm, mm)
    print("Selected model:", model)
