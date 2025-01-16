package feri.um.leaflink.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import feri.um.leaflink.R
import feri.um.leaflink.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences


    companion object {
        const val PREFS_NAME = "settings_prefs"
        const val THEME_KEY = "theme"
        const val NOTIFICATION_KEY = "notifications"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val SCRAPE_FREQUENCY_KEY = "scrape_frequency"
        const val FREQUENCY_HOURLY = "hourly"
        const val FREQUENCY_DAILY = "daily"
        const val FREQUENCY_NEVER = "never"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)

        loadSettings()
        setupThemeSelection()
        setupScrapeFrequencyDropdown()

        return binding.root
    }

    private fun loadSettings() {
        val theme = sharedPreferences.getString(THEME_KEY, THEME_LIGHT)
        if (theme == THEME_DARK) {
            binding.darkThemeRadio.isChecked = true
        } else {
            binding.lightThemeRadio.isChecked = true
        }

        val notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATION_KEY, true)
        binding.notificationSwitch.isChecked = notificationsEnabled


        val frequency = sharedPreferences.getString(SCRAPE_FREQUENCY_KEY, FREQUENCY_HOURLY)
        val frequencyIndex = getFrequencyIndex(frequency)
        binding.spinner2.setSelection(frequencyIndex)
    }

    private fun getFrequencyIndex(frequency: String?): Int {
        return when (frequency) {
            FREQUENCY_HOURLY -> 0
            FREQUENCY_DAILY -> 1
            FREQUENCY_NEVER -> 2
            else -> 2
        }
    }

    private fun setupScrapeFrequencyDropdown() {
        val frequencyOptions = resources.getStringArray(R.array.frequency_spinner).toList()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            frequencyOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner2.adapter = adapter

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedFrequency = when (position) {
                    0 -> FREQUENCY_NEVER
                    1 -> FREQUENCY_HOURLY
                    2 -> FREQUENCY_DAILY
                    else -> FREQUENCY_NEVER
                }
                saveScrapeFrequency(selectedFrequency)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }


    private fun saveScrapeFrequency(frequency: String) {
        sharedPreferences.edit().putString(SCRAPE_FREQUENCY_KEY, frequency).apply()
    }

    private fun setupThemeSelection() {
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.lightThemeRadio -> {
                    saveTheme(THEME_LIGHT)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                R.id.darkThemeRadio -> {
                    saveTheme(THEME_DARK)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    private fun saveTheme(theme: String) {
        sharedPreferences.edit().putString(THEME_KEY, theme).apply()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
