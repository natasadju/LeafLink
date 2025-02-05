package feri.um.leaflink.events

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import feri.um.leaflink.Event
import feri.um.leaflink.Image
import feri.um.leaflink.Park
import feri.um.leaflink.R
import feri.um.leaflink.databinding.DialogEventDetailsBinding
import feri.um.leaflink.ui.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

import org.osmdroid.api.IMapController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class EventsAdapter(
    private val context: Context,
    private val events: List<Event>
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.textViewEventName)
        val eventLocation: TextView = itemView.findViewById(R.id.textViewEventLocation)
        val eventDate: TextView = itemView.findViewById(R.id.textViewEventDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.name
        holder.eventDate.text = formatEventDateTime(event.date)

        fetchParkName(event.parkId) { parkName ->
            holder.eventLocation.text = parkName
        }

        holder.itemView.setOnClickListener {
            showEventDetailsDialog(event)
        }
    }

    private fun showEventDetailsDialog(event: Event) {
        val dialogBinding = DialogEventDetailsBinding.inflate(LayoutInflater.from(context))

        // Set event details in the dialog
        dialogBinding.dialogEventName.text = event.name
        dialogBinding.dialogEventDate.text = formatEventDateTime(event.date)
        dialogBinding.dialogEventDetails.text = event.description

        fetchParkDetails(event.parkId) { parkName, latitude, longitude ->
            dialogBinding.dialogEventLocation.text = parkName
            // Initialize the map view and add the marker
            val mapView = dialogBinding.mapView
            mapView.setBuiltInZoomControls(true)
            mapView.setMultiTouchControls(true)

            val geoPoint = GeoPoint(latitude, longitude)
            val mapController: IMapController = mapView.controller
            mapController.setZoom(15)
            mapController.setCenter(geoPoint)

            // Add a marker to the map
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.title = parkName
            mapView.overlays.add(marker)
        }

        fetchImagesForEvent(event._id) { imageUrls ->
            val photoGalleryRecyclerView = dialogBinding.photoGallery
            photoGalleryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val photoGalleryAdapter = PhotoGalleryAdapter(context, imageUrls)
            photoGalleryRecyclerView.adapter = photoGalleryAdapter
        }

        AlertDialog.Builder(context)
            .setTitle("Event Details")
            .setView(dialogBinding.root)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun fetchParkDetails(parkId: String, callback: (String, Double, Double) -> Unit) {
        RetrofitClient.instance.getParkDetails(parkId).enqueue(object : Callback<Park> {
            override fun onResponse(call: Call<Park>, response: Response<Park>) {
                if (response.isSuccessful) {
                    val park = response.body()

                    // Convert lat and long from String to Double
                    val latitude = park?.lat?.toDoubleOrNull() ?: 0.0
                    val longitude = park?.long?.toDoubleOrNull() ?: 0.0

                    callback(park?.name ?: "Unknown location", latitude, longitude)
                } else {
                    callback("Failed to load location", 0.0, 0.0)
                }
            }

            override fun onFailure(call: Call<Park>, t: Throwable) {
                callback("Error loading location", 0.0, 0.0)
            }
        })
    }

    private fun fetchImagesForEvent(eventId: String, callback: (List<String>) -> Unit) {
        RetrofitClient.instance.getImagesByEventId(eventId).enqueue(object : Callback<List<Image>> {
            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                if (response.isSuccessful) {
                    Log.d("EventImages", "Response successful: ${response.body()}")
                    val imageUrls = response.body()?.map { it.imageUrl } ?: emptyList()

                    Log.d("EventImages", "Fetched Image URLs: $imageUrls")
                    callback(imageUrls)
                } else {
                    Log.d("EventImages", "Response failed: ${response.errorBody()?.string()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                Log.e("EventImages", "Error fetching images: ${t.message}")
                callback(emptyList())
            }
        })
    }


    private fun fetchParkName(parkId: String, callback: (String) -> Unit) {
        RetrofitClient.instance.getParkDetails(parkId).enqueue(object : Callback<Park> {
            override fun onResponse(call: Call<Park>, response: Response<Park>) {
                if (response.isSuccessful) {
                    val park = response.body()
                    callback(park?.name ?: "Unknown location")
                } else {
                    callback("Failed to load location")
                }
            }

            override fun onFailure(call: Call<Park>, t: Throwable) {
                callback("Error loading location")
            }
        })
    }

    private fun formatEventDateTime(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return try {
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            date
        }
    }

    override fun getItemCount() = events.size
}
