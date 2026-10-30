package com.example.model

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val category: String = "Food",
    val note: String = "",
    val paymentMethod: String = "Cash",
    val dateIso: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val person: String = "",
    val given: Double = 0.0,
    val returned: Double = 0.0,
    val remaining: Double = 0.0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class UserProfile(
    val username: String,
    val password: String,
    val securityQuestion: String = "First School Name?",
    val securityAnswer: String = "",
    val mobile: String = "",
    val countryCode: String = "+91",
    val isVerified: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

data class AppConfig(
    val currency: String = "₹",
    val monthlyBudget: Double = 50000.0,
    val language: String = "en", // "en" or "hi"
    val summaryType: String = "monthly", // "monthly", "total", "yearly"
    val isDarkMode: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val updateServerUrl: String = "https://raw.githubusercontent.com/shivashandliya1457-ux/version.json/main/version.json",
    val autoCheckUpdates: Boolean = true
)

data class FeedbackItem(
    val id: Long = System.currentTimeMillis(),
    val plantName: String,
    val rating: Int = 5,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String
)

val PLANT_NAMES = listOf(
    "Neem 🌱", "Tulsi 🌿", "Peepal 🌳", "Banyan 🍃", "Gulmohar 🌺",
    "Lotus 🪷", "Aloe Vera 🪴", "Rosewood 🌲", "Sandalwood 🪵", "Bamboo 🎋",
    "Ashoka 🌴", "Marigold 🌼", "Mogra 🌸", "Amla 🍃", "Champa 🌻"
)

val WORLD_CURRENCIES = listOf(
    CurrencyItem("INR", "₹", "Indian Rupee", "India"),
    CurrencyItem("USD", "$", "US Dollar", "United States"),
    CurrencyItem("EUR", "€", "Euro", "European Union"),
    CurrencyItem("GBP", "£", "British Pound", "United Kingdom"),
    CurrencyItem("AED", "AED", "UAE Dirham", "United Arab Emirates"),
    CurrencyItem("SAR", "SAR", "Saudi Riyal", "Saudi Arabia"),
    CurrencyItem("CAD", "C$", "Canadian Dollar", "Canada"),
    CurrencyItem("AUD", "A$", "Australian Dollar", "Australia"),
    CurrencyItem("JPY", "¥", "Japanese Yen", "Japan"),
    CurrencyItem("CNY", "¥", "Chinese Yuan", "China"),
    CurrencyItem("SGD", "S$", "Singapore Dollar", "Singapore"),
    CurrencyItem("CHF", "CHF", "Swiss Franc", "Switzerland"),
    CurrencyItem("KWD", "KD", "Kuwaiti Dinar", "Kuwait"),
    CurrencyItem("QAR", "QR", "Qatari Riyal", "Qatar"),
    CurrencyItem("OMR", "OMR", "Omani Rial", "Oman"),
    CurrencyItem("BHD", "BD", "Bahraini Dinar", "Bahrain"),
    CurrencyItem("NZD", "NZ$", "New Zealand Dollar", "New Zealand"),
    CurrencyItem("RUB", "₽", "Russian Ruble", "Russia"),
    CurrencyItem("ZAR", "R", "South African Rand", "South Africa"),
    CurrencyItem("BRL", "R$", "Brazilian Real", "Brazil"),
    CurrencyItem("KRW", "₩", "South Korean Won", "South Korea"),
    CurrencyItem("THB", "฿", "Thai Baht", "Thailand"),
    CurrencyItem("MYR", "RM", "Malaysian Ringgit", "Malaysia"),
    CurrencyItem("IDR", "Rp", "Indonesian Rupiah", "Indonesia"),
    CurrencyItem("TRY", "₺", "Turkish Lira", "Turkey"),
    CurrencyItem("PKR", "Rs", "Pakistani Rupee", "Pakistan"),
    CurrencyItem("BDT", "৳", "Bangladeshi Taka", "Bangladesh"),
    CurrencyItem("NPR", "रू", "Nepalese Rupee", "Nepal"),
    CurrencyItem("LKR", "Rs", "Sri Lankan Rupee", "Sri Lanka")
)
