package feri.um.leaflink

import feri.um.leaflink.ui.RetrofitClient
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class Scraper {

    fun scrapePollen(): List<Pollen> {
        val scrapedItems = mutableListOf<Pollen>()
        val timestamp = getCurrentTimestamp()

        try {
            val document = Jsoup.connect("https://air-quality.com/place/slovenia/maribor/95149348?lang=en&standard=aqi_us").get()
            val pollenItems = document.select(".allergens .pollutant-item")

            pollenItems.forEach { item ->
                try {
                    val type = item.selectFirst(".name")?.text() ?: "Unknown"
                    val value = item.selectFirst(".value")?.text()?.toDouble() ?: 0.0

                    scrapedItems.add(
                        Pollen(
                            UUID.randomUUID().toString(),
                            type,
                            value,
                            timestamp,
                            false,
                            0
                        )
                    )
                } catch (e: Exception) {
                    println("Error parsing pollen item: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error fetching pollen data: ${e.message}")
        }

        return scrapedItems
    }

    fun scrapeAirQuality(): List<AirQuality> {
        val scrapedItems = mutableListOf<AirQuality>()
        val timestamp = getCurrentTimestamp()

        try {
            val document = Jsoup.connect("https://www.arso.gov.si/zrak/kakovost zraka/podatki/dnevne_koncentracije.html").get()
            val table = document.select("table.online").firstOrNull()
            table?.select("tr")?.drop(3)?.forEach { row ->
                try {
                    val station = row.selectFirst(".onlineimena")?.text() ?: return@forEach
                    if (station.contains("MB Vrbanski") || station.contains("MB Titova")) {
                        val cells = row.select(".onlinedesno")
                        if (cells.size >= 7) {
                            val airQuality = AirQuality(
                                _id = UUID.randomUUID().toString(),
                                station = station,
                                pm10 = cells[0].text().toDoubleOrNull() ?: 0.0,
                                pm25 = cells[1].text().toDoubleOrNull() ?: 0.0,
                                so2 = cells[2].text().toDoubleOrNull() ?: 0.0,
                                co = cells[3].text().toDoubleOrNull() ?: 0.0,
                                ozon = cells[4].text().toDoubleOrNull() ?: 0.0,
                                no2 = cells[5].text().toDoubleOrNull() ?: 0.0,
                                benzen = cells[6].text().toDoubleOrNull() ?: 0.0,
                                isFake = false,
                                timestamp = timestamp,
                                __v = 0
                            )
                            scrapedItems.add(airQuality)
                        }
                    }
                } catch (e: Exception) {
                    println("Error parsing air quality row: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error fetching air quality data: ${e.message}")
        }

        return scrapedItems
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun addPollenData(item: Pollen): Boolean {
        return try {
            val response = RetrofitClient.instance.addPollenData(item)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addAirQualityData(item: AirQuality): Boolean {
        return try {
            val response = RetrofitClient.instance.addAirQualityData(item)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
