#!/usr/bin/env python3
"""
Agricultural Dataset Generator for CropAI
Generates comprehensive, well-structured agronomic datasets based on
ICAR (Indian Council of Agricultural Research) & FAO agronomic standards.
"""

import os
import csv
import random
import math

random.seed(42)

DATASETS_DIR = os.path.join(os.path.dirname(__file__), "datasets")
os.makedirs(DATASETS_DIR, exist_ok=True)

# 1. CROP RECOMMENDATION DATASET
# Features: N (kg/ha), P (kg/ha), K (kg/ha), temperature (°C), humidity (%), pH, rainfall (mm), label
CROPS_CONFIG = {
    "Rice": {
        "N": (60, 110), "P": (35, 60), "K": (35, 60),
        "temp": (20.0, 32.0), "humidity": (75.0, 95.0), "ph": (5.0, 6.8), "rainfall": (180.0, 300.0)
    },
    "Wheat": {
        "N": (90, 140), "P": (45, 65), "K": (35, 55),
        "temp": (14.0, 25.0), "humidity": (50.0, 70.0), "ph": (6.0, 7.5), "rainfall": (50.0, 120.0)
    },
    "Maize": {
        "N": (70, 120), "P": (40, 60), "K": (20, 45),
        "temp": (18.0, 30.0), "humidity": (55.0, 75.0), "ph": (5.8, 7.2), "rainfall": (60.0, 130.0)
    },
    "Chickpea": {
        "N": (20, 50), "P": (55, 80), "K": (65, 90),
        "temp": (15.0, 26.0), "humidity": (45.0, 65.0), "ph": (6.0, 7.8), "rainfall": (40.0, 90.0)
    },
    "Pigeonpea": {
        "N": (20, 50), "P": (55, 80), "K": (40, 65),
        "temp": (20.0, 34.0), "humidity": (45.0, 70.0), "ph": (5.5, 7.5), "rainfall": (60.0, 140.0)
    },
    "Cotton": {
        "N": (100, 150), "P": (40, 65), "K": (40, 65),
        "temp": (22.0, 35.0), "humidity": (55.0, 80.0), "ph": (6.0, 8.0), "rainfall": (60.0, 140.0)
    },
    "Mustard": {
        "N": (60, 95), "P": (30, 50), "K": (20, 40),
        "temp": (12.0, 25.0), "humidity": (45.0, 65.0), "ph": (6.0, 7.5), "rainfall": (35.0, 80.0)
    },
    "Sugarcane": {
        "N": (120, 180), "P": (50, 80), "K": (60, 95),
        "temp": (24.0, 38.0), "humidity": (70.0, 90.0), "ph": (6.0, 7.8), "rainfall": (120.0, 250.0)
    },
    "Potato": {
        "N": (100, 160), "P": (50, 80), "K": (80, 140),
        "temp": (14.0, 24.0), "humidity": (60.0, 80.0), "ph": (5.2, 6.8), "rainfall": (50.0, 110.0)
    },
    "Tomato": {
        "N": (80, 130), "P": (50, 75), "K": (50, 80),
        "temp": (18.0, 30.0), "humidity": (55.0, 75.0), "ph": (6.0, 7.2), "rainfall": (50.0, 120.0)
    },
    "Soybean": {
        "N": (25, 55), "P": (55, 85), "K": (40, 65),
        "temp": (20.0, 32.0), "humidity": (60.0, 80.0), "ph": (6.0, 7.5), "rainfall": (70.0, 150.0)
    }
}

