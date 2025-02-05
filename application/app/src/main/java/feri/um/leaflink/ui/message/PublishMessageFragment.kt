package feri.um.leaflink.ui.message

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import feri.um.leaflink.Event
import feri.um.leaflink.ExtremeEvent
import feri.um.leaflink.databinding.FragmentPublishMessageBinding
import feri.um.leaflink.ui.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PublishMessageFragment : Fragment() {

    private var _binding: FragmentPublishMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: String = "Unknown Location"
    private var selectedCategory: String = "Air Quality"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishMessageBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermissionAndFetchLocation()

        setupCategorySpinner()

        binding.buttonSubmitEvent.setOnClickListener {
            val message = binding.editTextMessage.text.toString()

            if (message.isBlank() || userLocation == "Unknown Location") {
                Toast.makeText(
                    requireContext(),
                    "Message cannot be empty and location is required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitEvent(message, userLocation, selectedCategory)
            }
        }

        return binding.root
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(feri.um.leaflink.R.array.categories)

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner3.adapter = adapter

        binding.spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = "Air Quality"
            }
        }
    }

    private fun checkLocationPermissionAndFetchLocation() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required to fetch location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLocation = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                } else {
                    userLocation = "Unable to fetch location"
                }
            }.addOnFailureListener {
                userLocation = "Failed to get location"
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun submitEvent(message: String, location: String, category: String) {
        val timestamp = getCurrentTimestamp()
        val event = ExtremeEvent(
            message = message,
            location = location,
            category = category,
            date = timestamp
        )

        Log.d("ExtremeEvent", event.toString())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.addExtremeEvent(event)
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Extreme event submitted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Failed to submit event",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

