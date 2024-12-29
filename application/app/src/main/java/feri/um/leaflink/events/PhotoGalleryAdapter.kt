package feri.um.leaflink.events

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import feri.um.leaflink.R
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PhotoGalleryAdapter(private val context: Context, private val imageUrls: List<String>) : RecyclerView.Adapter<PhotoGalleryAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        // Check if the image URL is valid and can be opened
        if (canOpenImage(imageUrl)) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Placeholder while loading
                .error(R.drawable.side_nav_bar)           // Error image if loading fails
                .into(holder.imageView)
        } else {
            Glide.with(context)
                .load(R.drawable.side_nav_bar) // Show a default error image
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    // Function to check if the image can be opened (local or remote)
    private fun canOpenImage(imageUrl: String): Boolean {
        return try {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Check if the image URL is a valid remote URL
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD" // Use HEAD request to just check if the image exists
                connection.connectTimeout = 5000 // Timeout after 5 seconds
                connection.readTimeout = 5000
                connection.responseCode == HttpURLConnection.HTTP_OK
            } else {
                // Check if the image is a valid local file path
                val file = File(imageUrl)
                file.exists() && BitmapFactory.decodeFile(file.absolutePath) != null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