def generate_crop_dataset(samples_per_crop=200):
    filepath = os.path.join(DATASETS_DIR, "crop_recommendation_dataset.csv")
    fieldnames = ["N", "P", "K", "temperature", "humidity", "ph", "rainfall", "crop_label"]
    
    rows = []
    for crop, cfg in CROPS_CONFIG.items():
        for _ in range(samples_per_crop):
            n = round(random.uniform(cfg["N"][0], cfg["N"][1]) + random.gauss(0, 3), 1)
            p = round(random.uniform(cfg["P"][0], cfg["P"][1]) + random.gauss(0, 2), 1)
            k = round(random.uniform(cfg["K"][0], cfg["K"][1]) + random.gauss(0, 2), 1)
            temp = round(random.uniform(cfg["temp"][0], cfg["temp"][1]) + random.gauss(0, 0.8), 2)
            hum = round(max(20.0, min(100.0, random.uniform(cfg["humidity"][0], cfg["humidity"][1]) + random.gauss(0, 2))), 2)
            ph = round(max(4.0, min(9.5, random.uniform(cfg["ph"][0], cfg["ph"][1]) + random.gauss(0, 0.15))), 2)
            rain = round(max(10.0, random.uniform(cfg["rainfall"][0], cfg["rainfall"][1]) + random.gauss(0, 5)), 2)
            
            rows.append({
                "N": max(5.0, n),
                "P": max(5.0, p),
                "K": max(5.0, k),
                "temperature": temp,
                "humidity": hum,
                "ph": ph,
                "rainfall": rain,
                "crop_label": crop
            })
            
    random.shuffle(rows)
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"Generated {len(rows)} samples in {filepath}")
    return filepath

# 2. FERTILIZER RECOMMENDATION DATASET
def generate_fertilizer_dataset(num_samples=1500):
    filepath = os.path.join(DATASETS_DIR, "fertilizer_recommendation_dataset.csv")
    fieldnames = [
        "crop", "soil_type", "nitrogen", "phosphorus", "potassium", "ph", "organic_carbon_pct",
        "recommended_urea_kg_acre", "recommended_dap_kg_acre", "recommended_mop_kg_acre",
        "primary_fertilizer_formula"
    ]
    
    crops = ["Rice", "Wheat", "Maize", "Cotton", "Chickpea", "Mustard", "Sugarcane", "Potato", "Tomato", "Soybean"]
    soil_types = ["Alluvial Loam", "Black Regur", "Red Lateritic", "Sandy Loam", "Clay Loam"]
    
    rows = []
    for _ in range(num_samples):
        crop = random.choice(crops)
        soil = random.choice(soil_types)
        
        n = round(random.uniform(40, 240), 1)
        p = round(random.uniform(8, 65), 1)
        k = round(random.uniform(80, 380), 1)
        ph = round(random.uniform(5.5, 8.5), 2)
        oc = round(random.uniform(0.3, 1.2), 2)
        
        # Calculate scientifically tailored fertilizer needs per acre
        # Target requirements:
        req_n = {"Rice": 50, "Wheat": 55, "Maize": 45, "Cotton": 60, "Chickpea": 15, "Mustard": 35, "Sugarcane": 80, "Potato": 65, "Tomato": 45, "Soybean": 15}[crop]
        req_p = {"Rice": 25, "Wheat": 25, "Maize": 20, "Cotton": 25, "Chickpea": 35, "Mustard": 20, "Sugarcane": 35, "Potato": 40, "Tomato": 30, "Soybean": 35}[crop]
        req_k = {"Rice": 20, "Wheat": 20, "Maize": 15, "Cotton": 25, "Chickpea": 10, "Mustard": 15, "Sugarcane": 40, "Potato": 50, "Tomato": 35, "Soybean": 15}[crop]
        
        # Soil test deficit
        n_deficit = max(0, req_n - (n * 0.18))
        p_deficit = max(0, req_p - (p * 0.40))
        k_deficit = max(0, req_k - (k * 0.08))
        
        # DAP contains 18% N and 46% P
        dap = round(p_deficit / 0.46, 1)
        n_from_dap = dap * 0.18
        
        # Remaining N from Urea (46% N)
        remaining_n = max(0, n_deficit - n_from_dap)
        urea = round(remaining_n / 0.46, 1)
        
        # MOP contains 60% K
        mop = round(k_deficit / 0.60, 1)
        
        if dap > urea and dap > mop:
            primary_formula = "DAP + Zinc Sulphate"
        elif urea > 40:
            primary_formula = "Split Urea (Neem Coated)"
        elif mop > 25:
            primary_formula = "MOP Potash Balanced"
        else:
            primary_formula = "NPK 12:32:16 Complex"
            
        rows.append({
            "crop": crop,
            "soil_type": soil,
            "nitrogen": n,
            "phosphorus": p,
            "potassium": k,
            "ph": ph,
            "organic_carbon_pct": oc,
            "recommended_urea_kg_acre": max(10.0, urea),
            "recommended_dap_kg_acre": max(10.0, dap),
            "recommended_mop_kg_acre": max(5.0, mop),
            "primary_fertilizer_formula": primary_formula
        })
        
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"Generated {len(rows)} samples in {filepath}")
    return filepath

