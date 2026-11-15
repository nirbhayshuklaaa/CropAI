# CropAI Usability & Farmer Advisory Expansion Plan

An advanced usability expansion for CropAI delivering a pest & disease diagnostic scanner with organic remedies, an interactive crop yield and net profit calculator, and seamless text-to-speech voice readouts for field accessibility.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during interactive clarification and form the foundation of this release:

- **Confirmed Decision 1 (Primary High-Impact Feature)**: Implemented a dedicated **Pest & Disease Diagnostic Scanner** (`PestDiagnosticScreen`) featuring visual symptom classification, infected leaf photo capture / gallery pick, crop-specific pathology catalog (Wheat, Rice, Cotton, Tomato, Potato, Maize, Sugarcane, Mustard), and step-by-step biological/organic treatments alongside chemical controls and preventative agronomy.
- **Confirmed Decision 2 (Economic Decision Tool)**: Implemented an interactive **Crop Yield & Profitability Calculator** (`ProfitabilityCalculatorScreen`) calculating input expenditure (seeds, fertilizer, irrigation, labor, machinery) against expected quintal yields and market/MSP prices, producing net income, margin %, and sensitivity breakdown.
- **Confirmed Decision 3 (Rural Accessibility Voice Readout)**: Implemented a non-blocking bilingual **Text-To-Speech (TTS) Voice Engine** (`SpeechAdvisoryManager` / `VoiceAdvisoryFab`) allowing farmers in both Hindi (हिंदी) and English to listen to soil health results, crop advisories, and diagnostic recommendations aloud with one tap.
- **Navigation Integration (Recommended Default)**: Add new dedicated entries to the Home quick-action grid and Navigation Bar / Top App Bar without cluttering the existing clean light green M3 layout, ensuring all touch targets remain >= 48dp.

---

### 1. Overview & Core Concept

