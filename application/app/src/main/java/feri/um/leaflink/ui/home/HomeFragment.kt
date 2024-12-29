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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import feri.um.leaflink.Event
import feri.um.leaflink.Park
import feri.um.leaflink.ui.RetrofitClient
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView

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

        mapView = binding.mapView2
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(46.5596, 15.6385)
        mapController.setCenter(startPoint)

        fetchDataFromBackend()

        mapView.onResume()

        return root
    }

    private fun fetchDataFromBackend() {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    events?.forEach { event ->
                        fetchParkDetails(event)
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

    private fun addEventMarker(event: Event, geoPoint: GeoPoint, park: Park) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = event.name

        val formattedDate = formatEventDate(event.date)
        marker.snippet = "Date: $formattedDate"
        marker.setSubDescription("Location: ${park.name}")

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