# 3. CROP YIELD & ECONOMICS DATASET
def generate_yield_dataset(num_samples=1800):
    filepath = os.path.join(DATASETS_DIR, "crop_yield_economics_dataset.csv")
    fieldnames = [
        "state", "crop", "season", "farm_acreage", "soil_nitrogen", "soil_phosphorus",
        "soil_potassium", "rainfall_mm", "temperature_avg", "fertilizer_total_kg",
        "yield_quintals_per_acre", "market_price_per_quintal_inr", "net_profit_per_acre_inr"
    ]
    
    states = ["Uttar Pradesh", "Punjab", "Haryana", "Madhya Pradesh", "Bihar", "Rajasthan", "Maharashtra", "Gujarat"]
    crops_yield = {
        "Wheat": (16.0, 24.0, 2275, 12000),     # base_yield_range, MSP/price, cultivation_cost
        "Rice": (18.0, 28.0, 2183, 14000),
        "Maize": (15.0, 25.0, 2090, 11000),
        "Mustard": (6.0, 11.0, 5650, 9500),
        "Chickpea": (6.5, 12.0, 5440, 10000),
        "Cotton": (7.0, 14.0, 6620, 16000),
        "Sugarcane": (280.0, 420.0, 315, 38000),
        "Potato": (90.0, 150.0, 950, 28000)
    }
    
    seasons = ["Kharif", "Rabi", "Zaid"]
    
    rows = []
    for _ in range(num_samples):
        state = random.choice(states)
        crop = random.choice(list(crops_yield.keys()))
        cfg = crops_yield[crop]
        
        season = "Rabi" if crop in ["Wheat", "Mustard", "Chickpea", "Potato"] else "Kharif"
        acreage = round(random.uniform(1.0, 10.0), 1)
        n = round(random.uniform(60, 220), 1)
        p = round(random.uniform(10, 50), 1)
        k = round(random.uniform(100, 320), 1)
        rain = round(random.uniform(200, 1200), 1)
        temp = round(random.uniform(15.0, 36.0), 1)
        fert = round(random.uniform(50, 220), 1)
        
        # Agronomic yield response calculation
        base_min, base_max, price_base, cost_base = cfg
        # Productivity multiplier based on balanced NPK and rainfall
        n_factor = 0.85 + (0.30 * (min(n, 160.0) / 160.0))
        fert_factor = 0.90 + (0.20 * (fert / 180.0))
        climate_factor = 1.0 + random.uniform(-0.08, 0.08)
        
        yield_val = round(random.uniform(base_min, base_max) * n_factor * fert_factor * climate_factor, 2)
        price_val = round(price_base + random.uniform(-100, 200), 0)
        gross_income = yield_val * price_val
        cost_val = round(cost_base + (fert * 18) + (acreage * 200), 0)
        net_profit = round(gross_income - cost_val, 0)
        
        rows.append({
            "state": state,
            "crop": crop,
            "season": season,
            "farm_acreage": acreage,
            "soil_nitrogen": n,
            "soil_phosphorus": p,
            "soil_potassium": k,
            "rainfall_mm": rain,
            "temperature_avg": temp,
            "fertilizer_total_kg": fert,
            "yield_quintals_per_acre": yield_val,
            "market_price_per_quintal_inr": price_val,
            "net_profit_per_acre_inr": net_profit
        })
        
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"Generated {len(rows)} samples in {filepath}")
    return filepath

if __name__ == "__main__":
    print("Generating agricultural datasets...")
    generate_crop_dataset(200)
    generate_fertilizer_dataset(1500)
    generate_yield_dataset(1800)
    print("Datasets successfully written to ml_models/datasets/")
