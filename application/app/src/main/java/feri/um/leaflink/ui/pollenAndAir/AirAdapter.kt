package feri.um.leaflink.ui.pollenAndAir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import feri.um.leaflink.AirQuality
import feri.um.leaflink.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AirQualityAdapter(private val airQualityList: List<AirQuality>) :
    RecyclerView.Adapter<AirQualityAdapter.AirQualityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirQualityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_air_quality, parent, false)
        return AirQualityViewHolder(view)
    }

    override fun onBindViewHolder(holder: AirQualityViewHolder, position: Int) {
        val airQuality = airQualityList[position]
        holder.dateTextView.text = formatEventDateTime(airQuality.timestamp)
        holder.pm10TextView.text = airQuality.pm10?.toString() ?: "N/A"
        holder.pm25TextView.text = airQuality.pm25?.toString() ?: "N/A"
        holder.so2TextView.text = airQuality.so2?.toString() ?: "N/A"
        holder.co2TextView.text = airQuality.co?.toString() ?: "N/A"
        holder.ozonTextView.text = airQuality.ozon?.toString() ?: "N/A"
        holder.no2TextView.text = airQuality.no2?.toString() ?: "N/A"
        holder.benzenTextView.text = airQuality.benzen?.toString() ?: "N/A"
    }

    override fun getItemCount(): Int = airQualityList.size

    inner class AirQualityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val pm10TextView: TextView = itemView.findViewById(R.id.pm10TextView)
        val pm25TextView: TextView = itemView.findViewById(R.id.pm25TextView)
        val so2TextView: TextView = itemView.findViewById(R.id.so2TextView)
        val co2TextView: TextView = itemView.findViewById(R.id.co2TextView)
        val ozonTextView: TextView = itemView.findViewById(R.id.ozonTextView)
        val no2TextView: TextView = itemView.findViewById(R.id.no2TextView)
        val benzenTextView: TextView = itemView.findViewById(R.id.benzenTextView)
    }
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
