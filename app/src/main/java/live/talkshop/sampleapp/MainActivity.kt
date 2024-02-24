package live.talkshop.sampleapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.talkshop.sampleapp.ui.theme.SampleAppTheme
import live.talkshop.sdk.core.authentication.TalkShopLive
import live.talkshop.sdk.core.show.Show
import live.talkshop.sdk.core.show.models.ShowObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowDetailsScreen(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailsScreen(applicationContext: Context) {
    var productKey by remember { mutableStateOf("") }
    var clientKey by remember { mutableStateOf("") } // New clientKey input field
    var show by remember { mutableStateOf<ShowObject?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Text field to input the client key
        OutlinedTextField(
            value = clientKey,
            onValueChange = { clientKey = it },
            label = { Text("Client Key") }, // Label for client key input
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Text field to input the product key
        OutlinedTextField(
            value = productKey,
            onValueChange = { productKey = it },
            label = { Text("Product Key") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to trigger the API call and TalkShopLive initialization
        Button(
            onClick = {
                // Trigger TalkShopLive initialization
                TalkShopLive.initialize(
                    applicationContext,
                    clientKey,
                    debugMode = false,
                    testMode = false,
                    dnt = false
                ) {
                    // Trigger the API call
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Show.getDetails(productKey, object : Show.GetDetailsCallback {
                                override fun onSuccess(showObject: ShowObject) {
                                    // Update the state with the retrieved show object
                                    show = showObject
                                    errorText = null
                                }

                                override fun onError(error: String) {
                                    // Update the state with the error message
                                    errorText = error
                                    show = null
                                }
                            })
                        } catch (e: Exception) {
                            // Update the state with the error message
                            errorText = e.message ?: "Unknown error occurred"
                            show = null
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display show details or error message
        show?.let { ShowDetails(it) }
        errorText?.let { Text(it, color = Color.Red) }
    }
}

@Composable
fun ShowDetails(showObject: ShowObject) {
    Column {
        Text("id: ${showObject.id}")
        Text("Name: ${showObject.name}")
        Text("Description: ${showObject.description}")
        Text("Status: ${showObject.status}")
        Text("HLS Playback URL: ${showObject.hlsPlaybackUrl}")
        Text("HLS URL: ${showObject.hlsUrl}")
        Text("Trailer URL: ${showObject.trailerUrl}")
        Text("Air Date: ${showObject.airDate}")
        Text("End At: ${showObject.endedAt}")
        Text("Event ID: ${showObject.eventId}")
        Text("CC: ${showObject.cc}")
        // Add more details as needed
    }
}