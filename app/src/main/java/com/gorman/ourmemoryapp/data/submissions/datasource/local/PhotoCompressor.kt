package com.gorman.ourmemoryapp.data.submissions.datasource.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PhotoCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun compress(uri: Uri): ByteArray {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val (width, height) = scaledSize(info.size.width, info.size.height, MAX_SIDE_PX)
            decoder.setTargetSize(width, height)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            bitmap.recycle()
            stream.toByteArray()
        }
    }

    companion object {
        private const val MAX_SIDE_PX = 2048
        private const val JPEG_QUALITY = 85
    }
}
