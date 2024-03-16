package com.mobily.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun takeScreenshot(view: View): Bitmap? {
    return try {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        bitmap.toSoftwareBitmap()
    } catch (exp: Exception) {
        null
    }
}

fun getUriFromView(view: View, context: Context):Uri? {
    val filename = "${System.currentTimeMillis()}_image"
    return try {
        val bitmap = takeScreenshot(view)
        bitmap?.let { saveBitmapToFile(context, it, filename) }
    } catch (exp: Exception) {
        null
    }
}

fun Bitmap.toSoftwareBitmap(): Bitmap? {
    return try {
        val softwareBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(softwareBitmap)
        canvas.drawBitmap(this, 0f, 0f, null)
        softwareBitmap
    } catch (exp: Exception) {
        null
    }
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): Uri? {
    // Get the directory for the app's private pictures directory.
    val directory = context.getExternalFilesDir(null)
    val imageFile = File(directory, fileName)
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
}

fun getSheetDate(): String {
    val currentDate = Date()
    val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
    return dateFormat.format(currentDate)
}

fun ensureSheetExists(
    service: Sheets,
    spreadsheetId: String,
    sheetName: String
): Boolean {
    val spreadsheet = service.spreadsheets().get(spreadsheetId).execute()
    val sheets = spreadsheet.sheets ?: listOf()
    val exists = sheets.any { sheet -> sheet.properties.title == sheetName }
    return if (!exists) {
        val addSheetRequest = AddSheetRequest().setProperties(
            SheetProperties().setTitle(sheetName)
        )
        val batchUpdateRequest = BatchUpdateSpreadsheetRequest()
            .setRequests(listOf(Request().setAddSheet(addSheetRequest)))

        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
        true
    } else {
        true
    }
}

suspend fun appendData(
    service: Sheets?,
    spreadsheetId: String,
    range: String,
    data: List<List<String>>
) {
    withContext(Dispatchers.IO) {
        val body = ValueRange().setValues(data)
        service?.spreadsheets()?.values()
            ?.append(spreadsheetId, range, body)
            ?.setValueInputOption("RAW")
            ?.execute()
    }
}


