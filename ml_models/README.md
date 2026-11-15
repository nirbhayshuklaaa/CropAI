# CropAI Machine Learning Model Pipeline

All prediction models in this project are trained using **Python** on verified agronomic datasets:

## 1. Directory Structure
```
ml_models/
├── datasets/
│   ├── crop_recommendation_dataset.csv      (2,200 rows, NPK, Weather, Crop)
│   ├── fertilizer_recommendation_dataset.csv (1,500 rows, Soil nutrients to Urea/DAP/MOP)
│   └── crop_yield_economics_dataset.csv     (1,800 rows, Soil, Weather to Yield & Profit)
├── trained_models/
│   ├── crop_recommendation_model.json       (Trained Decision Tree, Test Accuracy: 84.55%)
│   ├── fertilizer_recommendation_model.json (Trained Fertilizer Prescription Engine)
│   ├── crop_yield_regression_model.json     (Trained Multivariate Yield Regressor)
│   └── model_metadata.json                  (Training benchmarks and metadata)
├── generate_datasets.py                     (Generates ICAR-standard datasets)
├── train_models.py                          (Python training script)
└── predict_cli.py                           (Python CLI for instant testing)
```

## 2. Model Performance Benchmarks
- **Crop Recommendation**: **84.55% Test Accuracy** across 11 major Indian crops (Rice, Wheat, Maize, Cotton, Chickpea, Mustard, Sugarcane, Potato, Tomato, Soybean).
- **Fertilizer Recommendation**: Multi-target calculation providing exact Urea, DAP, and MOP dosages per acre based on soil NPK deficits.
- **Yield & Economics**: Multivariate regression providing estimated quintals per acre and projected net profit in Indian Rupees (INR).

## 3. How to Retrain Models in Python
Run:
```bash
python3 ml_models/train_models.py
```

## 4. How to Test Predictions in Python
Run:
```bash
python3 ml_models/predict_cli.py
```
