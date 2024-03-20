package live.talkshop.sampleapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import live.talkshop.sampleapp.ui.theme.SampleAppTheme
import live.talkshop.sdk.core.authentication.TalkShopLive
import live.talkshop.sdk.core.chat.Chat
import live.talkshop.sdk.core.chat.models.MessageModel
import live.talkshop.sdk.core.show.Show

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this)
                }
            }
        }
    }
}

//Update these values to test scanarios
private const val clientKey = ""
private const val globalShowKey = ""
private const val jwt = ""

@Composable
fun MainScreen(context: Context) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        ClientKeyInputSection(context)
        ShowIdInputSection()
        InitializeChat()
        PublishMessage()
        ChatHistory()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientKeyInputSection(context: Context) {
    var clientKey by remember { mutableStateOf(clientKey) }
    var initializationResult by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = clientKey,
        onValueChange = { clientKey = it },
        label = { Text("Client Key") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            TalkShopLive.initialize(
                context,
                clientKey,
                debugMode = true,
                testMode = true,
                dnt = false
            ) {
                initializationResult = if (it) {
                    "Success"
                } else {
                    "Fail"
                }
            }
        },
        modifier = Modifier.wrapContentWidth(Alignment.End)

    ) {
        Text("Initialize SDK")
    }

    Spacer(modifier = Modifier.height(8.dp))

    initializationResult?.let {
        Text(it, color = if (it == "Success") Color.Green else Color.Red)
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowIdInputSection() {
    var showKey by remember { mutableStateOf(globalShowKey) }
    var showDetails by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = showKey,
        onValueChange = { showKey = it },
        label = { Text("Show ID") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row {
        Button(
            onClick = {
                coroutineScope.launch {
                    Show.getDetails(showKey) { error, show ->
                        if (error == null && show != null) {
                            showDetails =
                                "ID: ${show.id}, " +
                                        "\nName: ${show.name}, " +
                                        "\nDescription: ${show.description}, " +
                                        "\nStatus: ${show.status}, " +
                                        "\nTrailer URL: ${show.trailerUrl}, " +
                                        "\nHLS URL: ${show.hlsUrl}, " +
                                        "\nHLS Playback URL: ${show.hlsPlaybackUrl}"
                            errorText = null
                        } else {
                            errorText = error
                            showDetails = null
                        }

                    }
                }
            },
            modifier = Modifier.wrapContentWidth(Alignment.End)
        ) {
            Text("Fetch Show Details")
        }

        Spacer(modifier = Modifier.width(5.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    Show.getStatus(showKey) { error, show ->
                        if (error == null && show != null) {
                            showDetails = "Show Key: ${show.showKey}, " +
                                    "\nShow Status: ${show.status}," +
                                    "\nHLS Playback URL: ${show.hlsPlaybackUrl}," +
                                    "\nHLS URL: ${show.hlsUrl}"
                            errorText = null
                        } else {
                            errorText = error
                            showDetails = null
                        }

                    }
                }
            },
            modifier = Modifier.wrapContentWidth(Alignment.End)
        ) {
            Text("Fetch Show Status")
        }

    }
    Spacer(modifier = Modifier.height(8.dp))

    showDetails?.let { ShowDetails(it) }
    errorText?.let { Text(it, color = Color.Red) }
}

@Composable
fun ShowDetails(showDetails: String) {
    Column {
        Text(showDetails)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitializeChat() {
    var jwt by remember { mutableStateOf(jwt) }
    var isGuest by remember { mutableStateOf(false) }
    var apiResult by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = jwt,
        onValueChange = { jwt = it },
        label = { Text("JWT Token") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Is Guest", modifier = Modifier.weight(1f))
        Switch(
            checked = isGuest,
            onCheckedChange = { isGuest = it }
        )
    }

    Button(
        onClick = {
            apiResult = null
            Chat(globalShowKey, jwt, isGuest) { errorMessage, userTokenModel ->
                apiResult = errorMessage ?: "Great success! UserId: ${userTokenModel?.userId}"
            }
        },
        modifier = Modifier.wrapContentWidth(Alignment.End)
    ) {
        Text("Initialize Chat")
    }

    Spacer(modifier = Modifier.height(16.dp))

    apiResult?.let {
        Text(it, color = if (it.startsWith("Great success")) Color.Green else Color.Red)
    }
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublishMessage() {
    var message by remember { mutableStateOf("") }
    var apiResult by remember { mutableStateOf<String?>(null) }
    var subscriptionResult by remember { mutableStateOf("") }

    OutlinedTextField(
        value = message,
        onValueChange = { message = it },
        label = { Text("Message") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = {
                GlobalScope.launch {
                    Chat.subscribe(object : Chat.ChatCallback {
                        override fun onMessageReceived(message: MessageModel) {
                            subscriptionResult = "Received message: ${message.text}"
                        }
                    })
                }
            }
        ) {
            Text("Subscribe")
        }
        Spacer(modifier = Modifier.width(5.dp))
        Button(
            onClick = {
                GlobalScope.launch {
                    Chat.publish(message) { error, timetoken ->
                        apiResult = if (error == null) {
                            "Message sent, timetoken: $timetoken"
                        } else {
                            "Failed to send message: $error"
                        }
                    }
                }
            }
        ) {
            Text("Send Message")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    apiResult?.let {
        Text(it, color = if (!it.startsWith("Failed")) Color.Green else Color.Red)
    }

    if (subscriptionResult.isNotEmpty()) {
        Text(subscriptionResult, color = Color.Blue)
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ChatHistory() {
    var messages by remember { mutableStateOf<List<MessageModel>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPopup by remember { mutableStateOf(false) }

    Button(
        onClick = {
            GlobalScope.launch {
                Chat.getChatMessages { messageList, _, error ->
                    if (error == null) {
                        messages = messageList
                    } else {
                        errorMessage = error
                    }
                    showPopup = true
                }
            }
        }
    ) {
        Text("Get Chat History")
    }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("Chat History") },
            text = {
                if (messages != null) {
                    Column {
                        messages!!.forEach { message ->
                            message.text?.let { Text(it) }
                        }
                    }
                } else {
                    Text(errorMessage ?: "Unknown error")
                }
            },
            confirmButton = {
                Button(onClick = { showPopup = false }) {
                    Text("OK")
                }
            }
        )
    }
}