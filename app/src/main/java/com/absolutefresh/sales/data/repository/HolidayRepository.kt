package com.absolutefresh.sales.data.repository

import com.absolutefresh.sales.data.model.BeatImpact
import com.absolutefresh.sales.data.model.Holiday
import com.absolutefresh.sales.data.model.HolidayScope
import com.absolutefresh.sales.data.model.PreHolidayStockRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HolidayRepository {
    fun getHolidays(): Flow<List<Holiday>>
    fun getLastCacheTime(): Long
    suspend fun submitPreHolidayAdvance(request: PreHolidayStockRequest)
    fun getSubmittedRequests(): List<PreHolidayStockRequest>
}

class FakeHolidayRepository : HolidayRepository {

    private val lastCacheTime = System.currentTimeMillis() - (2 * 3600 * 1000) // 2 hours ago

    private val submittedRequests = mutableListOf<PreHolidayStockRequest>()

    private val holidaysFlow = MutableStateFlow(
        listOf(
            Holiday(
                id = "hol-2026-10-20",
                nameEn = "Dussehra / Vijayadashami",
                nameHi = "दशहरा / विजयादशमी",
                date = "2026-10-20",
                dayOfWeek = "Tuesday",
                monthYear = "Oct 2026",
                scope = HolidayScope.PAN_INDIA,
                description = "Victory of good over evil. High demand for fresh idli-dosa batter for morning festivities.",
                beatImpact = BeatImpact(
                    beatDay = "Tuesday",
                    beatName = "Tuesday Beat — Gomti Nagar",
                    affectedRoutes = listOf("Vipin Khand", "Patrakarpuram", "Manoj Pandey Crossing"),
                    advisoryNotice = "Vans dispatches moved to 05:00 AM early slot to complete retail deliveries prior to Ramlila processions."
                )
            ),
            Holiday(
                id = "hol-2026-10-24",
                nameEn = "Maharishi Valmiki Jayanti",
                nameHi = "महर्षि वाल्मीकि जयंती",
                date = "2026-10-24",
                dayOfWeek = "Saturday",
                monthYear = "Oct 2026",
                scope = HolidayScope.REGIONAL,
                description = "Uttar Pradesh state holiday honoring the sage Valmiki.",
                beatImpact = null
            ),
            Holiday(
                id = "hol-2026-11-08",
                nameEn = "Diwali / Deepawali Festival",
                nameHi = "दीपावली महापर्व",
                date = "2026-11-08",
                dayOfWeek = "Sunday",
                monthYear = "Nov 2026",
                scope = HolidayScope.PAN_INDIA,
                description = "Festival of Lights. Complete field sales dispatch closure on Sunday evening.",
                beatImpact = BeatImpact(
                    beatDay = "Sunday",
                    beatName = "Weekend Quick Supply",
                    affectedRoutes = listOf("All City Sectors", "Indiranagar", "Aliganj", "Hazratganj"),
                    advisoryNotice = "Warehouse dispatches halt at 12:00 Noon. All outlets must receive 2x buffer stock on Friday & Saturday."
                )
            ),
            Holiday(
                id = "hol-2026-11-09",
                nameEn = "Govardhan Puja / Annakut",
                nameHi = "गोवर्धन पूजा / अन्नकूट",
                date = "2026-11-09",
                dayOfWeek = "Monday",
                monthYear = "Nov 2026",
                scope = HolidayScope.REGIONAL,
                description = "Post-Diwali ritual day across Northern India.",
                beatImpact = BeatImpact(
                    beatDay = "Monday",
                    beatName = "Monday Beat — Aliganj & Kapoorthala",
                    affectedRoutes = listOf("Sector Q", "Kapoorthala Market", "Dandiya Bazar"),
                    advisoryNotice = "Shops open late after 11:30 AM. Beat schedule shifted by +2 hours."
                )
            ),
            Holiday(
                id = "hol-2026-11-10",
                nameEn = "Bhai Dooj",
                nameHi = "भाई दूज",
                date = "2026-11-10",
                dayOfWeek = "Tuesday",
                monthYear = "Nov 2026",
                scope = HolidayScope.REGIONAL,
                description = "Regional celebration of sibling bond.",
                beatImpact = null
            ),
            Holiday(
                id = "hol-2026-11-25",
                nameEn = "Guru Nanak Jayanti (Gurpurab)",
                nameHi = "गुरु नानक जयंती (गुरुपर्व)",
                date = "2026-11-25",
                dayOfWeek = "Wednesday",
                monthYear = "Nov 2026",
                scope = HolidayScope.PAN_INDIA,
                description = "Prakash Parv celebration. High footfall around Naka Hindola and Yahiyaganj gurudwaras.",
                beatImpact = BeatImpact(
                    beatDay = "Wednesday",
                    beatName = "Wednesday Beat — Old City & Charbagh",
                    affectedRoutes = listOf("Naka Hindola", "Gautam Buddha Marg", "Aminabad"),
                    advisoryNotice = "Traffic diversions expected near Gurudwara routes. Delivery drivers instructed to park at designated buffer points."
                )
            ),
            Holiday(
                id = "hol-2026-12-25",
                nameEn = "Christmas Day",
                nameHi = "बड़ा दिन / क्रिसमस",
                date = "2026-12-25",
                dayOfWeek = "Friday",
                monthYear = "Dec 2026",
                scope = HolidayScope.PAN_INDIA,
                description = "National celebration. Commercial zones and bakeries experience surge orders.",
                beatImpact = null
            ),
            Holiday(
                id = "hol-2027-01-01",
                nameEn = "New Year's Day",
                nameHi = "नव वर्ष दिवस",
                date = "2027-01-01",
                dayOfWeek = "Friday",
                monthYear = "Jan 2027",
                scope = HolidayScope.MARKET_CLOSURE,
                description = "Traditional morning market slowdown with evening grocery peak.",
                beatImpact = BeatImpact(
                    beatDay = "Friday",
                    beatName = "Friday Beat — Chowk & Aminabad",
                    affectedRoutes = listOf("Chowk Main", "Gol Darwaza", "Khun Khun Ji Road"),
                    advisoryNotice = "Wholesale mandi closed until 02:00 PM. Retail dispatches rescheduled to afternoon window."
                )
            ),
            Holiday(
                id = "hol-2027-01-26",
                nameEn = "Republic Day (Gantantra Diwas)",
                nameHi = "गणतंत्र दिवस",
                date = "2027-01-26",
                dayOfWeek = "Tuesday",
                monthYear = "Jan 2027",
                scope = HolidayScope.PAN_INDIA,
                description = "National holiday celebrating the Indian Constitution. Official dry day.",
                beatImpact = BeatImpact(
                    beatDay = "Tuesday",
                    beatName = "Tuesday Beat — Hazratganj & Vidhan Sabha Marg",
                    affectedRoutes = listOf("Vidhan Sabha Marg", "Hazratganj Main", "Raj Bhavan Area"),
                    advisoryNotice = "Full security barricades around Vidhan Sabha. Route rescheduled to Wednesday."
                )
            )
        )
    )

    override fun getHolidays(): Flow<List<Holiday>> = holidaysFlow.asStateFlow()

    override fun getLastCacheTime(): Long = lastCacheTime

    override suspend fun submitPreHolidayAdvance(request: PreHolidayStockRequest) {
        submittedRequests.add(request)
    }

    override fun getSubmittedRequests(): List<PreHolidayStockRequest> = submittedRequests.toList()
}
