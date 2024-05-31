import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import java.text.SimpleDateFormat
import java.util.*

data class PollenItem(
    val _id: String,
    val type: String,
    val value: String,
    val timestamp: String,
    val __v: Int
)


val client: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}

val gson = Gson()

fun scrapeData(option: String): List<Any> {
    return when (option) {
        "AirQuality" -> scrapeAirQuality()
        "Pollen" -> scrapePollen()
        else -> emptyList()
    }
}

fun scrapePollen(): List<PollenItem> {
    val scrapedItems = mutableListOf<PollenItem>()
    val timestamp = getCurrentTimestamp()

    skrape(HttpFetcher) {
        request {
            url = "https://air-quality.com/place/slovenia/maribor/95149348?lang=en&standard=aqi_us"
        }

        response {
            htmlDocument {
                val pollenItems = findFirst(".allergens").findAll(".pollutant-item")

                pollenItems.forEach { item ->
                    try {
                        val type = item.findFirst(".name").text
                        val value = item.findFirst(".value").text

                        scrapedItems.add(
                            PollenItem(
                                UUID.randomUUID().toString(),
                                type,
                                value,
                                timestamp,
                                0
                            )
                        )
                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                    }
                }
            }
        }
    }

    return scrapedItems
}

inline fun <T> T.maybe(block: T.() -> T?): T? = try {
    block()
} catch (e: Exception) {
    null
}

fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun scrapeAirQuality(): List<ScrapedAirData> {
    val scrapedItems = mutableListOf<ScrapedAirData>()
    val timestamp = getCurrentTimestamp()

    skrape(HttpFetcher) {
        request {
            url = "https://www.arso.gov.si/zrak/kakovost%20zraka/podatki/dnevne_koncentracije.html"
        }

        response {
            htmlDocument {
                fun processTable(tableIndex: Int) {
                    val tables = findAll("table.online")
                    if (tableIndex < tables.size) {
                        val table = tables[tableIndex]
                        table.findAll("tr").drop(3).forEach { row ->
                            try {
                                val stationCell = row.maybe { findFirst(".onlineimena") }
                                if (stationCell != null &&
                                    (stationCell.text.contains("MB Vrbanski") || stationCell.text.contains("MB Titova"))
                                ) {
                                    val station = stationCell.text
                                    val cells = row.findAll(".onlinedesno")
                                    if (cells.size >= 7) {
                                        val pm10 = cells[0].text
                                        val pm25 = cells[1].text
                                        val so2 = cells[2].text
                                        val co = cells[3].text
                                        val ozon = cells[4].text
                                        val no2 = cells[5].text
                                        val benzen = cells[6].text

                                        scrapedItems.add(
                                            ScrapedAirData(
                                                _id = UUID.randomUUID().toString(),
                                                station = station,
                                                pm10 = pm10,
                                                pm25 = pm25,
                                                so2 = so2,
                                                co = co,
                                                ozon = ozon,
                                                no2 = no2,
                                                benzen = benzen,
                                                timestamp = timestamp,
                                                __v = 0
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error: ${e.message}")
                            }
                        }
                    } else {
                        println("Table at index $tableIndex not found")
                    }
                }

                processTable(0)
            }
        }
    }

    return scrapedItems
}


@Composable
@Preview
fun App() {
    var selectedButton by remember { mutableStateOf("Add park") }
    var selectedScreen by remember { mutableStateOf("Add park") }

    Row(modifier = Modifier.fillMaxSize()) {
        Sidebar(selectedButton) { button ->
            selectedButton = button
            selectedScreen = button
        }
        when (selectedScreen) {
            "Add park" -> AddParkScreen {}
            "Add user" -> AddUserScreen {}
            "Parks" -> ParkGrid()
            "Users" -> UserGrid()
            "Air Quality" -> AirDataGrid()
            "Add Air Data" -> AddAirScreen {}
            "Scraper" -> ScraperMenu()
            else -> {}
        }
    }
}


@Composable
fun ScraperMenu() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("AirQuality") }
    var scrapedData by remember { mutableStateOf(emptyList<Any>()) }
    val options = listOf("AirQuality", "Pollen")

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Data to Scrape:")
        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier
                    .background(Color.Transparent)
                    .align(Alignment.Center)
            ) {
                Text(selectedOption)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        selectedOption = option
                        expanded = false
                        scrapedData = scrapeData(option)
                    }) {
                        Text(option)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ScrapedDataGrid(scrapedData, selectedOption)
    }
}

@Composable
fun ScrapedDataGrid(scrapedData: List<Any>, dataType: String) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        state = lazyGridState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(scrapedData.size) { index ->
            when (dataType) {
                "AirQuality" -> ScrapedAirCard(scrapedData[index] as ScrapedAirData)
                "Pollen" -> ScrapedPollenCard(scrapedData[index] as PollenItem)
            }
        }
    }
}

@Composable
fun ScrapedPollenCard(item: PollenItem) {
    var isAdding by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.type,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Value: ${item.value}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.timestamp,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    isAdding = true
                    addScrapedPollenData(item) { success ->
                        isAdding = false
                        showMessage = true
                    }
                },
                enabled = !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Add to Database")
                }
            }
            if (showMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAdding) "Adding..." else "Data added!",
                    color = if (isAdding) Color.Gray else Color.Green
                )
            }
        }
    }
}


fun addScrapedPollenData(item: PollenItem, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(item)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/pollen")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(false)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(response.isSuccessful)
        }
    })
}

@Composable
fun Sidebar(selectedButton: String, onButtonSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Divider()
        SidebarButton(
            text = "Add park",
            isSelected = selectedButton == "Add park",
            onClick = { onButtonSelected("Add park") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Parks",
            isSelected = selectedButton == "Parks",
            onClick = { onButtonSelected("Parks") },
            icon = Icons.Default.Forest
        )
        Divider()
        SidebarButton(
            text = "Add user",
            isSelected = selectedButton == "Add user",
            onClick = { onButtonSelected("Add user") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Users",
            isSelected = selectedButton == "Users",
            onClick = { onButtonSelected("Users") },
            icon = Icons.Default.Person
        )
        Divider()
        SidebarButton(
            text = "Add Air Data",
            isSelected = selectedButton == "Add Air Data",
            onClick = { onButtonSelected("Add Air Data") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Air Quality",
            isSelected = selectedButton == "Air Quality",
            onClick = { onButtonSelected("Air Quality") },
            icon = Icons.Default.Air
        )
        Divider()
        SidebarButton(
            text = "Scraper",
            isSelected = selectedButton == "Scraper",
            onClick = { onButtonSelected("Scraper") },
            icon = Icons.Default.Share
        )
        SidebarButton(
            text = "Generator",
            isSelected = selectedButton == "Generator",
            onClick = { onButtonSelected("Generator") },
            icon = Icons.Default.Build
        )
        Spacer(modifier = Modifier.weight(1f))
        SidebarButton(
            text = "About",
            isSelected = selectedButton == "About",
            onClick = { onButtonSelected("About") },
            icon = Icons.Default.Info
        )
    }
}

@Composable
fun SidebarButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    val contentColor = if (isSelected) Color.White else Color.Black

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Database manager") {
        MaterialTheme {
            Box {
                App()
            }
        }
    }
}
