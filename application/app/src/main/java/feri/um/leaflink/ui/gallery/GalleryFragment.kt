package feri.um.leaflink.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import feri.um.leaflink.databinding.FragmentAddBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class GalleryFragment : Fragment() {

private var _binding: FragmentAddBinding? = null
  private val binding get() = _binding!!
    private lateinit var mapView: MapView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

    _binding = FragmentAddBinding.inflate(inflater, container, false)
    val root: View = binding.root

      setupMapView()


    return root
  }



override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMapView() {
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = org.osmdroid.util.GeoPoint(46.5596, 15.6385)
        mapController.setCenter(startPoint)


        mapView.onResume()
    }
}