package com.gorman.ourmemoryapp.data.contentcheck.datasource.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import com.gorman.ourmemoryapp.data.contentcheck.model.NsfwScores
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NsfwImageClassifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val interpreter by lazy { Interpreter(loadModel(), Interpreter.Options().setNumThreads(THREADS)) }
    private val mutex = Mutex()

    suspend fun classify(uri: Uri) = mutex.withLock {
        val input = toInputBuffer(decode(uri))
        val output = Array(1) { FloatArray(CLASS_COUNT) }
        interpreter.run(input, output)
        NsfwScores(
            hentai = output[0][HENTAI_INDEX],
            porn = output[0][PORN_INDEX],
            sexy = output[0][SEXY_INDEX]
        )
    }

    private fun decode(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetSize(INPUT_SIZE, INPUT_SIZE)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun toInputBuffer(bitmap: Bitmap): ByteBuffer {
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        bitmap.recycle()
        return ByteBuffer.allocateDirect(pixels.size * CHANNELS * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .apply {
                pixels.forEach { pixel ->
                    putFloat(Color.red(pixel) / MAX_CHANNEL_VALUE)
                    putFloat(Color.green(pixel) / MAX_CHANNEL_VALUE)
                    putFloat(Color.blue(pixel) / MAX_CHANNEL_VALUE)
                }
                rewind()
            }
    }

    private fun loadModel(): MappedByteBuffer = context.assets.openFd(MODEL_FILE).use { descriptor ->
        FileInputStream(descriptor.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            descriptor.startOffset,
            descriptor.declaredLength
        )
    }

    companion object {
        private const val MODEL_FILE = "nsfw_mobilenet_v2_224.tflite"
        private const val INPUT_SIZE = 224
        private const val CHANNELS = 3
        private const val CLASS_COUNT = 5
        private const val HENTAI_INDEX = 1
        private const val PORN_INDEX = 3
        private const val SEXY_INDEX = 4
        private const val THREADS = 2
        private const val MAX_CHANNEL_VALUE = 255f
    }
}
