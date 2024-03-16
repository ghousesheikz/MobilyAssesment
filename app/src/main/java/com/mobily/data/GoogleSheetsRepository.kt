package com.mobily.data

import android.app.Application
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.mobily.R
import com.mobily.database.BugReport
import com.mobily.utils.GOOGLESHEET_ID
import com.mobily.utils.SPREADSHEET_URL
import com.mobily.utils.appendData
import com.mobily.utils.ensureSheetExists
import com.mobily.utils.getSheetDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSheetsRepository {

    fun uploadDataGoogleSheets(
        application: Application?,
        bugdata: List<BugReport>,
        success: (Boolean) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val credentialsStream = application?.assets?.open("credentials.json")
                val credential = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(listOf(SPREADSHEET_URL))

                val service = Sheets.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName(application?.getString(R.string.app_name))
                    .build()

                val spreadsheetId = GOOGLESHEET_ID
                val range =
                    "'${getSheetDate()}'!A1" // Specify the range where you want to write data
                if (ensureSheetExists(service, spreadsheetId, getSheetDate())) {
                    // data to be written to the spreadsheet
                    val data = arrayListOf<List<String>>()
                    bugdata.forEach {
                        data.add(
                            listOf(it.id.toString(), it.imageUrl, it.description)
                        )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        appendData(service, spreadsheetId, range, data)
                        success.invoke(true)
                    }
                }
            }
        } catch (exp: Exception) {
            success.invoke(false)
        }
    }
}