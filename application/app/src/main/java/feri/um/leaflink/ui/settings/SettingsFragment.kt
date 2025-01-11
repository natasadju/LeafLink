package feri.um.leaflink.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, 0)

        loadSettings()
        setupThemeSelection()

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
