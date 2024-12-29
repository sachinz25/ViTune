package app.vitune.android.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.applyCanvas
import app.vitune.android.utils.thumbnail
import coil3.imageLoader
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap

context(Context)
class BitmapProvider(
    private val getBitmapSize: () -> Int,
    private val getColor: (isDark: Boolean) -> Int
) {
    var lastUri: Uri? = null
        private set

    private var lastBitmap: Bitmap? = null
        set(value) {
            field = value
            listener?.invoke(value)
        }
    private var lastIsSystemInDarkMode = false
    private var currentTask: Disposable? = null

    private lateinit var defaultBitmap: Bitmap
    val bitmap get() = lastBitmap ?: defaultBitmap

    private var listener: ((Bitmap?) -> Unit)? = null

    init {
        setDefaultBitmap()
    }

    fun setDefaultBitmap(): Boolean {
        val isSystemInDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (::defaultBitmap.isInitialized && isSystemInDarkMode == lastIsSystemInDarkMode) return false

        lastIsSystemInDarkMode = isSystemInDarkMode

        val size = getBitmapSize()
        defaultBitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        ).applyCanvas {
            drawColor(getColor(isSystemInDarkMode))
        }

        return lastBitmap == null
    }

    fun load(
        uri: Uri?,
        onDone: (Bitmap) -> Unit = {}
    ) {
        if (lastUri == uri) {
            listener?.invoke(lastBitmap)
            return
        }

        currentTask?.dispose()
        lastUri = uri

        if (uri == null) {
            lastBitmap = null
            onDone(bitmap)
            return
        }

        currentTask = applicationContext.imageLoader.enqueue(
            ImageRequest.Builder(applicationContext)
                .data(uri.thumbnail(getBitmapSize()))
                .allowHardware(false)
                .listener(
                    onStart = {
                        Log.d("BitmapProvider", "Image loading started for $uri")
                    },
                    onError = { _, throwable ->
                        Log.e("BitmapProvider", "Image loading failed for $uri", throwable)
                        lastBitmap = null
                        onDone(bitmap)
                    },
                    onSuccess = { _, result ->
                        val bitmap = result.image.toBitmap(width, height)
                        if (bitmap != null && !bitmap.isRecycled) {
                            lastBitmap = bitmap
                            onDone(lastBitmap!!)
                        } else {
                            Log.e("BitmapProvider", "Cannot use a recycled or null Bitmap. Result: $result")
                            lastBitmap = null
                            onDone(bitmap)
                        }
                    }
                )
                .build()
        )
    }

    fun setListener(callback: ((Bitmap?) -> Unit)?) {
        listener = callback
        listener?.invoke(lastBitmap)
    }
}
