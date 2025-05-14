package com.example.shortnotes.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shortnotes.screens.ui.theme.ShortNotesTheme
import java.io.File
import androidx.core.graphics.createBitmap
import com.example.shortnotes.Constants
import com.example.shortnotes.components.TopBar
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PdfDisplay : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bundle: Bundle? = intent.extras
        setContent {
            ShortNotesTheme {
                ChapterDisplayScreen(bundle!!.getString("file_path")!!)
            }
        }
    }
}

@Composable
fun ChapterDisplayScreen(file_path: String) {

    val temp_file: File = File.createTempFile("temp", ".pdf")
    var loading: Boolean by remember { mutableStateOf(false) }

    Thread(kotlinx.coroutines.Runnable {

        loading = true

        val connection: HttpURLConnection = URL("http://192.168.1.4:8080/get_file").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.doInput = true
        connection.doOutput = true
        OutputStreamWriter(connection.outputStream).apply {
            this.write(file_path)
            this.flush()
            this.close()
        }
        connection.connect()

        connection.inputStream.apply {
            this.copyTo(temp_file.outputStream())
            this.close()
        }

        connection.disconnect()
        loading = false

    }).start()



    Scaffold (modifier = Modifier.fillMaxSize(), topBar = { TopBar() }) { innerPadding ->

        if (loading) {
            Column (modifier = Modifier.padding(innerPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = "Please wait while we are fetching data from servers ...", modifier = Modifier.padding(15.dp))
                LinearProgressIndicator()
            }
        } else {

            val pdfRenderer: PdfRenderer = PdfRenderer(ParcelFileDescriptor.open(
                temp_file,
                ParcelFileDescriptor.MODE_READ_ONLY
            ))

            LazyColumn (modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(count = pdfRenderer.pageCount) { i ->
                    val page: PdfRenderer.Page = pdfRenderer.openPage(i)
                    val bitmap: Bitmap = createBitmap(
                        page.width,
                        page.height
                    )
                    android.graphics.Canvas(bitmap).apply {
                        drawColor(Color.White.toArgb())
                        drawBitmap(bitmap, 0f, 0f, null)
                    }
                    page.render(bitmap, null, null, Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    Image(bitmap = bitmap.asImageBitmap(),
                        null,
                        modifier = Modifier
                            .border(width = 2.dp, color = Color.Gray)
                            .fillMaxSize()
                            .aspectRatio(9f / 16f ),
                    )
                }
            }

            temp_file.delete()

        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    ShortNotesTheme {
        ChapterDisplayScreen("")
    }
}