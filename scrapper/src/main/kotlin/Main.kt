import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.*

fun main() {
    println("========== Scraping Air Quality Data ==========")

    skrape(HttpFetcher) {
        request {
            url = "https://www.arso.gov.si/zrak/kakovost%20zraka/podatki/dnevne_koncentracije.html"
        }

        response {
            println("HTTP status code: ${status { code }}")
            println("HTTP status message: ${status { message }}")

            htmlDocument {
                fun processTable(tableIndex: Int) { // Instead of going through all of the tables with the class name we make a function to process the table by its index
                    val tables = findAll("table.online")
                    if (tableIndex < tables.size) {
                        val table = tables[tableIndex]
                        table.findAll("tr").drop(3).forEach { row ->
                            try {
                                val stationCell = row.maybe { findFirst(".onlineimena") }  //MAYBE!!!!
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
                    } else {
                        println("Table at index $tableIndex not found")
                    }
                }

                processTable(0)
            }
        }
    }
}

inline fun <T> T.maybe(block: T.() -> T?): T? = try {
    block()
} catch (e: Exception) {
    null
}
