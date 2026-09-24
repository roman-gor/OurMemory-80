package com.gorman.ourmemoryapp.ui.common.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.gorman.ourmemoryapp.ui.theme.MemoryPaper
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.yandex.runtime.image.ImageProvider

class NumberImageProvider(
    private val context: Context,
    private val number: Int
) : ImageProvider() {

    override fun getId() = "$ID_PREFIX$number"

    override fun getImage(): Bitmap {
        val density = context.resources.displayMetrics.density
        val radius = RADIUS_DP * density
        val strokeWidth = STROKE_DP * density
        val diameter = (radius * 2).toInt()
        val bitmap = createBitmap(diameter, diameter)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MemoryRed.toArgb() }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MemoryPaper.toArgb()
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MemoryPaper.toArgb()
            textSize = TEXT_SIZE_DP * density
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawCircle(radius, radius, radius - strokeWidth / 2, fillPaint)
        canvas.drawCircle(radius, radius, radius - strokeWidth / 2, strokePaint)
        val baseline = radius - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(number.toString(), radius, baseline, textPaint)
        return bitmap
    }

    companion object {
        private const val ID_PREFIX = "number_marker_"
        private const val RADIUS_DP = 20f
        private const val STROKE_DP = 2f
        private const val TEXT_SIZE_DP = 14f
    }
}
