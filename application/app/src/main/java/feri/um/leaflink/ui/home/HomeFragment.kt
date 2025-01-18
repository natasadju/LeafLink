package feri.um.leaflink.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import feri.um.leaflink.*
import feri.um.leaflink.databinding.FragmentHomeBinding
import feri.um.leaflink.helperClasses.DataType
import feri.um.leaflink.ui.RetrofitClient
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private val viewModel: MainActivityViewModel by activityViewModels()

    private val airQualityList = mutableListOf<AirQuality>()
    private val eventList = mutableListOf<Event>()
    private val parkList = mutableListOf<Park>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchInitialData()
        viewModel.dataType.observe(viewLifecycleOwner) { dataType ->
            updateMapMarkers(dataType)
        }

        setupMap()
    }

    private fun setupMap() {
        mapView = binding.mapView2
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(46.5596, 15.6385)
        mapController.setCenter(startPoint)
    }

    private fun fetchInitialData() {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    eventList.clear()
                    eventList.addAll(response.body().orEmpty())
                    Log.d("Fetching", "Events: ${eventList.size}")

                    if (eventList.isEmpty()) {
                        updateMapMarkers(DataType.EVENTS)
                        return
                    }

                    var parksToFetch = eventList.size
                    eventList.forEach { event ->
                        Log.d("Fetching", "Fetching park for Event: ${event.name}")
                        fetchParkDetails(event.parkId) {
                            parksToFetch--
                            if (parksToFetch == 0) {
                                // All parks have been fetched
                                updateMapMarkers(DataType.EVENTS)
                            }
                        }
                    }
                } else {
                    showToast("Failed to load events")
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })

        RetrofitClient.instance.getAirQuality().enqueue(object : Callback<List<AirQuality>> {
            override fun onResponse(call: Call<List<AirQuality>>, response: Response<List<AirQuality>>) {
                if (response.isSuccessful) {
                    airQualityList.clear()
                    airQualityList.addAll(response.body().orEmpty())
                    Log.d("Fetching", "AirQuality: ${airQualityList.size}")
                } else {
                    showToast("Failed to load air quality data")
                }
            }

            override fun onFailure(call: Call<List<AirQuality>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun updateMapMarkers(dataType: DataType) {
        mapView.overlays.clear()
        Log.d("MapMarkers", "Updating markers for $dataType")
        Log.d("MapMarkers", "Events: ${eventList.size}, parkSize: ${parkList.size} AirQuality: ${airQualityList.size}")
        when (dataType) {
            DataType.EVENTS -> {
                Log.d("MapMarkers", "Adding markers for Events")
                eventList.forEach { event ->
                    Log.d("MapMarkers", "Checking Event: ${event.name}")
                    parkList.forEach { park ->
                        Log.d("MapMarkers", "Checking if: ${event.parkId} with Park: ${park._id}")
                        if (event.parkId == park._id) {
                            Log.d("MapMarkers", "Adding marker for Event: ${event.name} with Park: ${park.name}")
                            addEventMarker(event, GeoPoint(park.lat.toDouble(), park.long.toDouble()), park)
                        }
                    }
                }
            }
            DataType.AIR_QUALITY -> {
                val filteredAirQuality = filterLatestAirQuality()
                Log.d("Filter", "size of filtered AirQuality: ${filteredAirQuality.size}")
                filteredAirQuality.forEach { handleAirQualityData(it) }
            }
        }
    }

    private fun fetchParkDetails(parkId: String, onParkFetched: (Park?) -> Unit) {
        RetrofitClient.instance.getParkDetails(parkId).enqueue(object : Callback<Park> {
            override fun onResponse(call: Call<Park>, response: Response<Park>) {
                if (response.isSuccessful) {
                    val park = response.body()
                    park?.let {
                        parkList.add(it)
                        Log.d("Fetching", "Park added: ${it.name}")
                    }
                    onParkFetched(park)
                } else {
                    Log.d("Fetching", "Failed to fetch park details for ID: $parkId")
                    onParkFetched(null)
                }
            }

            override fun onFailure(call: Call<Park>, t: Throwable) {
                Log.d("Fetching", "Error fetching park details: ${t.message}")
                onParkFetched(null)
            }
        })
    }


    private fun filterRecentAirQuality(): List<AirQuality> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        Log.d("Filter", "Today: ${today.time}, Yesterday: ${yesterday.time}, AirQuality size: ${airQualityList.size}")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

        return airQualityList.filter { airQuality ->
            val timestamp = airQuality.timestamp
            try {
                if (timestamp.isEmpty()) {
                    Log.d("Filter", "Skipping null or empty timestamp")
                    return@filter false
                }

                val date = dateFormat.parse(timestamp)
                date != null && (isSameDay(date, today.time) || isSameDay(date, yesterday.time))
            } catch (e: Exception) {
                Log.d("Filter", "Error parsing timestamp: $timestamp, Exception: ${e.message}")
                false
            }
        }
    }

    private fun filterLatestAirQuality(): List<AirQuality> {
        val groupedByStation = airQualityList.groupBy { it.station }

        val latestAirQualityList = groupedByStation.map { entry ->
            val validRecords = entry.value.filter { true }
            if (validRecords.isEmpty()) {
                Log.d("Filter", "No valid timestamp for station: ${entry.key}")
            }
            val latestRecord = entry.value
                .filter { true }
                .maxByOrNull { it.timestamp }
            latestRecord
        }.filterNotNull()

        Log.d("Filter", "Filtered latest AirQuality size: ${latestAirQualityList.size}")
        return latestAirQualityList
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }


    private fun handleAirQualityData(airQuality: AirQuality) {
        val locationConstants = LocationConstants()

        val geoPoint: GeoPoint = when (airQuality.station) {
            "MB Titova" -> locationConstants.MBTitova
            "LJ Bežigrad" -> locationConstants.LJBežigrad
            "LJ Celovška" -> locationConstants.LJCelovška
            "LJ Vič" -> locationConstants.LJVič
            "Kranj" -> locationConstants.Kranj
            "LJ Titova" -> locationConstants.LJTitova
            "MB Vrbanski" -> locationConstants.MBVrbanski
            "CE bolnica" -> locationConstants.CEbolnica
            "CE Ljubljanska" -> locationConstants.CEljubljanska
            "Ptuj" -> locationConstants.Ptuj
            "MS Rakičan" -> locationConstants.MSRakičan
            "MS Cankarjeva" -> locationConstants.MSCankarjeva
            "I.Bistrica Gregorčičeva" -> locationConstants.IBistricaGregorčičeva
            "NG Grčna" -> locationConstants.NGGrčna
            "Otlica" -> locationConstants.Otlica
            "Koper" -> locationConstants.Koper
            "Trbovlje" -> locationConstants.Trbovlje
            "Zagorje" -> locationConstants.Zagorje
            "Hrastnik" -> locationConstants.Hrastnik
            "Novo mesto" -> locationConstants.NovoMesto
            "Črna na Koroškem" -> locationConstants.ČrnaNaKoroškem
            "Črnomelj" -> locationConstants.Črnomelj
            "Iskrba" -> locationConstants.Iskrba
            "Krvavec" -> locationConstants.Krvavec
            else -> {
                Log.d("HandleAirQuality", "Unknown station: ${airQuality.station}")
                return
            }
        }

        val marker = Marker(mapView)
        val customIcon = ResourcesCompat.getDrawable(resources, R.drawable.air_quality, null) as BitmapDrawable
        val scaledBitmap = Bitmap.createScaledBitmap(customIcon.bitmap, 100, 100, true)
        marker.icon = BitmapDrawable(resources, scaledBitmap)
        marker.position = geoPoint
        marker.title = "Air Quality: ${airQuality.station}"
        marker.snippet = "PM2.5: ${airQuality.pm25}, PM10: ${airQuality.pm10}"

        marker.setOnMarkerClickListener { _, _ ->
            marker.showInfoWindow()
            true
        }

        mapView.overlays.add(marker)
    }


    private fun addEventMarker(event: Event, geoPoint: GeoPoint, park: Park) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = event.name
        marker.snippet = "Location: ${park.name}"
        marker.subDescription = "Date: ${formatEventDate(event.date)}"
        marker.setOnMarkerClickListener { _, _ ->
            marker.showInfoWindow()
            true
        }
        mapView.overlays.add(marker)
    }

    private fun formatEventDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return try {
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            date
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.onDetach()
    }
}
