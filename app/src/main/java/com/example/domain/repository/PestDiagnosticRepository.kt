package com.example.domain.repository

import com.example.domain.model.DiagnosticResult
import com.example.domain.model.DiseaseSeverity
import com.example.domain.model.PestDiseaseItem

class PestDiagnosticRepository {

    private val diseaseCatalog = listOf(
        // RICE / धान
        PestDiseaseItem(
            id = "rice_blast",
            cropName = "Rice",
            diseaseName = "Rice Blast (Magnaporthe oryzae)",
            diseaseNameHindi = "धान का झुलसा रोग (राइस ब्लास्ट)",
            pathogenType = "Fungal",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Spindle-shaped or eye-shaped lesions with gray center on leaves",
                "Dark brown margins around spots",
                "Black rot at neck node causing panicle breakage",
                "Premature lodging of tillers"
            ),
            symptomsHindi = listOf(
                "पत्तियों पर आँख के आकार के बीच में धूसर धब्बे",
                "धब्बों के चारों ओर गहरे भूरे रंग की किनारी",
                "बाली के पास गर्दन पर काला सड़न का निशान",
                "पौधों का असमय गिरना"
            ),
            organicRemedy = "Spray Neem seed kernel extract (NSKE 5%) or Pseudomonas fluorescens @ 10g/L. Apply fermented butter milk diluted 1:10 at early stage.",
            organicRemedyHindi = "नीम बीज अर्क (NSKE 5%) या स्यूडोमोनास फ्लोरेसेंस 10 ग्राम/लीटर छिड़कें। प्रारंभिक अवस्था में खट्टी छाछ (1:10) का छिड़काव करें।",
            chemicalRemedy = "Tricyclazole 75% WP @ 0.6g/L or Isoprothiolane 40% EC @ 1.5ml/L at boot leaf stage.",
            chemicalRemedyHindi = "ट्राइसाइक्लाजोल 75% WP 0.6 ग्राम/लीटर या आइसोप्रोथियोलेन 40% EC 1.5 मिली/लीटर का छिड़काव करें।",
            preventiveMeasures = listOf(
                "Avoid excessive nitrogen fertilizer application",
                "Treat seeds with Trichoderma viride @ 4g/kg seed before sowing",
                "Maintain proper spacing and water drainage"
            ),
            preventiveMeasuresHindi = listOf(
                "यूरिया (नाइट्रोजन) की अत्यधिक मात्रा से बचें",
                "बुवाई से पहले ट्राइकोडर्मा विरिडी (4 ग्राम/किग्रा) से बीज उपचार करें",
                "खेत में जल निकासी की उचित व्यवस्था रखें"
            )
        ),
        PestDiseaseItem(
            id = "rice_brown_plant_hopper",
            cropName = "Rice",
            diseaseName = "Brown Plant Hopper (BPH)",
            diseaseNameHindi = "धान का भूरा माहू (बीपीएच कीट)",
            pathogenType = "Insect Pest",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Circular patches of drying plants ('Hopper burn')",
                "Tiny brown insects clustered at stem base near water level",
                "Leaves turning yellow-orange then rapidly brown and dry",
                "Sooty mold growth on lower leaf sheaths"
            ),
            symptomsHindi = listOf(
                "खेत में गोल घेरे में पौधों का सूखना (हॉपर बर्न)",
                "पानी की सतह के पास तने पर छोटे भूरे कीड़े",
                "पत्तियों का पीला और भूरा होकर सूख जाना",
                "तने पर काले कवक की फफूंद जमना"
            ),
            organicRemedy = "Drain standing water for 3 days to break humidity cycle. Spray Neem oil (10,000 ppm) @ 3ml/L targeting the plant base. Release mirid bug natural predators.",
            organicRemedyHindi = "खेत से 2-3 दिन पानी निकालकर सुखाएं। 10,000 ppm नीम का तेल (3 मिली/लीटर) तने के निचले भाग पर छिड़कें।",
            chemicalRemedy = "Triflumuron + Pymetrozine 50% WG @ 120g/acre or Dinotefuran 20% SG @ 80g/acre.",
            chemicalRemedyHindi = "पाइमेट्रोज़िन 50% WG 120 ग्राम/एकड़ या डाइनोटेफ्यूरन 20% SG 80 ग्राम/एकड़ का छिड़काव करें।",
            preventiveMeasures = listOf(
                "Create alleyways (skip one row every 2-3 meters) for aeration and sunlight",
                "Avoid prophylactic synthetic pyrethroid sprays which kill beneficial spiders"
            ),
            preventiveMeasuresHindi = listOf(
                "हवा और धूप के लिए हर 2-3 मीटर पर एक पंक्ति खाली छोड़ें",
                "सिंथेटिक कीटनाशकों के अंधाधुंध छिड़काव से बचें जो मकड़ियों को मारते हैं"
            )
        ),

        // WHEAT / गेहूं
        PestDiseaseItem(
            id = "wheat_yellow_rust",
            cropName = "Wheat",
            diseaseName = "Yellow / Stripe Rust (Puccinia striiformis)",
            diseaseNameHindi = "गेहूं का पीला रतुआ (हल्दी रोग)",
            pathogenType = "Fungal",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Parallel yellow-orange powdery stripe pustules on leaf surface",
                "Yellow powder sticks to fingers when leaf is touched",
                "Leaves shrivel, dry, and lose green photosynthesis area",
                "Premature maturity with shriveled grain"
            ),
            symptomsHindi = listOf(
                "पत्तियों पर पीले-नारंगी चूर्ण की समानांतर धारियां",
                "पत्ती छूने पर उंगली पर हल्दी जैसा पीला पाउडर लगना",
                "पत्तियों का सूखना और प्रकाश संश्लेषण रुकना",
                "दाने सिकुड़ना और उपज में भारी गिरावट"
            ),
            organicRemedy = "Spray fermented bio-solution: 10% cow urine + garlic extract (500ml/15L pump). Dust sulfur powder in early morning dew.",
            organicRemedyHindi = "गौमूत्र (10%) + लहसुन अर्क का छिड़काव करें। सुबह ओस के समय सल्फर पाउडर का बुरकाव करें।",
            chemicalRemedy = "Propiconazole 25% EC (Tilt) @ 1ml/L water immediately upon seeing first yellow stripe.",
            chemicalRemedyHindi = "प्रोपिकोनाज़ोल 25% EC (टिल्ट) 1 मिली/लीटर पानी में घोलकर तुरंत छिड़काव करें।",
            preventiveMeasures = listOf(
                "Sow certified rust-resistant varieties like DBW 187, DBW 222, HD 3086",
                "Inspect northern perimeter and shade rows weekly from December to February"
            ),
            preventiveMeasuresHindi = listOf(
                "रोग रोधी किस्में (DBW 187, DBW 222, HD 3086) की ही बुवाई करें",
                "दिसंबर से फरवरी तक खेतों का नियमित निरीक्षण करें"
            )
        ),
        PestDiseaseItem(
            id = "wheat_aphids",
            cropName = "Wheat",
            diseaseName = "Wheat Aphids (Mahun / Chepa)",
            diseaseNameHindi = "गेहूं का माहू (चेपा कीट)",
            pathogenType = "Insect Pest",
            severity = DiseaseSeverity.MEDIUM,
            keySymptoms = listOf(
                "Small soft green/black insects clustered on tender earheads and leaves",
                "Sticky honeydew on earheads followed by black mold",
                "Stunted earhead development and reduced grain filling"
            ),
            symptomsHindi = listOf(
                "बालियों और पत्तियों पर चिपके छोटे हरे/काले कीड़े",
                "बालियों पर चिपचिपा रस और काले फफूंद का जमाव",
                "बालियों का अविकसित रहना और दानों का कमजोर होना"
            ),
            organicRemedy = "Spray 5% Neem seed kernel extract (NSKE) or Verticillium lecanii entomopathogenic fungus @ 5g/L.",
            organicRemedyHindi = "5% नीम का काढ़ा या वर्टिसिलियम लेकानी (5 ग्राम/लीटर) शाम के समय छिड़कें।",
            chemicalRemedy = "Thiamethoxam 25% WG @ 0.3g/L or Imidacloprid 17.8% SL @ 0.5ml/L water.",
            chemicalRemedyHindi = "थियामेथोक्सम 25% WG 0.3 ग्राम/लीटर या इमिडाक्लोप्रिड 17.8% SL 0.5 मिली/लीटर छिड़कें।",
            preventiveMeasures = listOf(
                "Conserve natural predators like ladybird beetles and hoverfly larvae",
                "Install yellow sticky traps @ 10-12 per acre"
            ),
            preventiveMeasuresHindi = listOf(
                "लेडीबर्ड भृंग (मित्र कीट) का संरक्षण करें",
                "प्रति एकड़ 10-12 पीले चिपचिपे ट्रैप (येलो स्टिकी ट्रैप) लगाएं"
            )
        ),

        // TOMATO / टमाटर
        PestDiseaseItem(
            id = "tomato_early_blight",
            cropName = "Tomato",
            diseaseName = "Tomato Early Blight (Alternaria solani)",
            diseaseNameHindi = "टमाटर का अगेती झुलसा रोग",
            pathogenType = "Fungal",
            severity = DiseaseSeverity.MEDIUM,
            keySymptoms = listOf(
                "Concentric dark brown rings forming 'bullseye' target patterns on older leaves",
                "Yellow halo surrounding brown circular lesions",
                "Defoliation starting from bottom leaves upward",
                "Sunken dark leathery rot at fruit stem end"
            ),
            symptomsHindi = listOf(
                "निचली पत्तियों पर गहरे भूरे छल्लेदार 'टारगेट' जैसे धब्बे",
                "धब्बों के चारों ओर पीले रंग का घेरा",
                "नीचे से ऊपर की ओर पत्तियों का सूखकर गिरना",
                "फल के डंठल के पास काले सूखे गड्ढेदार धब्बे"
            ),
            organicRemedy = "Foliar spray of Trichoderma harzianum @ 5g/L + Copper hydroxide or Bordeaux mixture 1%. Prune lower leaves touching soil.",
            organicRemedyHindi = "ट्राइकोडर्मा हार्ज़ियानम 5 ग्राम/लीटर या बोर्डो मिश्रण 1% का छिड़काव। मिट्टी छूने वाली निचली पत्तियों को काटें।",
            chemicalRemedy = "Mancozeb 75% WP @ 2.5g/L or Azoxystrobin 18.2% + Difenoconazole 11.4% SC @ 1ml/L.",
            chemicalRemedyHindi = "मैंकोज़ेब 75% WP 2.5 ग्राम/लीटर या एज़ोक्सीस्ट्रोबिन + डाइफेनोकोनाज़ोल 1 मिली/लीटर का छिड़काव करें।",
            preventiveMeasures = listOf(
                "Stake plants upright with bamboo to prevent soil contact",
                "Drip irrigation instead of overhead sprinklers to keep foliage dry"
            ),
            preventiveMeasuresHindi = listOf(
                "पौधों को बांस के सहारे बांधें ताकि पत्तियां मिट्टी न छुएं",
                "फव्वारे की जगह ड्रिप सिंचाई अपनाएं जिससे पत्तियां सूखी रहें"
            )
        ),

        // POTATO / आलू
        PestDiseaseItem(
            id = "potato_late_blight",
            cropName = "Potato",
            diseaseName = "Potato Late Blight (Phytophthora infestans)",
            diseaseNameHindi = "आलू का पछेती झुलसा रोग",
            pathogenType = "Fungal",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Water-soaked dark lesions on leaf tips and margins spreading rapidly",
                "White cottony fungal downy growth on underside of leaves in humid mornings",
                "Foul odor in potato field with rapid blackening of foliage",
                "Brown, granular dry rot under tuber skin"
            ),
            symptomsHindi = listOf(
                "पत्तियों के किनारों पर पानी से भीगे जैसे गहरे धब्बे",
                "सुबह के समय पत्ती के नीचे सफेद रुई जैसी फफूंद",
                "खेत में सड़ांध जैसी गंध और तेजी से पौधों का काला पड़ना",
                "आलू के कंदों के अंदर भूरा सूखा सड़ाव"
            ),
            organicRemedy = "Copper oxychloride (COC 50% WP) @ 2.5g/L. Spray diluted cow urine mixed with hing (asafoetida) as bio-fungicide.",
            organicRemedyHindi = "कॉपर ऑक्सीक्लोराइड 2.5 ग्राम/लीटर छिड़कें। हींग और गौमूत्र का मिश्रण जैविक फफूंदनाशक के रूप में प्रयोग करें।",
            chemicalRemedy = "Cymoxanil 8% + Mancozeb 64% WP @ 2g/L or Metalaxyl-M 4% + Mancozeb 64% WP @ 2.5g/L.",
            chemicalRemedyHindi = "साइमोक्सानिल + मैंकोज़ेब 2 ग्राम/लीटर या मेटालैक्सिल + मैंकोज़ेब 2.5 ग्राम/लीटर का तुरंत छिड़काव करें।",
            preventiveMeasures = listOf(
                "Avoid planting non-certified cold storage tubers",
                "Perform high earthing up to prevent zoospores washing down to tubers"
            ),
            preventiveMeasuresHindi = listOf(
                "प्रमाणित रोगमुक्त बीज कंदों का ही प्रयोग करें",
                "आलू पर मिट्टी अच्छी तरह चढ़ाएं ताकि फफूंद कंद तक न पहुंचे"
            )
        ),

        // COTTON / कपास
        PestDiseaseItem(
            id = "cotton_pink_bollworm",
            cropName = "Cotton",
            diseaseName = "Pink Bollworm (Pectinophora gossypiella)",
            diseaseNameHindi = "कपास की गुलाबी सुंडी",
            pathogenType = "Insect Pest",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Rosetted flowers that fail to open ('Rosette stage')",
                "Tiny bore holes in green bolls with frass accumulation",
                "Premature dropping of squares and young bolls",
                "Interlocked stained lint with hollow seeds inside boll"
            ),
            symptomsHindi = listOf(
                "गुलाब की कली जैसे बंद फूल जो खुलते नहीं (रोज़ेट फूल)",
                "हरे टिंडों में छेद और कीट का मल दिखाई देना",
                "टिंडों का समय से पहले गिरना",
                "कपास की रुई का पीला व खराब होना तथा बीज खोखले होना"
            ),
            organicRemedy = "Install 4-5 Pheromone traps per acre with Gossyplure lure. Release Trichogramma bactrae egg parasitoid wasps @ 60,000/acre weekly.",
            organicRemedyHindi = "प्रति एकड़ 4-5 फेरोमोन ट्रैप लगाएं। ट्राइकोग्रामा परजीवी मित्र कीट (60,000 प्रति एकड़) प्रति सप्ताह छोड़ें।",
            chemicalRemedy = "Emamectin benzoate 5% SG @ 0.4g/L or Chlorantraniliprole 18.5% SC (Coragen) @ 0.3ml/L.",
            chemicalRemedyHindi = "इमामेक्टिन बेंजोएट 5% SG 0.4 ग्राम/लीटर या कोराजन 0.3 मिली/लीटर का छिड़काव करें।",
            preventiveMeasures = listOf(
                "Destroy cotton stalks after harvest without storing for firewood",
                "Observe closed season of at least 4 months between crops"
            ),
            preventiveMeasuresHindi = listOf(
                "कटाई के बाद कपास की डंठलों (पराली) को नष्ट करें",
                "कपास की फसल के बीच कम से कम 4 महीने का अंतराल रखें"
            )
        ),

        // MAIZE / मक्का
        PestDiseaseItem(
            id = "maize_fall_armyworm",
            cropName = "Maize",
            diseaseName = "Fall Armyworm (Spodoptera frugiperda)",
            diseaseNameHindi = "मक्का की फॉल आर्मीवॉर्म (सैनिक कीट)",
            pathogenType = "Insect Pest",
            severity = DiseaseSeverity.HIGH,
            keySymptoms = listOf(
                "Shot-hole and ragged, torn feeding damage on whorl leaves",
                "Copious sawdust-like frass (excreta) deep inside the plant whorl",
                "Larva has inverted 'Y' mark on head and 4 dots on second-to-last segment",
                "Direct burrowing damage to corn ears and silk"
            ),
            symptomsHindi = listOf(
                "पौधे की पत्तों में बड़े-बड़े कटे-फटे छेद (गोभ में क्षति)",
                "पौधे के बीच (गोभ) में लकड़ी के बुरादे जैसा कीट का मल",
                "कीट के सिर पर उल्टा 'Y' का निशान और शरीर पर चार काले बिंदु",
                "भुट्टे के दानों और रेशों को सीधे कुतरना"
            ),
            organicRemedy = "Apply dry sand or wood ash + fine lime directly into whorl (10:1 ratio). Spray Bacillus thuringiensis (Bt kurstaki) @ 2g/L or Beauveria bassiana @ 5g/L.",
            organicRemedyHindi = "गोभ में सूखी रेत या लकड़ी की राख डालें। बैसिलस थुरिंजिएंसिस (Bt) 2 ग्राम/लीटर या बवेरिया बासियाना का छिड़काव करें।",
            chemicalRemedy = "Spinetoram 11.7% SC @ 0.5ml/L or Chlorantraniliprole 18.5% SC @ 0.4ml/L targeting central whorl.",
            chemicalRemedyHindi = "स्पिनेटोरम 11.7% SC 0.5 मिली/लीटर या क्लोरेंट्रानिलिप्रोल 0.4 मिली/लीटर सीधे गोभ में स्प्रे करें।",
            preventiveMeasures = listOf(
                "Intercrop with cowpea or desmodium as push-pull strategy",
                "Erect bird perches (T-shaped bamboo sticks) @ 10/acre for predatory birds"
            ),
            preventiveMeasuresHindi = listOf(
                "लोबिया की अंतःफसल (इंटरक्रॉपिंग) लगाएं",
                "खेत में पक्षियों के बैठने के लिए 'T' आकार की खूंटियां (10 प्रति एकड़) लगाएं"
            )
        ),

        // MUSTARD / सरसों
        PestDiseaseItem(
            id = "mustard_aphid",
            cropName = "Mustard",
            diseaseName = "Mustard Aphid (Lipaphis erysimi)",
            diseaseNameHindi = "सरसों का चेपा (माहू कीट)",
            pathogenType = "Insect Pest",
            severity = DiseaseSeverity.MEDIUM,
            keySymptoms = listOf(
                "Massive colonies of yellow-green tiny aphids sucking sap from inflorescence",
                "Curling and distortion of flowers and young pods",
                "Pods fail to form healthy seeds and turn papery",
                "Honey-dew excretion attracting black sooty mold"
            ),
            symptomsHindi = listOf(
                "फूलों और फलियों पर चिपके लाखों पीले-हरे सूक्ष्म कीड़े",
                "फूलों और नई फलियों का मुड़ना और सूखना",
                "फलियों में दाने न बनना और हल्की कागजी रह जाना",
                "चिपचिपा रस निकलने से फफूंद जमना"
            ),
            organicRemedy = "Spray 5% neem extract (NSKE) or tobacco decoction + liquid soap @ 2ml/L. Install yellow sticky boards.",
            organicRemedyHindi = "5% नीम का काढ़ा या साबुन पानी का घोल 2 मिली/लीटर छिड़कें। पीले चिपचिपे ट्रैप लगाएं।",
            chemicalRemedy = "Dimethoate 30% EC @ 1.5ml/L or Oxydemeton methyl 25% EC @ 1ml/L before 10 AM.",
            chemicalRemedyHindi = "डाइमेथोएट 30% EC 1.5 मिली/लीटर का छिड़काव सुबह 10 बजे से पहले करें।",
            preventiveMeasures = listOf(
                "Early sowing (first fortnight of October) to escape peak aphid flight",
                "Avoid late heavy irrigation during flowering"
            ),
            preventiveMeasuresHindi = listOf(
                "15 अक्टूबर से पहले अगेती बुवाई करें ताकि माहू से बचा जा सके",
                "फूल आते समय खेत में अधिक पानी न लगाएं"
            )
        )
    )

    fun getAllDiseases(): List<PestDiseaseItem> = diseaseCatalog

    fun getSupportedCrops(): List<String> = listOf("Rice", "Wheat", "Tomato", "Potato", "Cotton", "Maize", "Mustard")

    fun getDiseasesForCrop(cropName: String): List<PestDiseaseItem> {
        return diseaseCatalog.filter { it.cropName.equals(cropName, ignoreCase = true) }
    }

    fun getAllSymptomsForCrop(cropName: String): List<String> {
        return getDiseasesForCrop(cropName).flatMap { it.keySymptoms }.distinct()
    }

    fun diagnose(
        cropName: String,
        selectedSymptoms: List<String>,
        photoUri: String? = null
    ): DiagnosticResult? {
        val cropDiseases = getDiseasesForCrop(cropName)
        if (cropDiseases.isEmpty()) return null

        if (selectedSymptoms.isEmpty()) {
            // Default top disease if photo or general query
            val top = cropDiseases.first()
            return DiagnosticResult(
                matchedDisease = top,
                matchedSymptoms = top.keySymptoms.take(2),
                confidencePercentage = if (photoUri != null) 85 else 70,
                photoUri = photoUri
            )
        }

        // Calculate best match
        var bestMatch: PestDiseaseItem = cropDiseases.first()
        var bestScore = -1
        var matchedList = emptyList<String>()

        for (disease in cropDiseases) {
            val intersection = disease.keySymptoms.filter { s ->
                selectedSymptoms.any { selected ->
                    s.contains(selected, ignoreCase = true) || selected.contains(s, ignoreCase = true)
                }
            }
            val score = intersection.size
            if (score > bestScore) {
                bestScore = score
                bestMatch = disease
                matchedList = intersection.ifEmpty { selectedSymptoms.take(2) }
            }
        }

        val confidence = when {
            bestScore >= 3 -> 95
            bestScore == 2 -> 88
            bestScore == 1 -> 78
            else -> 65
        }

        return DiagnosticResult(
            matchedDisease = bestMatch,
            matchedSymptoms = matchedList,
            confidencePercentage = confidence,
            photoUri = photoUri
        )
    }
}
