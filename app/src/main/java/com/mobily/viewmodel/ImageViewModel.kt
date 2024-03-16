package com.mobily.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.mobily.R
import com.mobily.data.GoogleApiRepository
import com.mobily.data.NetworkRepository
import com.mobily.database.AppDatabase
import com.mobily.database.BugReport
import com.mobily.database.BugReportDao
import com.mobily.model.ImageModel
import com.mobily.utils.appendData
import com.mobily.utils.ensureSheetExists
import com.mobily.utils.getSheetDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoogleApiRepository()
    private val networkRepository = NetworkRepository()

    private val _imageResponse = MutableLiveData<ImageModel>()
    val imageResponse: LiveData<ImageModel> = _imageResponse

    private val bugReportDao: BugReportDao
    private var application: Application? = null

    init {
        val database = AppDatabase.getDatabase(application)
        bugReportDao = database.bugReportDao()
        this.application = application
    }

    fun uploadImage(imageUri: Uri?) {
        viewModelScope.launch {
            try {
                repository.uploadImage(imageUri, success = {
                    _imageResponse.value = ImageModel(success = it)
                }, error = {
                    _imageResponse.value = ImageModel(error = it)
                })
            } catch (e: Exception) {
                _imageResponse.value = ImageModel(error = e.message.toString())
            }
        }
    }

    suspend fun insertDatabase(bugData: BugReport): Long {
        return bugReportDao.insert(bugData)
    }

    fun getBugData(bugdata: (List<BugReport>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            bugdata.invoke(bugReportDao.getAllData())
        }
    }

    fun deleteData(){
        bugReportDao.deleteAll()
    }

    fun uploadDataInSheets(bugdata: List<BugReport>, success: (Boolean) -> Unit) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val credentialsStream = application?.assets?.open("credentials.json")
                val credential = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

                val service = Sheets.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName(application?.getString(R.string.app_name))
                    .build()

                val spreadsheetId = "1ygyuPDkjcyo6cWDwfd10uesJRkw07ek0FFColmU5twU"
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
