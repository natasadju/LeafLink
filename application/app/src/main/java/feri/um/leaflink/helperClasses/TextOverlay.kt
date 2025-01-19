package feri.um.leaflink.helperClasses

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import org.osmdroid.util.GeoPoint

class TextOverlay(private val text: String, private val geoPoint: GeoPoint, private val backgroundColor: Int) : Overlay() {

    override fun draw(c: Canvas, mapView: MapView, shadow: Boolean) {
        super.draw(c, mapView, shadow)

        val projection = mapView.projection
        val screenPoint = projection.toPixels(geoPoint, null)

        val rectPaint = Paint()
        rectPaint.color = backgroundColor
        rectPaint.isAntiAlias = true

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true

        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent
        val padding = 15f

        val rect = RectF(
            screenPoint.x - textWidth / 2 - padding,
            screenPoint.y - textHeight / 2 - 6,
            screenPoint.x + textWidth / 2 + padding,
            screenPoint.y + textHeight / 2 + padding
        )

        c.drawRoundRect(rect, 25f, 25f, rectPaint)

        c.drawText(text, screenPoint.x.toFloat(), screenPoint.y - textPaint.fontMetrics.ascent / 2, textPaint)
    }
}
