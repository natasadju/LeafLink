package feri.um.leaflink.ui.pollenAndAir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import feri.um.leaflink.AirQuality
import feri.um.leaflink.Pollen
import feri.um.leaflink.databinding.FragmentPollenAndAirBinding
import feri.um.leaflink.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PollenAirFragment : Fragment() {

    private var _binding: FragmentPollenAndAirBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPollenAndAirBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView for Pollens
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch and display pollens
        fetchPollens { pollens ->
            allPollens = pollens
            setupDropdown(pollens)
            displayPollens(pollens) // Default display of all pollens
        }

        // Fetch air quality data

        // Set up collapsible sections
        setupCollapsibleSections()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchPollens(onPollensFetched: (List<Pollen>) -> Unit) {
        RetrofitClient.instance.getPollens().enqueue(object : Callback<List<Pollen>> {
            override fun onResponse(call: Call<List<Pollen>>, response: Response<List<Pollen>>) {
                if (response.isSuccessful && response.body() != null) {
                    onPollensFetched(response.body()!!)
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Pollen>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupDropdown(pollens: List<Pollen>) {
        val types = pollens.map { it.type }.distinct().sorted()
        val options = listOf("All") + types
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = options[position]
                if (selectedType == "All") {
                    displayPollens(allPollens)
                } else {
                    val filteredPollens = allPollens.filter { it.type == selectedType }
                    displayPollens(filteredPollens)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun displayPollens(pollens: List<Pollen>) {
        binding.recyclerView.adapter = PollenAdapter(pollens)
    }


    private fun createTextView(text: String): TextView {
        return TextView(requireContext()).apply { this.text = text }
    }

    private fun setupCollapsibleSections() {
        // Pollens section collapsible
        binding.pollenSectionHeader.setOnClickListener {
            toggleSectionVisibility(binding.pollenSection)
        }

        // Air quality section collapsible

    private fun toggleSectionVisibility(section: View) {
        section.visibility = if (section.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}

