import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import java.text.SimpleDateFormat
import java.util.*


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
                                false,
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

fun scrapeAirQuality(): List<AirData> {
    val scrapedItems = mutableListOf<AirData>()
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
                                    val station = stationCell?.text
                                    val cells = row.findAll(".onlinedesno")
                                    if (cells.size >= 7) {
                                        val pm10 = cells[0].text
                                        val pm25 = cells[1].text
                                        val so2 = cells[2].text
                                        val co = cells[3].text
                                        val ozon = cells[4].text
                                        val no2 = cells[5].text
                                        val benzen = if (cells[6].text == "-") "" else cells[6].text

                                        scrapedItems.add(
                                            AirData(
                                                _id = UUID.randomUUID().toString(),
                                                station = station?:"",
                                                pm10 = pm10,
                                                pm25 = pm25,
                                                so2 = so2,
                                                co = co,
                                                ozon = ozon,
                                                no2 = no2,
                                                benzen = benzen,
                                                timestamp = timestamp,
                                                isFake = false,
                                                __v = 0
                                            )
                                        )
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
                "AirQuality" -> ScrapedAirCard(scrapedData[index] as AirData)
                "Pollen" -> PollenCard(scrapedData[index] as PollenItem)
            }
        }
    }
}

