import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.*

//PM10	PM2,5	SO2	CO	Ozon	NO2	Benzen
data class AirQualityData(
    val station: String,
    val pm10: String,
    val pm25: String,
    val so2: String,
    val co: String,
    val ozon: String,
    val no2: String,
    val benzen: String
)

fun main() {
    println("========== Scraping Maribor Air Quality Data ==========")

    skrape(HttpFetcher) {
        request {
            url = "https://www.arso.gov.si/zrak/kakovost%20zraka/podatki/dnevne_koncentracije.html"
        }

        response {
            println("HTTP status code: ${status { code }}")
            println("HTTP status message: ${status { message }}")

            htmlDocument {
                // Select the table with class "online"
                table {
                    withClass = "online"
                    findAll("tr").drop(3).forEach { row ->
                        // if td.onlineimena = "MB Vrbanski" || "MB Titova" then find td.onlinedesno
                        try {
                            if (row.findFirst(".onlineimena")?.text?.contains("MB Vrbanski") == true ||
                                row.findFirst(".onlineimena")?.text?.contains("MB Titova") == true
                            ) {

//                                println(row.findFirst(".onlineimena")?.text)
                                //if there is nothing write -
                                val station = row.findFirst(".onlineimena")?.text
                                val pm10 = row.findAll(".onlinedesno")[0].text
                                val pm25 = row.findAll(".onlinedesno")[1].text
                                val so2 = row.findAll(".onlinedesno")[2].text
                                val co = row.findAll(".onlinedesno")[3].text
                                val ozon = row.findAll(".onlinedesno")[4].text
                                val no2 = row.findAll(".onlinedesno")[5].text
                                val benzen = row.findAll(".onlinedesno")[6].text

                                AirQualityData(
                                    station = station ?: "",
                                    pm10 = pm10,
                                    pm25 = pm25,
                                    so2 = so2,
                                    co = co,
                                    ozon = ozon,
                                    no2 = no2,
                                    benzen = benzen
                                ).run {
                                    println(
                                        "Station: $station\n" +
                                                "PM10: $pm10\n" +
                                                "PM2.5: $pm25\n" +
                                                "SO2: $so2\n" +
                                                "CO: $co\n" +
                                                "Ozon: $ozon\n" +
                                                "NO2: $no2\n" +
                                                "Benzen: $benzen\n"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            println("Error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}