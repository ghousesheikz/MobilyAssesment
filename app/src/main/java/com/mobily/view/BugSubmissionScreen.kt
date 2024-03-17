package com.mobily.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.mobily.R
import com.mobily.database.BugReport
import com.mobily.utils.getUriFromView
import com.mobily.viewmodel.ImageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugSubmissionScreen(viewModel: ImageViewModel, intentUri: Uri?) {
    val imageResponseData by viewModel.imageResponse.observeAsState(null)
    var description by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val uploadImage = remember { mutableStateOf(false) }
    val uploadData = remember { mutableStateOf(false) }
    val showProgress = remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val view = LocalView.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )
    val context = LocalContext.current.applicationContext
    LaunchedEffect(key1 = true) {
        if (intentUri != null) imageUri = intentUri
    }
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Bug It",
                    style = MaterialTheme.typography.headlineSmall
                )
            })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                imageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .border(
                                BorderStroke(2.dp, Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Image(
                            painter = rememberImagePainter(uri),
                            contentDescription = null, // Decorative image so a content description is not required
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Inside// Adjust the scaling if needed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    IconButton(
                        onClick = { showDialog.value = true },
                        modifier = Modifier
                            .background(Color(0xFF006DFF), shape = CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_camera),
                            contentDescription = stringResource(id = R.string.upload_screenshot),
                            modifier = Modifier.size(30.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Button(
                        modifier = Modifier
                            .size(width = 80.dp, height = 40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006DFF), // Background color of the button
                            contentColor = Color.White // Color of the text and icon inside the button
                        ),
                        onClick = {
                            if (imageUri != null) {
                                uploadImage.value = true
                                showProgress.value = true
                                viewModel.uploadImage(imageUri)
                            }
                        }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
            FloatingActionButton(
                onClick = { uploadData.value = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(70.dp)
                    .padding(16.dp),
                containerColor = Color(0xFF006DFF),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_icon_excel),
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
            if (showProgress.value) {
                CircularProgressIndicator(
                    color = Color(0xFF006DFF),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }

    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            // Dialog content
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(
                            onClick = {
                                showDialog.value = false
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .background(Color(0xFF006DFF), shape = RoundedCornerShape(8.dp))
                                .size(50.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_gallery),
                                contentDescription = stringResource(id = R.string.upload_screenshot),
                                modifier = Modifier.size(36.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(50.dp))
                        IconButton(
                            onClick = {
                                showDialog.value = false
                                imageUri = getUriFromView(view, context)
                            },
                            modifier = Modifier
                                .background(Color(0xFF006DFF), shape = RoundedCornerShape(8.dp))
                                .size(50.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_screenshot),
                                contentDescription = stringResource(id = R.string.upload_screenshot),
                                modifier = Modifier.size(36.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
    if (uploadImage.value) {
        imageResponseData?.success?.let {
            showProgress.value = false
            uploadImage.value = false
            CoroutineScope(Dispatchers.IO).launch {
                if (viewModel.insertDatabase(
                        BugReport(
                            description = description,
                            imageUrl = it
                        )
                    ) > 0
                ) {
                    withContext(Dispatchers.Main) {
                        description = ""
                        imageUri = null
                        Toast.makeText(
                            context,
                            context.getString(R.string.data_inserted_successfully),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }
        imageResponseData?.error?.let {
            showProgress.value = false
            uploadImage.value = false
            Toast.makeText(context, it, Toast.LENGTH_LONG)
                .show()
        }
    }
    if (uploadData.value) {
        uploadData.value = false
        viewModel.getBugData { bugList ->
            if (bugList.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.uploadDataInSheets(bugList, success = {
                        viewModel.deleteData()
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.data_uploaded_successfully),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
                }
            }
        }
    }

}
