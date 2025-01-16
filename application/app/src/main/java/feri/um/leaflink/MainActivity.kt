package feri.um.leaflink

import android.app.Activity
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import feri.um.leaflink.databinding.ActivityMainBinding
import feri.um.leaflink.events.EventsAdapter
import feri.um.leaflink.ui.RetrofitClient
import feri.um.leaflink.ui.settings.SettingsFragment
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.Manifest
import android.content.SharedPreferences
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import feri.um.leaflink.helperClasses.DataType

class MainActivityViewModel : ViewModel() {
    private val _dataType = MutableLiveData<DataType>(DataType.EVENTS)
    val dataType: LiveData<DataType> get() = _dataType

    fun setDataType(newDataType: DataType) {
        _dataType.value = newDataType
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val notificationChannelId = "leaflink_notifications"
    var dataType: DataType = DataType.EVENTS
    var changed: Boolean = false
    lateinit var viewModel: MainActivityViewModel

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scraper: Scraper
    private lateinit var scraperScheduler: ScraperScheduler

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences =
            getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val theme =
            sharedPreferences.getString(SettingsFragment.THEME_KEY, SettingsFragment.THEME_LIGHT)
        if (theme == SettingsFragment.THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        createNotificationChannel()

        scraper = Scraper()
        scraperScheduler = ScraperScheduler(scraper, sharedPreferences)
        scraperScheduler.startScheduler()

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.imageProcessingFragment) {
                binding.appBarMain.fab.hide()
                binding.appBarMain.fab2.hide()
            } else {
                binding.appBarMain.fab.show()
                binding.appBarMain.fab2.show()
            }

            if (destination.id == R.id.settingsFragment) {
                binding.appBarMain.fab.hide()
                binding.appBarMain.fab2.hide()
            } else {
                binding.appBarMain.fab.show()
                binding.appBarMain.fab2.show()
            }
        }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val imageUri = data?.data
                    if (imageUri != null) {
                        handleSelectedImage(imageUri)
                    } else {
                        Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        binding.appBarMain.fab2.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.appBarMain.dataTypeSelectionBtn.setOnClickListener {
            showDataSelectionDialog()
        }

        binding.appBarMain.fab.setOnClickListener {
            RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
                override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                    if (response.isSuccessful) {
                        val events = response.body()
                        events?.let {
                            showEventsDialog(it)
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to load events",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_addEvent, R.id.nav_pollen
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleSelectedImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "selected_image.jpg")
        file.outputStream().use {
            inputStream?.copyTo(it)
        }
        uploadImage(file)
    }

    private fun showEventsDialog(events: List<Event>) {
        Log.d("MainActivity", "showEventsDialog: Received ${events.size} events")

        val dialogView = layoutInflater.inflate(R.layout.dialog_events_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewEvents)

        val adapter = EventsAdapter(this, events)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Events")
            .setView(dialogView)
            .setPositiveButton("Close") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_home_to_settingsFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take a Picture", "Select from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val navController = findNavController(R.id.nav_host_fragment_content_main)
                    navController.navigate(R.id.imageProcessingFragment)
                }

                1 -> selectImageFromGallery()
            }
        }
        builder.show()
    }

    private fun showDataSelectionDialog() {
        val options = arrayOf("Events", "Air Quality", "Pollen")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
//                    Toast.makeText(this, "Events selected", Toast.LENGTH_SHORT).show()
                    viewModel.setDataType(DataType.EVENTS)
                    Toast.makeText(this, "selected ${viewModel.dataType.value}", Toast.LENGTH_SHORT).show()
                }

                1 -> {
//                    Toast.makeText(this, "AirQuality selected", Toast.LENGTH_SHORT).show()
                    viewModel.setDataType(DataType.AIR_QUALITY)
                    Toast.makeText(this, "selected: ${viewModel.dataType.value}", Toast.LENGTH_SHORT).show()
                }

                2 -> {
//                    Toast.makeText(this, "Pollen selected", Toast.LENGTH_SHORT).show()
                    viewModel.setDataType(DataType.POLLEN)
                    Toast.makeText(this, "selected ${viewModel.dataType.value}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val file = File(currentPhotoPath)
                    uploadImage(file)
                }

                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data ?: return
                    val file = File(getRealPathFromURI(imageUri))
                    uploadImage(file)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun uploadImage(file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

        sendNotification(
            title = "Uploading Image",
            message = "The picture is being uploaded..."
        )

        RetrofitClient.instance.uploadImage(multipartBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        try {
                            val jsonObject = responseBody?.let { JSONObject(it) }
                            val result = jsonObject?.getString("result")
                            sendNotification(
                                title = getString(R.string.notification_upload_success_title),
                                message = result ?: "Upload successful"
                            )
                            Toast.makeText(this@MainActivity, "Response: $result", Toast.LENGTH_LONG).show()
                            Toast.makeText(
                                this@MainActivity,
                                "Response: $result",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error parsing response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                            sendNotification(
                            title = getString(R.string.notification_upload_fail_title),
                            message = "Upload failed: ${response.message()}"
                        )
                        Toast.makeText(this@MainActivity, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        Toast.makeText(
                            this@MainActivity,
                            "Upload failed: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sendNotification(title: String, message: String) {
        val context = applicationContext // Use the application context to send the notification
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_menu_camera) // Replace with your app's notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Check if notification permission is granted
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(index ?: 0)
        cursor?.close()
        return path ?: ""
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}
