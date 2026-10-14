package com.absolutefresh.sales.ui.invoice

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Generates official GST-compliant PDF Tax Invoices on device and handles
 * export to local storage and direct 1-tap WhatsApp sharing.
 */
object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    /**
     * Generates a PDF file in app cache and returns the File handle.
     */
    fun generatePdf(context: Context, invoice: InvoiceUiState.Success): File {
        val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val sanitizedNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val pdfFile = File(invoicesDir, "Invoice_$sanitizedNumber.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoice(canvas, invoice)

        document.finishPage(page)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Exports the PDF invoice to device external documents/downloads folder.
     */
    fun exportPdfToStorage(context: Context, invoice: InvoiceUiState.Success): File {
        val cachePdf = generatePdf(context, invoice)
        val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir

        val sanitizedNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val exportFile = File(targetDir, "AFF_Tax_Invoice_$sanitizedNumber.pdf")

        cachePdf.copyTo(exportFile, overwrite = true)
        return exportFile
    }

    /**
     * Directly launches WhatsApp with the generated PDF attached and formatted receipt text.
     */
    fun shareViaWhatsApp(
        context: Context,
        invoice: InvoiceUiState.Success
    ) {
        val pdfFile = generatePdf(context, invoice)
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        val packageManager = context.packageManager

        var targetPackage: String? = null
        for (pkg in whatsAppPackages) {
            try {
                packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                targetPackage = pkg
                break
            } catch (_: PackageManager.NameNotFoundException) {
                // Not installed, check next
            }
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, invoice.shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice ${invoice.invoiceNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }

        try {
            if (targetPackage != null) {
                context.startActivity(shareIntent)
            } else {
                val chooser = Intent.createChooser(shareIntent, "Share Invoice via")
                context.startActivity(chooser)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp not available. Opening share menu...", Toast.LENGTH_SHORT).show()
            shareIntent.setPackage(null)
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share invoice: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the generated PDF in the device's default PDF reader.
     */
    fun openPdfViewer(context: Context, invoice: InvoiceUiState.Success) {
        try {
            val exportedFile = exportPdfToStorage(context, invoice)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportedFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(viewIntent, "Open Tax Invoice"))
            Toast.makeText(context, "Invoice exported to ${exportedFile.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Performs standard 2D vector drawing of the GST-compliant Tax Invoice on the Canvas.
     */
    private fun drawInvoice(canvas: Canvas, invoice: InvoiceUiState.Success) {
        val paint = Paint().apply { isAntiAlias = true }

        // --- Brand Header Banner (Green) ---
        paint.color = Color.rgb(15, 76, 37) // #0F4C25
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, paint)

        // Company Name
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("ABSOLUTE FRESH FOOD PVT. LTD.", 24f, 28f, paint)

        // Statutory & Tagline
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(220, 245, 225)
        canvas.drawText("Pure Fermented Idli & Dosa Batter • FSSAI Lic. No: 22726747000407 • GSTIN: 09TULPS3880J1ZB", 24f, 44f, paint)
        canvas.drawText("Gomti Nagar, Lucknow, Uttar Pradesh - 226010 • WhatsApp/Help: +91 83187 67689", 24f, 58f, paint)

        // --- Status Banner ---
        val bannerTop = 82f
        val bannerHeight = 26f
        val bannerRect = Rect(24, bannerTop.toInt(), PAGE_WIDTH - 24, (bannerTop + bannerHeight).toInt())

        if (invoice.isProvisional) {
            paint.color = Color.rgb(255, 248, 225) // Light amber
            canvas.drawRect(bannerRect, paint)
            paint.color = Color.rgb(230, 81, 0) // Amber border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(bannerRect, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(183, 28, 28)
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            canvas.drawText("PROVISIONAL TAX INVOICE (अस्थायी चालान) — PENDING SERVER ISSUANCE", 36f, bannerTop + 17f, paint)
        } else {
            paint.color = Color.rgb(232, 245, 233) // Light green
            canvas.drawRect(bannerRect, paint)
            paint.color = Color.rgb(76, 175, 80)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(bannerRect, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(27, 94, 32)
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            canvas.drawText("TAX INVOICE (कर चालान) — GST COMPLIANT OFFICIAL BILL", 36f, bannerTop + 17f, paint)
        }

        // --- Metadata Two-Column Grid ---
        var currentY = 126f

        // Column Left: Bill To
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("BILLED TO / BUYER DETAILS", 24f, currentY, paint)

        // Column Right: Invoice Meta
        canvas.drawText("INVOICE PARTICULARS", 340f, currentY, paint)

        currentY += 14f

        // Buyer details
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText(invoice.outletName, 24f, currentY, paint)

        // Invoice No
        paint.color = Color.rgb(15, 76, 37)
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", 340f, currentY, paint)

        currentY += 13f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 8.5f
        canvas.drawText("Contact: ${invoice.outletOwner} (${invoice.outletPhone})", 24f, currentY, paint)
        canvas.drawText("Date: ${invoice.invoiceDateFormatted}", 340f, currentY, paint)

        currentY += 13f
        canvas.drawText("Area: ${invoice.outletAddress}, ${invoice.outletArea}", 24f, currentY, paint)
        canvas.drawText("State of Supply: 09 - Uttar Pradesh", 340f, currentY, paint)

        currentY += 13f
        val gstinText = invoice.outletGstin ?: "Unregistered (General Trade)"
        canvas.drawText("Buyer GSTIN: $gstinText", 24f, currentY, paint)
        canvas.drawText("Payment Mode: ${invoice.paymentStatusText}", 340f, currentY, paint)

        currentY += 13f
        canvas.drawText("Pricing Tier: Tier ${invoice.priceTier} (Wholesale)", 24f, currentY, paint)
        canvas.drawText("Due Date: Immediate on Delivery", 340f, currentY, paint)

        currentY += 18f

        // --- Itemized Products Table ---
        val tableTop = currentY
        val tableHeight = 22f

        // Table Header Bar
        paint.color = Color.rgb(241, 245, 249) // Slate 100
        paint.style = Paint.Style.FILL
        canvas.drawRect(24f, tableTop, (PAGE_WIDTH - 24).toFloat(), tableTop + tableHeight, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8f
        paint.isFakeBoldText = true

        canvas.drawText("#", 30f, tableTop + 14f, paint)
        canvas.drawText("Item Description", 52f, tableTop + 14f, paint)
        canvas.drawText("HSN", 215f, tableTop + 14f, paint)
        canvas.drawText("Qty", 265f, tableTop + 14f, paint)
        canvas.drawText("Weight", 305f, tableTop + 14f, paint)
        canvas.drawText("Rate (₹)", 360f, tableTop + 14f, paint)
        canvas.drawText("Taxable (₹)", 425f, tableTop + 14f, paint)
        canvas.drawText("Total (₹)", 515f, tableTop + 14f, paint)

        currentY = tableTop + tableHeight

        // Rows
        paint.isFakeBoldText = false
        paint.textSize = 8.5f
        invoice.items.forEachIndexed { index, item ->
            currentY += 18f

            // Alternate subtle row shading
            if (index % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                paint.style = Paint.Style.FILL
                canvas.drawRect(24f, currentY - 13f, (PAGE_WIDTH - 24).toFloat(), currentY + 5f, paint)
            }

            paint.color = Color.rgb(30, 41, 59)
            canvas.drawText("${index + 1}", 30f, currentY, paint)
            canvas.drawText(item.productName, 52f, currentY, paint)
            canvas.drawText(item.hsnCode, 215f, currentY, paint)
            canvas.drawText("${item.quantityPackets} pkts", 265f, currentY, paint)
            canvas.drawText("${item.weightKg} kg", 305f, currentY, paint)
            canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", item.unitRate), 360f, currentY, paint)
            canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", item.taxableAmount), 425f, currentY, paint)
            paint.isFakeBoldText = true
            canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", item.lineTotal), 515f, currentY, paint)
            paint.isFakeBoldText = false
        }

        currentY += 16f

        // Table Bottom Border
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(24f, currentY, (PAGE_WIDTH - 24).toFloat(), currentY, paint)

        currentY += 14f

        // --- Totals & GST Summary Box (Right Side) ---
        val totalsBoxLeft = 320f
        val totalsBoxRight = (PAGE_WIDTH - 24).toFloat()

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 8.5f

        // Subtotal
        canvas.drawText("Taxable Subtotal:", totalsBoxLeft, currentY, paint)
        canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", invoice.subtotal), totalsBoxRight - 65f, currentY, paint)

        currentY += 13f
        canvas.drawText("CGST (2.5%):", totalsBoxLeft, currentY, paint)
        canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", invoice.cgstTotal), totalsBoxRight - 65f, currentY, paint)

        currentY += 13f
        canvas.drawText("SGST (2.5%):", totalsBoxLeft, currentY, paint)
        canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", invoice.sgstTotal), totalsBoxRight - 65f, currentY, paint)

        currentY += 13f
        paint.isFakeBoldText = true
        canvas.drawText("Total GST (5.0%):", totalsBoxLeft, currentY, paint)
        canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", invoice.cgstTotal + invoice.sgstTotal), totalsBoxRight - 65f, currentY, paint)

        currentY += 16f
        // Grand Total Highlight Bar
        paint.color = Color.rgb(240, 253, 244) // Light emerald
        canvas.drawRect(totalsBoxLeft - 8f, currentY - 14f, totalsBoxRight, currentY + 12f, paint)

        paint.color = Color.rgb(21, 128, 61) // Emerald text
        paint.textSize = 12f
        canvas.drawText("GRAND TOTAL:", totalsBoxLeft, currentY, paint)
        canvas.drawText(String.format(Locale.ENGLISH, "₹%.2f", invoice.grandTotal), totalsBoxRight - 75f, currentY, paint)

        // Left summary note: Weight & packets
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("Total Units: ${invoice.totalPackets} Packets | Total Net Weight: ${invoice.totalWeightKg} Kg", 24f, currentY - 18f, paint)
        canvas.drawText("Amount in Words: Indian Rupees Only", 24f, currentY - 4f, paint)

        currentY += 45f

        // --- Terms, Quality & System Declaration ---
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        canvas.drawRect(24f, currentY, (PAGE_WIDTH - 24).toFloat(), currentY + 70f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(24f, currentY, (PAGE_WIDTH - 24).toFloat(), currentY + 70f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 7.5f
        paint.isFakeBoldText = true
        canvas.drawText("TERMS & CONDITIONS & FRESHNESS COMMITMENT:", 32f, currentY + 14f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 7f
        canvas.drawText("1. Storage Invariant: 100% natural fermented batter without chemical preservatives. Keep chilled at 0°C to 4°C.", 32f, currentY + 26f, paint)
        canvas.drawText("2. Expiry Policy: Natural 10-day shelf life. Damaged or bloated packets must be returned on the next scheduled beat visit.", 32f, currentY + 37f, paint)
        canvas.drawText("3. Jurisdiction: All disputes are subject to Lucknow, Uttar Pradesh jurisdiction. Liability limited to invoice value.", 32f, currentY + 48f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.isFakeBoldText = true
        canvas.drawText("This is a computer-generated tax invoice issued via Absolute Fresh Field Sales. No signature required.", 32f, currentY + 61f, paint)

        // Footer Brand Line
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        canvas.drawText("Absolute Fresh Food Pvt. Ltd. • Delivering freshness from Lucknow kitchen to your store daily", 24f, PAGE_HEIGHT - 20f, paint)
    }
}
