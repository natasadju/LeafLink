package feri.um.leaflink.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import feri.um.leaflink.Event
import feri.um.leaflink.Park
import feri.um.leaflink.databinding.FragmentAddBinding
import feri.um.leaflink.ui.RetrofitClient
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.MapView
import android.util.Log
import feri.um.leaflink.EventNew
import feri.um.leaflink.ParksResponse
import feri.um.leaflink.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private var parks: List<Park> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapView = binding.mapView
        setupMapView()

        loadParks()

        binding.submitEventButton.setOnClickListener { submitEvent() }

        binding.parkSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                outlineSelectedPark(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMapView() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        mapController.setCenter(GeoPoint(46.5596, 15.6385))
    }

    private fun loadParks() {
        RetrofitClient.instance.getParks().enqueue(object : Callback<ParksResponse> {
            override fun onResponse(call: Call<ParksResponse>, response: Response<ParksResponse>) {
                if (response.isSuccessful) {
                    val parksResponse = response.body()
                    parksResponse?.let {
                        parks = it.parks
                        val parkNames = parks.map { it.name }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            parkNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.parkSpinner.adapter = adapter
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load parks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ParksResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error loading parks: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AddFragment", "Error loading parks", t)
            }
        })
    }

    private fun outlineSelectedPark(position: Int) {
        if (position != -1) {
            val selectedPark = parks[position]
            binding.mapView.overlays.clear()
            val parkGeoPoint = GeoPoint(selectedPark.lat.toDouble(), selectedPark.long.toDouble())
            val marker = org.osmdroid.views.overlay.Marker(binding.mapView)
            marker.position = parkGeoPoint
            val drawable = resources.getDrawable(R.drawable.pin, null)
            marker.icon = drawable
            binding.mapView.overlays.add(marker)
            binding.mapView.controller.setCenter(parkGeoPoint)
            binding.mapView.controller.setZoom(15.0)
        }
    }

    private fun submitEvent() {
        val selectedParkIndex = binding.parkSpinner.selectedItemPosition
        if (selectedParkIndex == -1) {
            Toast.makeText(requireContext(), "Please select a park", Toast.LENGTH_SHORT).show()
            return
        }

        val eventName = binding.eventNameInput.text.toString().trim()
        val eventDescription = binding.eventDescriptionInput.text.toString().trim()
        val eventDate = binding.editTextDate2.text.toString().trim()
        val eventTime = binding.editTextTime.text.toString().trim()
        val parkId = parks[selectedParkIndex]._id

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val dateTimeString = "$eventDate $eventTime"
        val inputDateFormat = SimpleDateFormat("ddMMyyyy HH:mm", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())

        try {
            val date = inputDateFormat.parse(dateTimeString)
            val formattedDate = outputDateFormat.format(date)

            val newEvent = EventNew(
                name = eventName,
                location = parkId,
                description = eventDescription,
                date = formattedDate
            )

            RetrofitClient.instance.addEvent(newEvent).enqueue(object : Callback<Event> {
                override fun onResponse(call: Call<Event>, response: Response<Event>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Event added successfully", Toast.LENGTH_SHORT).show()
                        clearFields()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add event", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Event>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        binding.eventNameInput.text.clear()
        binding.eventDescriptionInput.text.clear()
        binding.editTextDate2.text.clear()
        binding.editTextTime.text.clear()
        binding.parkSpinner.setSelection(0)
    }
}
