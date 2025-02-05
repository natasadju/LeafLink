package feri.um.leaflink.ui.simulation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import feri.um.leaflink.AirQuality
import feri.um.leaflink.databinding.FragmentSimulationBinding
import feri.um.leaflink.ui.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration

class SimulationFragment : Fragment() {

    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    private var rangeValue: Int = 0
    private var frequencyValue: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        _binding = FragmentSimulationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rangeSeekBar = binding.rangeSeekBar
        val rangeValueLabel = binding.rangeValueLabel
        val frequencySeekBar = binding.frequencySeekBar
        val frequencyValueLabel = binding.frequencyValueLabel
        val startSimulationButton = binding.startSimulationButton

        rangeSeekBar.max = 100
        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rangeValue = progress
                rangeValueLabel.text = "Value: $rangeValue"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        frequencySeekBar.max = 60
        frequencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                frequencyValue = progress
                frequencyValueLabel.text = "Frequency: ${frequencyValue}min"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startSimulationButton.setOnClickListener {
            if (frequencyValue > 0) {
                Toast.makeText(requireContext(), "Starting simulation...", Toast.LENGTH_SHORT).show()
                startSimulation()
            } else {
                Toast.makeText(requireContext(), "Frequency must be greater than 0.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startSimulation() {
        CoroutineScope(Dispatchers.IO).launch {
            val stepSize = 10
            val maxIterations = (rangeValue / stepSize) + 1

            repeat(maxIterations) { iteration ->
                val currentRange = iteration * stepSize
                if (currentRange > rangeValue) return@repeat

                val airQuality = generateFakeAirQuality(currentRange)
                val success = addAirQualityData(airQuality)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Log.d("Simulation", "Data sent successfully: $airQuality")
                    } else {
                        Log.e("Simulation", "Failed to send data: $airQuality")
                    }
                }
                kotlinx.coroutines.delay(frequencyValue * 60 * 1000L)
            }
        }
    }

    private fun generateFakeAirQuality(range: Int): AirQuality {
        return AirQuality(
            _id = "sim-${System.currentTimeMillis()}",
            station = "Simulated Station",
            pm10 = range.toDouble(),
            pm25 = range.toDouble(),
            so2 = range.toDouble(),
            co = range.toDouble(),
            ozon = range.toDouble(),
            no2 = range.toDouble(),
            benzen = range.toDouble(),
            isFake = true,
            timestamp = System.currentTimeMillis().toString(),
            __v = 0
        )
    }

    private suspend fun addAirQualityData(item: AirQuality): Boolean {
        return try {
            val response = RetrofitClient.instance.addAirQualityData(item)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