- **What It Does**: Enhances CropAI from a soil-and-fertilizer advisor into an end-to-end everyday agricultural companion. Farmers can diagnose crop pests and leaf diseases instantly with organic remedies, project financial profitability per acre before planting, and listen to spoken agronomic advisories directly in their fields.
- **Target Audience / Persona**: Smallholder and commercial farmers, village agricultural extension workers, and kisan service providers who need immediate actionable advice on their mobile devices without complex jargon.
- **Key Value**: Closes the loop from soil preparation to crop economics and crop protection, directly preventing yield loss from unchecked infestations and improving farm income transparency.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Pest & Disease Diagnosis Flow**:
   - Tap **"Pest & Disease Doctor / फसल सुरक्षा"** from Home or Knowledge Center.
   - Select affected crop (or auto-filter by farmer's current saved farm crop) and tap either **"Capture Leaf Photo"** or pick symptom indicators (leaf discoloration, yellow spots, wilting, stem bore).
   - Instant diagnostic card displays: Disease name (Hindi & English), confidence rating, severity badge, organic/neem-based remedies, chemical alternatives with safe dosage, and prevention tips.
   - Farmers can tap **"Listen Advisory 🔊"** or share the diagnosis directly to WhatsApp/SMS.

2. **Crop Yield & Profitability Calculator Flow**:
   - Tap **"Profit & Yield Calculator / आय कैलकुलेटर"** on Home.
   - Enter or adjust cultivated acreage, choose crop, and review auto-populated benchmark cultivation expenses (seeds, NPK fertilizers, irrigation, labor).
   - Real-time dynamic KPI card recalculates: Total Estimated Cost, Projected Yield (Quintals), Market Revenue, and Net Profit per acre with profit margin badges.
   - Ability to compare alternative crops side-by-side to make data-backed sowing decisions.

3. **Audio Voice Readout (TTS) Experience**:
   - A floating and inline speaker button (🔊) appears across Soil Analysis results, Crop Advisory cards, and Disease diagnoses.
   - Tapping toggles natural voice synthesis matching the active app language (Hindi or English), with active playback wave indication and instant pause/stop control.

#### Visual Identity & Theme
- **Color Palette**: Built on the established M3 Light Green dynamic palette (`#2E6B34` Primary, `#4E644E` Secondary, `#246D5E` Tertiary, `#F6FBF2` Fresh Meadow Background, `#E0E8DC` Surface Variant).
- **Disease & Health Tokens**: High-contrast severity pills (Warning Amber `#F59E0B`, Danger Red `#DC2626`, Organic Green `#16A34A`).
- **Typography & Scale**: Clear hierarchical typography (HeadlineMedium 24sp, TitleMedium 16sp bold, BodyMedium 14sp, LabelLarge 14sp) ensuring high readability under direct outdoor sunlight.
- **Layout Precision**: Edge-to-edge layout adhering to `WindowInsets.safeDrawing`, 48dp minimum touch targets, card padding on the 8dp grid, and responsive width containers for tablets/foldables.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Android Text-To-Speech (TTS) over Heavy Cloud Speech APIs**
  - *Chosen Approach*: Leverage Android's built-in `android.speech.tts.TextToSpeech` engine with locale auto-detection (`Locale("hi", "IN")` for Hindi and `Locale.ENGLISH` for English).
  - *Why*: Instant zero-latency playback, 100% offline functionality in rural connectivity blackspots, zero cloud API costs or authentication quotas.
  - *Alternatives Considered*: Cloud TTS REST API (rejected due to network latency, offline failure in fields, and API credential overhead).

- **Decision 2: Comprehensive On-Device Diagnostic Pathology Catalog with Photo Picker**
  - *Chosen Approach*: Deliver a curated agronomic diagnostic engine for top Indian and global staples (Wheat rust/blight, Rice blast/brown spot, Cotton bollworm/leaf curl, Tomato early/late blight, Potato blight, Maize fall armyworm) pairing symptom checklist filtering with Camera/Photo capture.
  - *Why*: Guarantees instant, reliable advice even with low rural network bandwidth, giving farmers dependable organic bio-remedies (neem oil, Trichoderma, cow urine extract, crop rotation) immediately.

- **Decision 3: Integrated Farm State Synchronization with Profitability Engine**
  - *Chosen Approach*: Automatically pre-fill the calculator from the farmer's registered fields and active soil test results, while allowing instant manual slider/numeric adjustments.
  - *Why*: Reduces repetitive data entry for the farmer while maintaining full customization for local mandi rates.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          CropAI Mobile Client                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────────────┐
│           UI Screen Layer            │  │          Core Services Layer         │
│  - HomeScreen (New Quick Actions)    │  │  - SpeechAdvisoryManager (TTS)       │
│  - PestDiagnosticScreen              │  │  - PestDiseaseRepository             │
│  - ProfitabilityCalculatorScreen     │  │  - ProfitCalculationEngine           │
│  - VoiceAdvisoryFab Component        │  │  - Android PhotoPicker Contract      │
└──────────────────┬───────────────────┘  └──────────────────┬───────────────────┘
                   │                                         │
                   ▼                                         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   MainViewModel & Agricultural Repository              │
│  - Exposes disease catalog, symptoms, diagnoses, profit calculations   │
│  - Manages audio playback state (isPlaying, activeSnippet)             │
│  - Firestore persistent backup for saved diagnoses & custom budgets    │
└────────────────────────────────────────────────────────────────────────┘
```

#### Data Models & State Entities
- `PestDiseaseItem`: `id`, `cropName`, `diseaseName`, `diseaseNameHindi`, `symptoms`, `severity`, `organicRemedy`, `chemicalRemedy`, `preventiveMeasures`, `sampleImageUrl`.
- `DiagnosticResult`: `disease`, `matchedSymptoms`, `confidenceScore`, `timestamp`.
- `CropProfitProfile`: `cropName`, `seedCostPerAcre`, `fertilizerCostPerAcre`, `irrigationCostPerAcre`, `laborCostPerAcre`, `otherCostPerAcre`, `averageYieldQuintals`, `estimatedPricePerQuintal`.
- `ProfitCalculation`: `grossRevenue`, `totalExpense`, `netProfit`, `profitMarginPercent`, `breakEvenPricePerQuintal`.

#### Interactive Component & State Mapping
- `PestDiagnosticScreen`: Crop selector chips -> Symptom filter checkboxes or Photo Picker -> Instant diagnosis card with Organic Remedy tab & Chemical Treatment tab -> Audio speech readout button & Share sheet.
- `ProfitabilityCalculatorScreen`: Acreage stepper, Cost breakdown expandable card, MSP/Market rate slider -> Live dynamic profit computation card with ROI badge.
- `VoiceAdvisoryFab`: Global composable listening to current screen advisory text, speaking with Android TTS in Hindi/English, updating speaker animated visualizer.
