package com.absolutefresh.sales.data.model

import java.util.UUID

enum class HolidayScope(val labelEn: String, val labelHi: String) {
    PAN_INDIA("Pan-India", "राष्ट्रीय"),
    REGIONAL("Regional (UP)", "क्षेत्रीय (उ.प्र.)"),
    MARKET_CLOSURE("Market Closure", "बाजार बंद")
}

data class BeatImpact(
    val beatDay: String,
    val beatName: String,
    val affectedRoutes: List<String>,
    val advisoryNotice: String
)

data class Holiday(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val date: String,             // Format: YYYY-MM-DD (e.g. "2026-10-20")
    val dayOfWeek: String,        // e.g. "Tuesday"
    val monthYear: String,        // e.g. "Oct 2026"
    val scope: HolidayScope,
    val description: String,
    val beatImpact: BeatImpact? = null,
    val isPast: Boolean = false
)

data class PreHolidayStockRequest(
    val id: String = UUID.randomUUID().toString(),
    val holidayId: String,
    val holidayName: String,
    val outletName: String,
    val advancePackets: Int,
    val justification: String,
    val submittedAtEpochMillis: Long = System.currentTimeMillis()
)
