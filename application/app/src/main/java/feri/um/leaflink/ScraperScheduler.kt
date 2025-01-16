package feri.um.leaflink

import android.content.SharedPreferences
import android.util.Log
import feri.um.leaflink.ui.settings.SettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class ScraperScheduler(
    private val scraper: Scraper,
    private val sharedPreferences: SharedPreferences
) {
    private var timer: Timer? = null

    fun startScheduler() {
        val frequency = sharedPreferences.getString(
            SettingsFragment.SCRAPE_FREQUENCY_KEY,
            SettingsFragment.FREQUENCY_HOURLY
        )
        val interval = when (frequency) {
            SettingsFragment.FREQUENCY_HOURLY -> 60 * 60 * 1000L
            SettingsFragment.FREQUENCY_DAILY -> 24 * 60 * 60 * 1000L
            SettingsFragment.FREQUENCY_NEVER -> null
            else -> null
        }

        if (interval != null) {
            timer?.cancel()
            timer = fixedRateTimer("scraper-timer", initialDelay = 0, period = interval) {
                CoroutineScope(Dispatchers.IO).launch {
                    scrapeAndStoreData()
                }
            }
        } else {
            stopScheduler()
        }
    }

    private suspend fun scrapeAndStoreData() {
        val pollenData = scraper.scrapePollen()
        pollenData.forEach { data ->
            val success = scraper.addPollenData(data)
            if (success) {
                Log.d("Pollen data successfully added to database: $data", "ScraperScheduler")
            } else {
                Log.d("Failed to add pollen data to database: $data", "ScraperScheduler scraping and storing")
            }
        }

        val airQualityData = scraper.scrapeAirQuality()
        airQualityData.forEach { data ->
            val success = scraper.addAirQualityData(data)
            if (success) {
                Log.d("Air quality data successfully added to database: $data", "airqualitydata")
            } else {
                println("Failed to add air quality data to database: $data")
            }
        }
    }

    fun stopScheduler() {
        timer?.cancel()
    }
}
