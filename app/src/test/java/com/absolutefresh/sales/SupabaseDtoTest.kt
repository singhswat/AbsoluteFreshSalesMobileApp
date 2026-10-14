package com.absolutefresh.sales

import com.absolutefresh.sales.data.network.dto.BeatVisitDto
import com.absolutefresh.sales.data.network.dto.FieldOrderDto
import com.absolutefresh.sales.data.network.dto.FieldOrderItemDto
import com.absolutefresh.sales.data.network.dto.HolidayDto
import com.absolutefresh.sales.data.network.dto.OutletDto
import com.absolutefresh.sales.data.network.dto.ProductDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun outletDto_serializesAndDeserializesCorrectly() {
        val dto = OutletDto(
            id = "out-101",
            clientId = "c-uuid-1",
            name = "Gupta Provision Store",
            area = "Hazratganj",
            owner = "Ramesh Gupta",
            phone = "9876543201",
            assignedRep = "rep-1",
            averageKgPerDay = 15.5,
            status = "active",
            priceTier = 3,
            outletType = "general_trade",
            gstin = "09ABCDE1234F1Z5",
            address = "Shop 4, Market Lane",
            latitude = 26.8467,
            longitude = 80.9462,
            syncVersion = 2L
        )

        val serialized = json.encodeToString(dto)
        assertTrue(serialized.contains("\"client_id\":\"c-uuid-1\""))
        assertTrue(serialized.contains("\"price_tier\":3"))
        assertTrue(serialized.contains("\"outlet_type\":\"general_trade\""))
        assertTrue(serialized.contains("\"sync_version\":2"))

        val deserialized = json.decodeFromString<OutletDto>(serialized)
        assertEquals(dto.id, deserialized.id)
        assertEquals(dto.clientId, deserialized.clientId)
        assertEquals(dto.name, deserialized.name)
        assertEquals(dto.priceTier, deserialized.priceTier)
        assertEquals(dto.latitude, deserialized.latitude)
    }

    @Test
    fun beatVisitDto_serializesAndDeserializesCorrectly() {
        val dto = BeatVisitDto(
            id = "visit-1",
            rep = "rep-1",
            date = "2026-10-14",
            outletId = "out-101",
            outletName = "Gupta Provision Store",
            area = "Hazratganj",
            sequence = 1,
            targetKg = 20.0,
            status = "PLANNED",
            linkedOrder = null,
            visitedTime = null,
            missedReason = null
        )

        val serialized = json.encodeToString(dto)
        assertTrue(serialized.contains("\"outlet_id\":\"out-101\""))
        assertTrue(serialized.contains("\"target_kg\":20.0"))

        val deserialized = json.decodeFromString<BeatVisitDto>(serialized)
        assertEquals(dto.date, deserialized.date)
        assertEquals(dto.sequence, deserialized.sequence)
    }

    @Test
    fun fieldOrderDto_serializesAndDeserializesCorrectly() {
        val dto = FieldOrderDto(
            id = "ord-srv-1",
            clientId = "order-uuid-99",
            outletId = "out-101",
            outletName = "Gupta Provision Store",
            repId = "rep-1",
            orderDate = "2026-10-14",
            deliveryDate = "2026-10-15",
            status = "PENDING",
            notes = "Ring bell on delivery",
            invoiceNumber = "AFF-2610-101",
            subtotal = 81.0,
            taxAmount = 4.05,
            totalAmount = 85.05,
            paymentMode = "UPI",
            paymentStatus = "UNPAID",
            syncVersion = 1L
        )

        val serialized = json.encodeToString(dto)
        assertTrue(serialized.contains("\"client_id\":\"order-uuid-99\""))
        assertTrue(serialized.contains("\"invoice_number\":\"AFF-2610-101\""))
        assertTrue(serialized.contains("\"payment_mode\":\"UPI\""))

        val deserialized = json.decodeFromString<FieldOrderDto>(serialized)
        assertEquals(dto.clientId, deserialized.clientId)
        assertEquals(dto.totalAmount, deserialized.totalAmount, 0.001)
    }

    @Test
    fun fieldOrderItemDto_serializesAndDeserializesCorrectly() {
        val itemDto = FieldOrderItemDto(
            id = "item-1",
            orderId = "ord-srv-1",
            productId = "p1",
            skuName = "Fresh Idli Dosa Batter 500g",
            quantity = 2,
            kgOrdered = 1.0,
            unitPrice = 40.5,
            discountAmount = 0.0,
            subtotal = 81.0,
            taxRate = 0.05
        )

        val serialized = json.encodeToString(itemDto)
        val deserialized = json.decodeFromString<FieldOrderItemDto>(serialized)
        assertEquals(itemDto.quantity, deserialized.quantity)
        assertEquals(itemDto.kgOrdered, deserialized.kgOrdered, 0.001)
        assertEquals(itemDto.unitPrice, deserialized.unitPrice, 0.001)
    }

    @Test
    fun productDto_and_holidayDto_serializeCorrectly() {
        val product = ProductDto(
            id = "p1",
            name = "Fresh Idli Dosa Batter 500g",
            unit = "kg",
            basePrice = 45.0,
            mrp = 60.0,
            shelfLife = 10,
            packWeight = "500g",
            code = "BAT-500",
            active = true
        )
        val holiday = HolidayDto(
            id = "h1",
            date = "2026-10-24",
            name = "Dussehra"
        )

        val prodStr = json.encodeToString(product)
        val holStr = json.encodeToString(holiday)

        val decodedProd = json.decodeFromString<ProductDto>(prodStr)
        val decodedHol = json.decodeFromString<HolidayDto>(holStr)

        assertEquals("BAT-500", decodedProd.code)
        assertEquals("Dussehra", decodedHol.name)
    }
}
