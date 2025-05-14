package com.example.shortnotes.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.shortnotes.Constants
import com.example.shortnotes.R
import com.example.shortnotes.components.TopBar
import com.example.shortnotes.components.get_alert
import com.example.shortnotes.ui.theme.ShortNotesTheme
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BrowseBySubject : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bundle: Bundle? = intent.extras
        setContent {
            ShortNotesTheme {
                BrowseBySubjectScreen(bundle!!.getString("subject")!!)
            }
        }
    }
}

@Composable
fun BrowseBySubjectScreen(subject: String) {
    val context: Context = LocalContext.current
    val url_stack: MutableList<String> = mutableListOf(subject)
    var loading:Boolean by remember { mutableStateOf(false) }
    var file_list: List<String> = remember { mutableStateListOf() }
    var folder_list: List<String> = remember { mutableStateListOf() }
    var error_list: List<String> = remember { mutableStateListOf() }

    BackHandler {
        if (url_stack.size == 1) {
            (context as Activity).apply {
                this.finish()
            }
        } else {
            Thread(kotlinx.coroutines.Runnable {

                loading = true
                url_stack.removeAt(url_stack.size - 1)

                val connection: HttpURLConnection = URL("http://192.168.1.4:8080/browse_by_subject").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "text/plain")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val outputStreamWriter: OutputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(url_stack[url_stack.size - 1])
                outputStreamWriter.flush()
                outputStreamWriter.close()
                connection.connect()

                val response_string: StringBuilder = StringBuilder()
                try {
                    val inputStreamReader: InputStreamReader = InputStreamReader(connection.inputStream)
                    inputStreamReader.use { input ->

                        val bufferedReader: BufferedReader = BufferedReader(input)
                        bufferedReader.forEachLine {
                            response_string.append(it.trim())
                        }
                        bufferedReader.close()
                    }
                    inputStreamReader.close()
                    if (response_string.isNotEmpty()) {
                        val data: Map<String, List<String>> = Json.decodeFromString(response_string.toString())
                        if (data["file_list"] != null) {
                            file_list = data["file_list"]!!
                        }
                        if (data["folder_list"] != null) {
                            folder_list = data["folder_list"]!!
                        }
                        if (data["error_list"] != null) {
                            error_list = data["error_list"]!!
                        }

                    } else {
                        Log.e("BrowseBySubject", "Response is empty")
                    }
                } catch (e:Exception) {
                    Log.e("BrowseBySubject", e.localizedMessage!!)
                }

                connection.disconnect()
                loading = false

            }).start()

        }
    }

    Thread(kotlinx.coroutines.Runnable {

        loading = true

        val connection: HttpURLConnection = URL("http://192.168.1.4:8080/browse_by_subject").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.doInput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        val outputStreamWriter: OutputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(subject)
        outputStreamWriter.flush()
        outputStreamWriter.close()
        connection.connect()

        val response_string: StringBuilder = StringBuilder()
        try {
            val inputStreamReader: InputStreamReader = InputStreamReader(connection.inputStream)
            inputStreamReader.use { input ->

                val bufferedReader: BufferedReader = BufferedReader(input)
                bufferedReader.forEachLine {
                    response_string.append(it.trim())
                }
            }
            if (response_string.isNotEmpty()) {
                val data: Map<String, List<String>> = Json.decodeFromString(response_string.toString())
                if (data["file_list"] != null) {
                    file_list = data["file_list"]!!
                }
                if (data["folder_list"] != null) {
                    folder_list = data["folder_list"]!!
                }
                if (data["error_list"] != null) {
                    error_list = data["error_list"]!!
                }

            } else {
                Log.e("BrowseBySubject", "Response is empty")
            }
        } catch (e:Exception) {
            Log.e("BrowseBySubject", e.localizedMessage!!)
        }
        connection.disconnect()
        loading = false

    }).start()


    Scaffold (modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() },
        )
    { innerPadding ->
        if (loading) {
            Column (modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "Please wait while we are fetching data from servers ...", modifier = Modifier.padding(15.dp))
                LinearProgressIndicator()
            }
        } else {
            if (error_list.isNotEmpty()) {
                Column (modifier = Modifier.padding(innerPadding)) { get_alert(error_list[0], "", {}, {}) }
            } else {
                LazyVerticalGrid(modifier = Modifier.padding(innerPadding),
                    columns = GridCells.Fixed(2),
                    content = {

                        if (url_stack.size != 1) {

                            item {
                                Button(onClick = {

                                        Thread(kotlinx.coroutines.Runnable {

                                            loading = true
                                            url_stack.removeAt(url_stack.size - 1)

                                            val connection: HttpURLConnection = URL("http://192.168.1.4:8080/browse_by_subject").openConnection() as HttpURLConnection
                                            connection.requestMethod = "POST"
                                            connection.setRequestProperty("Content-Type", "text/plain")
                                            connection.setRequestProperty("Accept", "application/json")
                                            connection.doOutput = true
                                            connection.doInput = true
                                            connection.connectTimeout = 10000
                                            connection.readTimeout = 10000
                                            val outputStreamWriter: OutputStreamWriter = OutputStreamWriter(connection.outputStream)
                                            outputStreamWriter.write(url_stack[url_stack.size - 1])
                                            outputStreamWriter.flush()
                                            outputStreamWriter.close()
                                            connection.connect()

                                            val response_string: StringBuilder = StringBuilder()
                                            try {
                                                val inputStreamReader: InputStreamReader = InputStreamReader(connection.inputStream)
                                                inputStreamReader.use { input ->

                                                    val bufferedReader: BufferedReader = BufferedReader(input)
                                                    bufferedReader.forEachLine {
                                                        response_string.append(it.trim())
                                                    }
                                                    bufferedReader.close()
                                                }
                                                inputStreamReader.close()
                                                if (response_string.isNotEmpty()) {
                                                    val data: Map<String, List<String>> = Json.decodeFromString(response_string.toString())
                                                    if (data["file_list"] != null) {
                                                        file_list = data["file_list"]!!
                                                    }
                                                    if (data["folder_list"] != null) {
                                                        folder_list = data["folder_list"]!!
                                                    }
                                                    if (data["error_list"] != null) {
                                                        error_list = data["error_list"]!!
                                                    }

                                                } else {
                                                    Log.e("BrowseBySubject", "Response is empty")
                                                }
                                            } catch (e:Exception) {
                                                Log.e("BrowseBySubject", e.localizedMessage!!)
                                            }

                                            connection.disconnect()
                                            loading = false

                                        }).start()

                                }) {
                                    Column (verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                        .padding(7.dp)
                                        .fillMaxHeight()) {
                                        Image(imageVector = Icons.Default.ArrowBack, contentDescription = "Back arrow", modifier = Modifier.padding(4.dp))
                                    }

                                }
                            }

                        }

                        items(folder_list.size + file_list.size) { index ->
                        //If folder list is 2 and file list is also 2 then upto index 1 should be folder list

                        if (index < folder_list.size) {
                            Button(onClick = {
                                Thread(Runnable {

                                    loading = true
                                    url_stack.add("$subject/${folder_list[index]}")

                                    val connection: HttpURLConnection = URL("http://192.168.1.3:8080/browse_by_subject").openConnection() as HttpURLConnection
                                    connection.requestMethod = "POST"
                                    connection.setRequestProperty("Content-Type", "text/plain")
                                    connection.setRequestProperty("Accept", "application/json")
                                    connection.doOutput = true
                                    connection.doInput = true
                                    connection.connectTimeout = 10000
                                    connection.readTimeout = 10000
                                    val outputStreamWriter: OutputStreamWriter = OutputStreamWriter(connection.outputStream)
                                    outputStreamWriter.write("$subject/${folder_list[index]}")
                                    outputStreamWriter.flush()
                                    outputStreamWriter.close()
                                    connection.connect()

                                    val response_string: StringBuilder = StringBuilder()
                                    try {
                                        val inputStreamReader: InputStreamReader = InputStreamReader(connection.inputStream)
                                        inputStreamReader.use { input ->

                                            val bufferedReader: BufferedReader = BufferedReader(input)
                                            bufferedReader.forEachLine {
                                                response_string.append(it.trim())
                                            }
                                            bufferedReader.close()
                                        }
                                        inputStreamReader.close()
                                        if (response_string.isNotEmpty()) {
                                            val data: Map<String, List<String>> = Json.decodeFromString(response_string.toString())
                                            if (data["file_list"] != null) {
                                                file_list = data["file_list"]!!
                                            }
                                            if (data["folder_list"] != null) {
                                                folder_list = data["folder_list"]!!
                                            }
                                            if (data["error_list"] != null) {
                                                error_list = data["error_list"]!!
                                            }

                                        } else {
                                            Log.e("BrowseBySubject", "Response is empty")
                                        }
                                    } catch (e:Exception) {
                                        Log.e("BrowseBySubject", e.localizedMessage!!)
                                    }

                                    connection.disconnect()
                                    loading = false

                                }).start()
                            }) {
                                Column (verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(7.dp)) {
                                    Image(painter = painterResource(R.drawable.folder_icon), contentDescription = "Folder icon", modifier = Modifier.padding(4.dp))
                                    Text(folder_list[index], modifier = Modifier.padding(4.dp))
                                }
                            }
                        } else {
                            val actual_index: Int = index - (folder_list.size)
                            Button(onClick = {
                                    val intent: Intent = Intent(context, PdfDisplay::class.java)
                                    val bundle: Bundle = Bundle()
                                    bundle.putString("file_path", url_stack[url_stack.size - 1] + "/" + file_list[actual_index])
                                    intent.putExtras(bundle)
                                    startActivity(context, intent, null)
                                }
                            ) {
                                    Column(
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(7.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.document_icon),
                                            contentDescription = "File icon",
                                            modifier = Modifier.padding(4.dp)
                                        )
                                        Text(
                                            file_list[actual_index],
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                        }
                    } })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    ShortNotesTheme {
        BrowseBySubjectScreen("Preview subject")
    }
}