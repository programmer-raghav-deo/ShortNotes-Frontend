package com.example.shortnotes.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.shortnotes.R
import com.example.shortnotes.components.TopBar
import com.example.shortnotes.components.get_alert
import com.example.shortnotes.networking.get_network_status
import com.example.shortnotes.ui.theme.ShortNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShortNotesTheme {
                var checker: Boolean by remember { mutableStateOf(get_network_status(getSystemService(ConnectivityManager::class.java))) }
                if (checker) {
                    App()
                } else {
                    get_alert(main_content = "Internet connection not available. Try connecting to WIFI or cellular data", button_content = "Retry", on_dismiss = {}, on_button_click = {checker = get_network_status(getSystemService(ConnectivityManager::class.java)) })
                }

            }
        }
    }
}

fun getIntentSubject(subject: String, context: Context): Intent {
    val intent: Intent = Intent(context, BrowseBySubject::class.java)
    val bundle: Bundle = Bundle()
    bundle.putString("subject", subject)
    intent.putExtras(bundle)
    return intent
}

@Composable
fun getCard(subject: String, context: Context, image_id: Int) {
    Card(onClick = {
        startActivity(context, getIntentSubject(subject, context), null)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(5.dp)
            .size(width = 112.dp, height = 100.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)) {
        Column (modifier = Modifier.align(Alignment.CenterHorizontally), verticalArrangement = Arrangement.Center) {
            Image(painter = painterResource(image_id), contentDescription = "Physics logo", modifier = Modifier.size(height = 60.dp, width = 65.dp).align(Alignment.CenterHorizontally).padding(vertical = 6.dp))
            Text(text = subject, fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp), textAlign = TextAlign.Center)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context: Context = LocalContext.current
    var query: String by remember { mutableStateOf("") }
    var active_search: Boolean by remember { mutableStateOf(false) }
    val map: Map<String, String> = mapOf("area under curve" to "Maths/calculus", "fluids" to "Physics/mechanics", "general conics" to "Maths/coordinate geometry", "kinetic theory of gases" to "Physics/mechanics", "logarithm" to "Maths", "logic" to "Maths", "modulus" to "Maths", "periodic classification of elements" to "Chemistry/inorganic chemistry", "solid state" to "Chemistry/physical chemistry", "solution of triangles" to "Maths/trigonometry", "trigonometric equations" to "Maths/trigonometry", "3D geometry" to "Maths/3D", "alternating current" to "Physics/electrodynamics", "binomial theorem" to "Maths/algebra", "capacitors" to "Physics/electrodynamics", "centre of mass and conservation of linear momentum" to "Physics/mechanics",  "chemical bonding" to "Chemistry/inorganic chemistry", "chemical kinetics" to "Chemistry/physical chemistry", "circles" to "Maths/coordinate geometry", "complex numbers" to "Maths/algebra", "current electricity" to "Physics/electrodynamics", "definite integeration" to "Maths/calculus", "determinants" to "Maths/algebra", "differential equations" to "Maths/calculus", "electrochemistry" to "Physics/electrodynamics", "electromagnetic induction" to "Physics/electrodynamics", "electromagnetic waves" to "Physics/electrodynamics", "electrostatics" to "Physics/electrodynamics", "ellipse" to "Maths/coordinate geometry", "gaseous state" to "Chemistry/physical chemistry", "gravitation" to "Physics/mechanics", "heat transfer" to "Physics/heat", "hyperbola" to "Maths/coordinate geometry", "indefinite integeration" to "Maths/calculus", "inverse trigonometric functions" to "Maths/trigonometry", "ionic equillibrium" to "Chemistry/physical chemistry", "limits" to "Maths/calculus", "liquid solution" to "Chemistry/physical chemistry", "mechanical properties of solids" to "Physics/mechanics", "modern physics" to "Physics", "parabola" to "Maths/coordinate geometry", "permutation and combination" to "Maths/algebra", "probability" to "Maths", "quadratic equations" to "Maths/algebra", "ray optics" to "Physics/optics", "simple harmonic motion" to "Physics/mechanics", "statistics" to "Maths", "straight lines" to "Maths/coordinate geometry", "thermal properties of matter" to "Physics/heat", "thermodynamics(chemistry)" to "Chemistry/physical chemistry", "thermodynamics(physics)" to "Physics/heat", "trigonometric ratios" to "Maths/trigonometry", "vectors" to "Maths/3D", "wave optics" to "Physics/optics", "waves" to "Physics/mechanics")
    val chapter_names: MutableList<String> = mutableListOf("area under curve", "fluids", "general conics", "kinetic theory of gases", "logarithm", "logic", "modulus", "periodic classification of elements", "solid state", "solution of triangles", "trigonometric equations", "3D geometry", "alternating current", "binomial theorem", "capacitors", "centre of mass and conservation of linear momentum", "chemical bonding", "chemical kinetics", "circles", "complex numbers", "current electricity", "definite integeration", "determinants", "differential equations", "electrochemistry", "electromagnetic induction", "electromagnetic waves", "electrostatics", "ellipse", "gaseous state", "gravitation", "heat transfer", "hyperbola", "indefinite integeration", "inverse trigonometric functions", "ionic equillibrium", "limits", "liquid solution", "mechanical properties of solids", "modern physics", "parabola", "permutation and combination", "probability", "quadratic equations", "ray optics", "simple harmonic motion", "statistics", "straight lines", "thermal properties of matter", "thermodynamics(chemistry)", "thermodynamics(physics)", "trigonometric ratios", "vectors", "wave optics", "waves")
    var display_list: MutableList<String> = remember { mutableStateListOf() }
    display_list = chapter_names

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() },
    ) { innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                query = query,
                onQueryChange = {
                    query = it
                    if (query.isEmpty()) {
                        display_list = chapter_names
                    } else {
                        display_list = mutableListOf()
                        for (chapter in chapter_names) {
                            if (chapter.contains(query)) {
                                display_list.add(chapter)
                            }
                        }
                        if (display_list.isEmpty()) {
                            display_list.add("No results found")
                        }
                    }

                },
                onSearch = {
                    active_search = false
                    if (query.isNotEmpty()) {
                        display_list = mutableListOf()
                        for (chapter in chapter_names) {
                            if (chapter.contains(it)) {
                                display_list.add(chapter)
                            }
                        }
                        if (display_list.isEmpty()) {
                            display_list.add("No results found")
                        }
                    }
                },
                active = active_search,
                onActiveChange = {
                    active_search = it
                },
                placeholder = { Text(text = "Search chapter name here") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (active_search) {
                        IconButton(onClick = {
                            if (query.isNotEmpty()) {
                                query = ""
                            } else {
                                active_search = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close/Clear")
                        }
                    }
                }
                ) {

                    LazyColumn {

                        display_list.forEach() {

                            item {

                                Row (modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                                    .clickable {
                                        val intent: Intent = Intent(context, PdfDisplay::class.java)
                                        val bundle: Bundle = Bundle()
                                        bundle.putString("file_path", map.getValue(it) + "/" + it)
                                        intent.putExtras(bundle)
                                        startActivity(context, intent, null)
                                    },
                                    horizontalArrangement = Arrangement.Center) {
                                    Text(text = it)
                                }

                            }


                        }
                    }

            }

            HorizontalDivider(thickness = 5.dp)

            Column (modifier = Modifier
                .padding(5.dp)
                .fillMaxHeight(), verticalArrangement = Arrangement.Center) {

                Row (modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp), horizontalArrangement = Arrangement.Center) {
                    Text(text = "Browse by Subject", fontSize = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }


                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                    getCard(subject = "Physics", context = context, image_id = R.drawable.i1)

                    getCard(subject = "Chemistry", context = context, image_id = R.drawable.i2)

                    getCard(subject = "Maths", context = context, image_id = R.drawable.i3)


            }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShortNotesTheme {
        App()
    }
}