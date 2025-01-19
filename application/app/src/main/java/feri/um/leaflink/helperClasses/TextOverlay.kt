package feri.um.leaflink.helperClasses

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import org.osmdroid.util.GeoPoint

class TextOverlay(private val text: String, private val geoPoint: GeoPoint, private val startColor: Int, private val endColor: Int) : Overlay() {

    override fun draw(c: Canvas, mapView: MapView, shadow: Boolean) {
        super.draw(c, mapView, shadow)

        val projection = mapView.projection
        val screenPoint = projection.toPixels(geoPoint, null)

        // Paint for the text
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        paint.setShadowLayer(3f, 2f, 2f, Color.GRAY) // Soft shadow for text

        // Paint for the gradient background
        val rectPaint = Paint()
        rectPaint.isAntiAlias = true
        rectPaint.setShadowLayer(8f, 4f, 4f, Color.DKGRAY) // Softer shadow for background

        // Create a linear gradient background
        val gradient = LinearGradient(
            0f, 0f, 0f, 200f,
            startColor, endColor, Shader.TileMode.CLAMP
        )
        rectPaint.shader = gradient

        // Calculate the width and height of the text
        val textWidth = paint.measureText(text)
        val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent

        // Padding around the text
        val padding = 15f

        // Create the rounded rectangle around the text
        val rect = RectF(
            screenPoint.x - textWidth / 2 - padding,
            screenPoint.y - textHeight / 2 - padding,
            screenPoint.x + textWidth / 2 + padding,
            screenPoint.y + textHeight / 2 + padding
        )

        // Draw the rounded rectangle with the gradient background
        c.drawRoundRect(rect, 25f, 25f, rectPaint) // Rounded corners with 25f radius

        // Draw the text over the rounded rectangle
        c.drawText(text, screenPoint.x.toFloat(), screenPoint.y.toFloat(), paint)
    }
}
