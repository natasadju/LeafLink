package feri.um.leaflink.ui.pollenAndAir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import feri.um.leaflink.Pollen
import feri.um.leaflink.databinding.ItemPollenBinding
import java.text.SimpleDateFormat
import java.util.*

class PollenAdapter(private val pollens: List<Pollen>) : RecyclerView.Adapter<PollenAdapter.PollenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollenViewHolder {
        val binding = ItemPollenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PollenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PollenViewHolder, position: Int) {
        val pollen = pollens[position]
        holder.bind(pollen)
    }

    override fun getItemCount(): Int = pollens.size

    class PollenViewHolder(private val binding: ItemPollenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pollen: Pollen) {
            binding.typeTextView.text = "Type: ${pollen.type}"
            binding.valueTextView.text = "Value: ${pollen.value}"
            binding.timestampTextView.text = "Timestamp: ${formatEventDateTime(pollen.timestamp)}"
        }
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
