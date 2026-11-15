#!/usr/bin/env python3
"""
CropAI Prediction CLI (Python)
Allows testing of all trained Python models directly from command line.
"""

import os
import json
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, "trained_models")

def predict_crop(node, x):
    if node.get("type") == "leaf":
        return node["predicted_crop"], node.get("probabilities", {})
    feature_idx = node["feature_index"]
    threshold = node["threshold"]
    if x[feature_idx] <= threshold:
        return predict_crop(node["left"], x)
    return predict_crop(node["right"], x)

def run_crop_prediction(n=110, p=45, k=40, temp=22.0, humidity=65.0, ph=6.8, rainfall=80.0):
    model_path = os.path.join(MODELS_DIR, "crop_recommendation_model.json")
    if not os.path.exists(model_path):
        print("Model not found. Run python3 train_models.py first.")
        return
        
    with open(model_path, "r", encoding="utf-8") as f:
        model = json.load(f)
        
    x = [float(n), float(p), float(k), float(temp), float(humidity), float(ph), float(rainfall)]
    crop, probs = predict_crop(model["tree_structure"], x)
    print(f"\n--- Crop Recommendation Result ---")
    print(f"Inputs: N={n}, P={p}, K={k}, Temp={temp}°C, Humidity={humidity}%, pH={ph}, Rain={rainfall}mm")
    print(f"Recommended Crop: {crop}")
    if probs:
        print("Class Probabilities:", json.dumps(probs, indent=2))
    return crop

def run_fertilizer_prediction(crop="Wheat", soil_type="Alluvial Loam", n=120, p=22, k=180):
    model_path = os.path.join(MODELS_DIR, "fertilizer_recommendation_model.json")
    with open(model_path, "r", encoding="utf-8") as f:
        model = json.load(f)
        
    rule = model["rules"].get(crop, model["rules"].get("Wheat"))
    soil_mult = rule["soil_factors"].get(soil_type, {"urea_multiplier": 1.0, "dap_multiplier": 1.0, "mop_multiplier": 1.0})
    
    urea = round(rule["baseline_urea_kg_acre"] * soil_mult["urea_multiplier"], 1)
    dap = round(rule["baseline_dap_kg_acre"] * soil_mult["dap_multiplier"], 1)
    mop = round(rule["baseline_mop_kg_acre"] * soil_mult["mop_multiplier"], 1)
    
    print(f"\n--- Fertilizer Prescription Result ---")
    print(f"Crop: {crop} | Soil: {soil_type} | N:{n} P:{p} K:{k}")
    print(f"Recommended Primary Formula: {rule['recommended_primary_formula']}")
    print(f"Dosage Per Acre:")
    print(f"  • Urea: {urea} kg/acre")
    print(f"  • DAP : {dap} kg/acre")
    print(f"  • MOP (Potash): {mop} kg/acre")

def run_yield_prediction(crop="Wheat", n=120, p=22, k=180, rain=80, temp=22, fert=110):
    model_path = os.path.join(MODELS_DIR, "crop_yield_regression_model.json")
    with open(model_path, "r", encoding="utf-8") as f:
        model = json.load(f)
        
    crop_model = model["crops"].get(crop, model["crops"].get("Wheat"))
    features = [float(n), float(p), float(k), float(rain), float(temp), float(fert)]
    pred_yield = round(crop_model["intercept"] + sum(w * x for w, x in zip(crop_model["weights"], features)), 2)
    pred_profit = round((pred_yield * crop_model["avg_market_price_inr"]) - (12000 + fert * 18), 0)
    
    print(f"\n--- Yield & Profitability Result ---")
    print(f"Crop: {crop} | Estimated Yield: {pred_yield} quintals/acre")
    print(f"Projected Market Price: ₹{crop_model['avg_market_price_inr']}/quintal")
    print(f"Projected Net Profit  : ₹{pred_profit}/acre")

if __name__ == "__main__":
    print("Testing CropAI Machine Learning Models via Python CLI...")
    run_crop_prediction(110, 45, 40, 22.0, 65.0, 6.8, 80.0)
    run_fertilizer_prediction("Wheat", "Alluvial Loam", 120, 22, 180)
    run_yield_prediction("Wheat", 120, 22, 180, 80, 22, 110)
