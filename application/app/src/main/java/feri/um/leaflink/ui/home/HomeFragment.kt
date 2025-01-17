package feri.um.leaflink.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import feri.um.leaflink.databinding.FragmentHomeBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import feri.um.leaflink.AirQuality
import feri.um.leaflink.Event
import feri.um.leaflink.LocationConstants
import feri.um.leaflink.MainActivityViewModel
import feri.um.leaflink.Park
import feri.um.leaflink.Pollen
import feri.um.leaflink.ui.RetrofitClient
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*
import feri.um.leaflink.helperClasses.DataType

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private val viewModel: MainActivityViewModel by activityViewModels()

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
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fetchDataFromBackend(null)
        viewModel.dataType.observe(viewLifecycleOwner) { dataType ->
            Log.d("HomeFragment", "Observer triggered: $dataType")
//            Toast.makeText(requireContext(), "Data type: $dataType", Toast.LENGTH_SHORT).show()
            updateMapMarkers(dataType)
        }

        mapView = binding.mapView2
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(46.5596, 15.6385)
        mapController.setCenter(startPoint)

        mapView.onResume()
    }

    private fun updateMapMarkers(dataType: DataType) {
//        Toast.makeText(requireContext(), "Updating markers for $dataType", Toast.LENGTH_SHORT).show()
        mapView.overlays.clear()
        fetchDataFromBackend(dataType)
    }

    private fun fetchDataFromBackend(dataType: DataType?) {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    events?.forEach { event ->
                        when (dataType) {
                            DataType.EVENTS -> fetchParkDetails(event)
                            DataType.AIR_QUALITY -> fetchAirQualityDetails()
                            DataType.POLLEN -> fetchParkDetails(event)
                            null -> fetchParkDetails(event)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchParkDetails(event: Event) {
        RetrofitClient.instance.getParkDetails(event.parkId).enqueue(object : Callback<Park> {
            override fun onResponse(call: Call<Park>, response: Response<Park>) {
                if (response.isSuccessful) {
                    val park = response.body()
                    park?.let {
                        val geoPoint = GeoPoint(it.lat.toDouble(), it.long.toDouble())
                        addEventMarker(event, geoPoint, it)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load park details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Park>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAirQualityDetails() {
        RetrofitClient.instance.getAirQuality().enqueue(object : Callback<List<AirQuality>> {
            override fun onResponse(call: Call<List<AirQuality>>, response: Response<List<AirQuality>>) {
                if (response.isSuccessful) {
                    val airQualityList = response.body()
                    airQualityList?.forEach { airQuality ->
                        handleAirQualityData(airQuality)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load air quality details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<AirQuality>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun fetchPollenDetails() {
//        RetrofitClient.instance.getPollens().enqueue(object : Callback<List<Pollen>> {
//            override fun onResponse(call: Call<List<Pollen>>, response: Response<List<Pollen>>) {
//                if (response.isSuccessful) {
//                    val pollenList = response.body()
//                    pollenList?.forEach { pollen ->
//                        // Handle each pollen item, e.g., add markers or update UI
//                        handlePollenData(pollen)
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Failed to load pollen details", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<Pollen>>, t: Throwable) {
//                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    private fun handleAirQualityData(airQuality: AirQuality) {
        val geoPoint: GeoPoint = if (airQuality.station == "MB Titova") {
            LocationConstants().MBTitova
        } else {
            LocationConstants().MBVrbanska
        }
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = "Air Quality: ${airQuality.station}"
        marker.snippet = "PM2.5: ${airQuality.pm25}, PM10: ${airQuality.pm10}"

        marker.setOnMarkerClickListener { _, _ ->
            marker.showInfoWindow()
            true
        }

        mapView.overlays.add(marker)
//        Toast.makeText(requireContext(), "Air Quality: ${airQuality.station}", Toast.LENGTH_SHORT).show()
    }

//    private fun handlePollenData(pollen: Pollen) {
//        // Example: Add a marker to the map
//        val geoPoint =
//        val marker = Marker(mapView)
//        marker.position = geoPoint
//        marker.title = "Pollen Type: ${pollen.type}"
//        marker.snippet = "Concentration: ${pollen.concentration}"
//
//        marker.setOnMarkerClickListener { _, _ ->
//            marker.showInfoWindow()
//            true
//        }
//
//        mapView.overlays.add(marker)
//    }


    private fun addEventMarker(event: Event, geoPoint: GeoPoint, park: Park) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = event.name

        val formattedDate = formatEventDate(event.date)
        marker.snippet = "Date: $formattedDate"
        marker.subDescription = "Location: ${park.name}"

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.onDetach()
    }
}
