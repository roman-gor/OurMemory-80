package com.gorman.ourmemoryapp.ui.map.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.gorman.ourmemoryapp.ui.theme.MemoryPaper
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.yandex.runtime.image.ImageProvider

class UserLocationImageProvider(private val context: Context) : ImageProvider() {

    override fun getId() = ID

    override fun getImage(): Bitmap {
        val density = context.resources.displayMetrics.density
        val outerRadius = OUTER_RADIUS_DP * density
        val size = (outerRadius * 2).toInt()
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryPaper.toArgb() }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryRed.toArgb() }
        canvas.drawCircle(outerRadius, outerRadius, outerRadius, ringPaint)
        canvas.drawCircle(outerRadius, outerRadius, INNER_RADIUS_DP * density, dotPaint)
        return bitmap
    }

    companion object {
        private const val ID = "user_location_dot"
        private const val OUTER_RADIUS_DP = 11f
        private const val INNER_RADIUS_DP = 7.5f
    }
}
