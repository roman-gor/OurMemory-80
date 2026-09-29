package com.gorman.ourmemoryapp.ui.common.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.gorman.ourmemoryapp.ui.theme.MemoryPaper
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.ImageProvider

class MarkerImageProvider(private val context: Context) : ImageProvider() {

    override fun getId() = ID

    override fun getImage(): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = WIDTH_DP * density
        val height = HEIGHT_DP * density
        val strokeWidth = STROKE_DP * density
        val center = width / 2
        val radius = center - strokeWidth
        val bitmap = createBitmap(width.toInt(), height.toInt())
        val canvas = Canvas(bitmap)

        val circleBounds = RectF(center - radius, center - radius, center + radius, center + radius)
        val pin = Path().apply {
            arcTo(circleBounds, ARC_START_DEGREES, ARC_SWEEP_DEGREES)
            lineTo(center, height - strokeWidth)
            close()
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryRed.toArgb() }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MemoryPaper.toArgb()
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            this.strokeWidth = strokeWidth
        }
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryPaper.toArgb() }

        canvas.drawPath(pin, fillPaint)
        canvas.drawPath(pin, strokePaint)
        canvas.drawCircle(center, center, radius * HOLE_RATIO, holePaint)
        return bitmap
    }

    companion object {
        private const val ID = "burial_marker"
        private const val WIDTH_DP = 32f
        private const val HEIGHT_DP = 42f
        private const val STROKE_DP = 2f
        private const val HOLE_RATIO = 0.36f
        private const val ARC_START_DEGREES = 144f
        private const val ARC_SWEEP_DEGREES = 252f
        private const val ANCHOR_X = 0.5f
        private const val ANCHOR_Y = 1f

        fun iconStyle(): IconStyle = IconStyle().setAnchor(PointF(ANCHOR_X, ANCHOR_Y))
    }
}
