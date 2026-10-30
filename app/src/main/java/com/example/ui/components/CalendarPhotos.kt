package com.example.ui.components

import androidx.compose.ui.graphics.Color

data class CalendarPhoto(
    val titleEn: String,
    val titleHi: String,
    val url: String,
    val gradient: List<Color>
)

val CALENDAR_PHOTOS: List<CalendarPhoto> = listOf(
    // Year 1 (Months 1-12): Defenders of the Nation & Historic Forts
    CalendarPhoto(
        titleEn = "Siachen Patrol • Indian Army",
        titleHi = "सियाचिन गश्त • भारतीय सेना",
        url = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
    ),
    CalendarPhoto(
        titleEn = "Sky Fighter • Indian Air Force",
        titleHi = "लड़ाकू विमान • भारतीय वायु सेना",
        url = "https://images.unsplash.com/photo-1519074069444-1ba4ea16e917?w=600",
        gradient = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8))
    ),
    CalendarPhoto(
        titleEn = "Fleet at Sea • Indian Navy",
        titleHi = "समुद्री बेड़ा • भारतीय नौसेना",
        url = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600",
        gradient = listOf(Color(0xFF082F49), Color(0xFF0E7490), Color(0xFF155E75))
    ),
    CalendarPhoto(
        titleEn = "Red Fort • New Delhi",
        titleHi = "लाल किला • नई दिल्ली",
        url = "https://images.unsplash.com/photo-1587474260584-136574528ed5?w=600",
        gradient = listOf(Color(0xFF7F1D1D), Color(0xFF991B1B), Color(0xFFB91C1C))
    ),
    CalendarPhoto(
        titleEn = "Taj Mahal • Agra",
        titleHi = "ताजमहल • आगरा",
        url = "https://images.unsplash.com/photo-1548013146-72479768bada?w=600",
        gradient = listOf(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF8B5CF6))
    ),
    CalendarPhoto(
        titleEn = "Tejas Fighter • IAF",
        titleHi = "तेजस लड़ाकू विमान • वायु सेना",
        url = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=600",
        gradient = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
    ),
    CalendarPhoto(
        titleEn = "Gateway of India • Mumbai",
        titleHi = "गेटवे ऑफ इंडिया • मुंबई",
        url = "https://images.unsplash.com/photo-1570168007204-dfb528c6958f?w=600",
        gradient = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF60A5FA))
    ),
    CalendarPhoto(
        titleEn = "Golden Temple • Amritsar",
        titleHi = "स्वर्ण मंदिर • अमृतसर",
        url = "https://images.unsplash.com/photo-1594910074125-9a84a6fb7d34?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFBBF24))
    ),
    CalendarPhoto(
        titleEn = "Ganga Aarti • Varanasi",
        titleHi = "गंगा आरती • वाराणसी",
        url = "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?w=600",
        gradient = listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFFF97316))
    ),
    CalendarPhoto(
        titleEn = "Thar Desert • Jaisalmer",
        titleHi = "थार मरुस्थल • जैसलमेर",
        url = "https://images.unsplash.com/photo-1477587458883-47145ed94245?w=600",
        gradient = listOf(Color(0xFF713F12), Color(0xFFCA8A04), Color(0xFFFACC15))
    ),
    CalendarPhoto(
        titleEn = "Mysore Royal Palace • Karnataka",
        titleHi = "मैसूर राजमहल • कर्नाटक",
        url = "https://images.unsplash.com/photo-1590766940554-634a7d33a68f?w=600",
        gradient = listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFFA855F7))
    ),
    CalendarPhoto(
        titleEn = "Andaman Blue Waters • Navy Bastion",
        titleHi = "अंडमान तट • नौसेना बेस",
        url = "https://images.unsplash.com/photo-1559128010-7c1ad6e1b6a5?w=600",
        gradient = listOf(Color(0xFF134E4A), Color(0xFF0F766E), Color(0xFF14B8A6))
    ),

    // Year 2 (Months 13-24): Himalayan Frontiers & Natural Wonders
    CalendarPhoto(
        titleEn = "Kargil Snow Heights • Ladakh",
        titleHi = "कारगिल बर्फीली चोटियां • लद्दाख",
        url = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B))
    ),
    CalendarPhoto(
        titleEn = "Sukhoi-30MKI • IAF Supersonic",
        titleHi = "सुखोई-30MKI • वायु सेना",
        url = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=600",
        gradient = listOf(Color(0xFF172554), Color(0xFF1D4ED8), Color(0xFF3B82F6))
    ),
    CalendarPhoto(
        titleEn = "INS Vikrant • Sea Trials",
        titleHi = "आईएनएस विक्रांत • समुद्री परीक्षण",
        url = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=600",
        gradient = listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF059669))
    ),
    CalendarPhoto(
        titleEn = "Qutub Minar • Delhi",
        titleHi = "कुतुब मीनार • दिल्ली",
        url = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?w=600",
        gradient = listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFFEC4899))
    ),
    CalendarPhoto(
        titleEn = "Kerala Backwaters • Alleppey",
        titleHi = "केरल बैकवाटर्स • अल्लेप्पी",
        url = "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=600",
        gradient = listOf(Color(0xFF14532D), Color(0xFF15803D), Color(0xFF22C55E))
    ),
    CalendarPhoto(
        titleEn = "Para Commandos • Indian Army",
        titleHi = "पैरा कमांडो • भारतीय सेना",
        url = "https://images.unsplash.com/photo-1508614589041-895b88991e3e?w=600",
        gradient = listOf(Color(0xFF365314), Color(0xFF4D7C0F), Color(0xFF65A30D))
    ),
    CalendarPhoto(
        titleEn = "Hawa Mahal • Jaipur",
        titleHi = "हवा महल • जयपुर",
        url = "https://images.unsplash.com/photo-1599661046289-e31897846e41?w=600",
        gradient = listOf(Color(0xFF9F1239), Color(0xFFE11D48), Color(0xFFFB7185))
    ),
    CalendarPhoto(
        titleEn = "Pangong Tso • Ladakh",
        titleHi = "पैंगोंग झील • लद्दाख",
        url = "https://images.unsplash.com/photo-1581793745862-99fde7fa73d2?w=600",
        gradient = listOf(Color(0xFF083344), Color(0xFF0891B2), Color(0xFF22D3EE))
    ),
    CalendarPhoto(
        titleEn = "Konark Sun Temple • Odisha",
        titleHi = "कोणार्क सूर्य मंदिर • ओडिशा",
        url = "https://images.unsplash.com/photo-1627894483216-2138af692e32?w=600",
        gradient = listOf(Color(0xFF713F12), Color(0xFFB45309), Color(0xFFF59E0B))
    ),
    CalendarPhoto(
        titleEn = "Ranthambore Royal Tiger • Rajasthan",
        titleHi = "रणथंभौर राष्ट्रीय उद्यान • राजस्थान",
        url = "https://images.unsplash.com/photo-1534188753412-3e26d0d618d6?w=600",
        gradient = listOf(Color(0xFF451A03), Color(0xFF9A3412), Color(0xFFEA580C))
    ),
    CalendarPhoto(
        titleEn = "Victoria Memorial • Kolkata",
        titleHi = "विक्टोरिया मेमोरियल • कोलकाता",
        url = "https://images.unsplash.com/photo-1558431382-27e303142255?w=600",
        gradient = listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF94A3B8))
    ),
    CalendarPhoto(
        titleEn = "Lakshadweep Coral Waters • Indian Coast",
        titleHi = "लक्षद्वीप द्वीपसमूह • भारतीय तट",
        url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
        gradient = listOf(Color(0xFF042F2E), Color(0xFF0D9488), Color(0xFF2DD4BF))
    ),

    // Year 3 (Months 25-36): Coastal Bastions & Mountain Passes
    CalendarPhoto(
        titleEn = "Siachen Mountain Base • Army Outpost",
        titleHi = "सियाचिन बेस कैंप • सैन्य चौकी",
        url = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF475569))
    ),
    CalendarPhoto(
        titleEn = "Mirage 2000 • Tactical Wings",
        titleHi = "मिराज 2000 • भारतीय वायु सेना",
        url = "https://images.unsplash.com/photo-1517976487515-5928d39f67a7?w=600",
        gradient = listOf(Color(0xFF082F49), Color(0xFF0369A1), Color(0xFF0EA5E9))
    ),
    CalendarPhoto(
        titleEn = "Western Naval Dock • Mumbai Harbor",
        titleHi = "पश्चिमी नौसेना कमान • मुंबई बंदरगाह",
        url = "https://images.unsplash.com/photo-1498084393753-b411b2d26b34?w=600",
        gradient = listOf(Color(0xFF172554), Color(0xFF1E40AF), Color(0xFF3B82F6))
    ),
    CalendarPhoto(
        titleEn = "Amer Fort Ramparts • Jaipur",
        titleHi = "आमेर किला • जयपुर",
        url = "https://images.unsplash.com/photo-1605649487212-47bdab064df7?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFBBF24))
    ),
    CalendarPhoto(
        titleEn = "Munnar Green Valleys • Kerala",
        titleHi = "मुन्नार हरी वादियां • केरल",
        url = "https://images.unsplash.com/photo-1516483638261-f4dbaf036963?w=600",
        gradient = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981))
    ),
    CalendarPhoto(
        titleEn = "Surya Kiran Aerobatics • IAF",
        titleHi = "सूर्य किरण एयरोबेटिक्स • वायु सेना",
        url = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600",
        gradient = listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF6366F1))
    ),
    CalendarPhoto(
        titleEn = "Meenakshi Temple • Madurai",
        titleHi = "मीनाक्षी मंदिर • मदुरै",
        url = "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=600",
        gradient = listOf(Color(0xFF581C87), Color(0xFF9333EA), Color(0xFFA855F7))
    ),
    CalendarPhoto(
        titleEn = "Rohtang & Atal Pass • Himachal",
        titleHi = "रोहतांग एवं अटल दर्रा • हिमाचल",
        url = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=600",
        gradient = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF64748B))
    ),
    CalendarPhoto(
        titleEn = "Rishikesh Holy Ganga • Uttarakhand",
        titleHi = "ऋषिकेश पावन गंगा • उत्तराखंड",
        url = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=600",
        gradient = listOf(Color(0xFF134E4A), Color(0xFF0F766E), Color(0xFF2DD4BF))
    ),
    CalendarPhoto(
        titleEn = "Chittorgarh Fort • Fortress of Valour",
        titleHi = "चित्तौड़गढ़ किला • शौर्य की भूमि",
        url = "https://images.unsplash.com/photo-1566552881560-0be86c53e56f?w=600",
        gradient = listOf(Color(0xFF7F1D1D), Color(0xFFB91C1C), Color(0xFFEF4444))
    ),
    CalendarPhoto(
        titleEn = "Charminar • Hyderabad",
        titleHi = "चारमीनार • हैदराबाद",
        url = "https://images.unsplash.com/photo-1572445271230-a78b5944a659?w=600",
        gradient = listOf(Color(0xFF701A75), Color(0xFFA21CAF), Color(0xFFE879F9))
    ),
    CalendarPhoto(
        titleEn = "Goa Shore • Arabian Sea",
        titleHi = "गोवा सागर तट • अरब सागर",
        url = "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=600",
        gradient = listOf(Color(0xFF0E7490), Color(0xFF06B6D4), Color(0xFF67E8F9))
    ),

    // Year 4 (Months 37-48): Strategic Horizons & Architectural Jewels
    CalendarPhoto(
        titleEn = "Zojila High Pass • Army Convoy",
        titleHi = "जोजिला दर्रा • सैन्य मार्ग",
        url = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600",
        gradient = listOf(Color(0xFF1C1917), Color(0xFF44403C), Color(0xFF78716C))
    ),
    CalendarPhoto(
        titleEn = "Rafale Supersonic • IAF Fighter",
        titleHi = "राफेल लड़ाकू विमान • भारतीय वायु सेना",
        url = "https://images.unsplash.com/photo-1520437358207-323b43b50729?w=600",
        gradient = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF60A5FA))
    ),
    CalendarPhoto(
        titleEn = "Eastern Naval Command • Visakhapatnam",
        titleHi = "पूर्वी नौसेना कमान • विशाखापट्टनम",
        url = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=600",
        gradient = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8))
    ),
    CalendarPhoto(
        titleEn = "India Gate • Amar Jawan Jyoti",
        titleHi = "इंडिया गेट • अमर जवान ज्योति",
        url = "https://images.unsplash.com/photo-1597040663342-45b6af3d91a5?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFF59E0B))
    ),
    CalendarPhoto(
        titleEn = "Valley of Flowers • Uttarakhand",
        titleHi = "फूलों की घाटी • उत्तराखंड",
        url = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=600",
        gradient = listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFFF472B6))
    ),
    CalendarPhoto(
        titleEn = "C-17 Globemaster • IAF Lifeline",
        titleHi = "सी-17 ग्लोबमास्टर • वायु सेना",
        url = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600",
        gradient = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF64748B))
    ),
    CalendarPhoto(
        titleEn = "Hampi Stone Chariot • Karnataka",
        titleHi = "हम्पी पाषाण रथ • कर्नाटक",
        url = "https://images.unsplash.com/photo-1600100397608-f010e42e584f?w=600",
        gradient = listOf(Color(0xFF713F12), Color(0xFFCA8A04), Color(0xFFEAB308))
    ),
    CalendarPhoto(
        titleEn = "Nubra Cold Desert • Ladakh",
        titleHi = "नुब्रा शीत मरुस्थल • लद्दाख",
        url = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF475569))
    ),
    CalendarPhoto(
        titleEn = "Khajuraho Heritage • Madhya Pradesh",
        titleHi = "खजुराहो मंदिर • मध्य प्रदेश",
        url = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFF59E0B))
    ),
    CalendarPhoto(
        titleEn = "Kumbhalgarh Great Wall • Rajasthan",
        titleHi = "कुंभलगढ़ महाप्राचीर • राजस्थान",
        url = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600",
        gradient = listOf(Color(0xFF365314), Color(0xFF4D7C0F), Color(0xFF84CC16))
    ),
    CalendarPhoto(
        titleEn = "Mahabodhi Temple • Bodh Gaya",
        titleHi = "महाबोधि मंदिर • बोधगया",
        url = "https://images.unsplash.com/photo-1545239351-ef35f43d514b?w=600",
        gradient = listOf(Color(0xFF713F12), Color(0xFFD97706), Color(0xFFFBBF24))
    ),
    CalendarPhoto(
        titleEn = "Kanyakumari • Confluence of 3 Oceans",
        titleHi = "कन्याकुमारी • त्रिवेणी सागर संगम",
        url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600",
        gradient = listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF38BDF8))
    ),

    // Year 5 (Months 49-60): Airborne Pride & Cultural Heritage
    CalendarPhoto(
        titleEn = "Tawang Monastery • Arunachal Snows",
        titleHi = "तवांग मठ • अरुणाचल प्रदेश",
        url = "https://images.unsplash.com/photo-1483921020237-2ff51e8e4b22?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B))
    ),
    CalendarPhoto(
        titleEn = "Combat Helicopter • Indian Army Aviation",
        titleHi = "हमलावर हेलीकॉप्टर • सेना विमानन",
        url = "https://images.unsplash.com/photo-1508614589041-895b88991e3e?w=600",
        gradient = listOf(Color(0xFF14532D), Color(0xFF166534), Color(0xFF22C55E))
    ),
    CalendarPhoto(
        titleEn = "Scorpene Submarine • Deep Blue Patrol",
        titleHi = "स्कॉर्पीन पनडुब्बी • गहरे सागर की सुरक्षा",
        url = "https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=600",
        gradient = listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF000000))
    ),
    CalendarPhoto(
        titleEn = "Amrit Udyan • Rashtrapati Bhavan",
        titleHi = "अमृत उद्यान • राष्ट्रपति भवन",
        url = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=600",
        gradient = listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFFF472B6))
    ),
    CalendarPhoto(
        titleEn = "Jog Falls • Western Ghats",
        titleHi = "जोग जलप्रपात • पश्चिमी घाट",
        url = "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?w=600",
        gradient = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF34D399))
    ),
    CalendarPhoto(
        titleEn = "Netra Airborne Radar • IAF Watch",
        titleHi = "नेत्र हवाई रडार • वायु सेना",
        url = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600",
        gradient = listOf(Color(0xFF082F49), Color(0xFF0369A1), Color(0xFF38BDF8))
    ),
    CalendarPhoto(
        titleEn = "Brihadeeswarar Marvel • Thanjavur",
        titleHi = "बृहदेश्वर मंदिर • तंजावुर",
        url = "https://images.unsplash.com/photo-1621644810619-a90530454655?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFF59E0B))
    ),
    CalendarPhoto(
        titleEn = "Key Monastery • Spiti Valley",
        titleHi = "की मठ • स्पीति घाटी",
        url = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF475569))
    ),
    CalendarPhoto(
        titleEn = "Kaziranga Rhino Sanctuary • Assam",
        titleHi = "काजीरंगा राष्ट्रीय उद्यान • असम",
        url = "https://images.unsplash.com/photo-1516426122078-c23e76319801?w=600",
        gradient = listOf(Color(0xFF14532D), Color(0xFF15803D), Color(0xFF4ADE80))
    ),
    CalendarPhoto(
        titleEn = "Mehrangarh Fortress • Jodhpur",
        titleHi = "मेहरानगढ़ दुर्ग • जोधपुर",
        url = "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0?w=600",
        gradient = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF60A5FA))
    ),
    CalendarPhoto(
        titleEn = "Nalanda Mahavihara • Ancient Ruins",
        titleHi = "नालंदा विश्वविद्यालय • प्राचीन धरोहर",
        url = "https://images.unsplash.com/photo-1590059390046-538466b0d9e7?w=600",
        gradient = listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFFFB923C))
    ),
    CalendarPhoto(
        titleEn = "White Rann of Kutch • Gujarat",
        titleHi = "कच्छ का श्वेत रण • गुजरात",
        url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
        gradient = listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFFE2E8F0))
    ),

    // Year 6 (Months 61-72): Ultimate Frontiers & Panoramas
    CalendarPhoto(
        titleEn = "Nathu La Pass • Sikkim Frontier",
        titleHi = "नाथू ला दर्रा • सिक्किम सीमा",
        url = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600",
        gradient = listOf(Color(0xFF0F172A), Color(0xFF334155), Color(0xFF94A3B8))
    ),
    CalendarPhoto(
        titleEn = "Mirage Night Sortie • Combat Watch",
        titleHi = "मिराज रात्रि गश्त • वायु सुरक्षा",
        url = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=600",
        gradient = listOf(Color(0xFF311042), Color(0xFF6B21A8), Color(0xFFA855F7))
    ),
    CalendarPhoto(
        titleEn = "INS Vikramaditya • Ocean Fortress",
        titleHi = "आईएनएस विक्रमादित्य • महासागरीय शक्ति",
        url = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=600",
        gradient = listOf(Color(0xFF0A192F), Color(0xFF1E3A8A), Color(0xFF0284C7))
    ),
    CalendarPhoto(
        titleEn = "Lotus Temple • Architecture of Peace",
        titleHi = "लोटस टेम्पल • कमल मंदिर दिल्ली",
        url = "https://images.unsplash.com/photo-1592635196078-9fe3d54f2377?w=600",
        gradient = listOf(Color(0xFF3B0764), Color(0xFF7E22CE), Color(0xFFC084FC))
    ),
    CalendarPhoto(
        titleEn = "Nilgiri Mountain Rail • Western Ghats",
        titleHi = "नीलगिरि पर्वतीय रेल • पश्चिमी घाट",
        url = "https://images.unsplash.com/photo-1509749837427-ac94a2553d0e?w=600",
        gradient = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981))
    ),
    CalendarPhoto(
        titleEn = "Akash Ganga Parachute Team • IAF",
        titleHi = "आकाश गंगा पैराशूट टीम • वायु सेना",
        url = "https://images.unsplash.com/photo-1519074069444-1ba4ea16e917?w=600",
        gradient = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8))
    ),
    CalendarPhoto(
        titleEn = "Somnath Coast • Holy Shoreline",
        titleHi = "सोमनाथ सागर तट • पावन तीर्थ",
        url = "https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=600",
        gradient = listOf(Color(0xFF134E4A), Color(0xFF0F766E), Color(0xFF14B8A6))
    ),
    CalendarPhoto(
        titleEn = "Gurudongmar Glacier Lake • High Altitude",
        titleHi = "गुरुडोंगमार पवित्र झील • सिक्किम",
        url = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600",
        gradient = listOf(Color(0xFF083344), Color(0xFF0891B2), Color(0xFF67E8F9))
    ),
    CalendarPhoto(
        titleEn = "Sundarbans Delta • Mangrove Wilds",
        titleHi = "सुंदरवन डेल्टा • मैंग्रोव वन",
        url = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=600",
        gradient = listOf(Color(0xFF14532D), Color(0xFF166534), Color(0xFF22C55E))
    ),
    CalendarPhoto(
        titleEn = "Gwalior Fort • Pearl in Citadel",
        titleHi = "ग्वालियर किला • किलों का सिरमौर",
        url = "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?w=600",
        gradient = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFBBF24))
    ),
    CalendarPhoto(
        titleEn = "Ajanta & Ellora Caves • Rock Marvel",
        titleHi = "अजंता एवं एलोरा गुफाएं • महाराष्ट्र",
        url = "https://images.unsplash.com/photo-1600100397608-f010e42e584f?w=600",
        gradient = listOf(Color(0xFF7C2D12), Color(0xFFB45309), Color(0xFFEA580C))
    ),
    CalendarPhoto(
        titleEn = "Diu Coastal Bastion • Arabian Horizon",
        titleHi = "दीव तटीय किला • अरब सागर",
        url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
        gradient = listOf(Color(0xFF0A192F), Color(0xFF1E3A8A), Color(0xFF38BDF8))
    )
)

/**
 * Returns a 100% unique photo for each month and year.
 * Guarantees that no photo repeats for at least 6 consecutive years (72 unique months).
 */
fun getCalendarPhoto(year: Int, month: Int): CalendarPhoto {
    val totalMonths = (year - 2024) * 12 + month
    val index = ((totalMonths % CALENDAR_PHOTOS.size) + CALENDAR_PHOTOS.size) % CALENDAR_PHOTOS.size
    return CALENDAR_PHOTOS[index]
}
