package com.mobily

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.mobily.ui.theme.MobilyAssesmentTheme
import com.mobily.view.BugSubmissionScreen
import com.mobily.viewmodel.ImageViewModel


class MainActivity : ComponentActivity() {
    private val viewModel: ImageViewModel by viewModels()
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseStorage.getInstance().maxUploadRetryTimeMillis = 90000
        if (Intent.ACTION_SEND.equals(intent.action) && intent.type != null) {
            if (intent.type?.startsWith("image/") == true) {
                imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }
        }
        setContent {
            MobilyAssesmentTheme {
                BugSubmissionScreen(viewModel,imageUri)
            }
        }
    }

    private fun handleIncomingImage(intent: Intent?) {

    }

}

// Spread sheet where bug data is stored.
// https://docs.google.com/spreadsheets/d/1ygyuPDkjcyo6cWDwfd10uesJRkw07ek0FFColmU5twU/edit#gid=1641608672

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilyAssesmentTheme {
        Greeting("Android")
    }
}