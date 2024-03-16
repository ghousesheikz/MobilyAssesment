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
import com.mobily.data.FirebaseRepository
import com.mobily.data.GoogleSheetsRepository
import com.mobily.database.AppDatabase
import com.mobily.database.BugReport
import com.mobily.database.BugReportDao
import com.mobily.model.ImageModel
import com.mobily.utils.GOOGLESHEET_ID
import com.mobily.utils.SPREADSHEET_URL
import com.mobily.utils.appendData
import com.mobily.utils.ensureSheetExists
import com.mobily.utils.getSheetDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()
    private val googleSheetsRepository = GoogleSheetsRepository()

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
        googleSheetsRepository.uploadDataGoogleSheets(application,bugdata,success)
    }
}
