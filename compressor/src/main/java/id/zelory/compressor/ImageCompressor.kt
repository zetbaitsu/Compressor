package id.zelory.compressor

import android.content.Context
import id.zelory.compressor.constraint.Compression
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * Created on : April 12, 2020
 * @author     : jeziellago
 * Name       : Jeziel Lago
 * GitHub     : https://github.com/jeziellago
 */
class ImageCompressor private constructor(private val context: Context) {

    private var compressionPatch: Compression.() -> Unit = { default() }
    private var coroutineContext: CoroutineContext = Dispatchers.IO
    private var coroutineScope: CoroutineScope? = null
    private var outputImageFile: File? = null

    /***
     * @param patch is a block of configuration using [Compression] types.
     * e.g.
     * ImageCompressor.with(context)
     *     .applyCompressionWith{
     *         resolution(1280, 720)
     *         quality(80)
     *         format(Bitmap.CompressFormat.WEBP)
     *         ...
     *     }
     */
    fun applyCompressionWith(patch: Compression.() -> Unit) = apply { compressionPatch = patch }

    /***
     * @param coroutineCtx [CoroutineContext] where compressor will run.
     * Default: Dispatchers.IO
     * @see [Dispatchers]
     */
    fun launchOn(coroutineCtx: CoroutineContext) = apply { coroutineContext = coroutineCtx }

    /***
     * @param scope [CoroutineScope] required to launch the compression task.
     * e.g: lifecycleScope, viewModelScope, customCoroutineScope, etc.
     */
    fun observeOn(scope: CoroutineScope) = apply { coroutineScope = scope }

    /***
     * @param output [File] expected to save compressed image.
     * It is an optional param.
     */
    fun saveOn(output: File) = apply {
        outputImageFile = output
    }

    /***
     * @param inputImageFile [File]
     * @param onFailure, (optional param), receive a [Throwable] on failure cases.
     * @param onSuccess, block to receive output compressed image file.
     *
     * @throws [IllegalArgumentException] if `coroutineScope` is not received from `observeOn` method.
     *
     * @see [Compressor]
     */
    fun compress(
            inputImageFile: File,
            onFailure: ((Throwable) -> Unit)? = null,
            onSuccess: (outputImage: File) -> Unit
    ) {
        coroutineScope?.launch {
            try {
                val compressionOptions = outputImageFile?.let {
                    fun Compression.() {
                        destination(it)
                        compressionPatch.invoke(this)
                    }
                } ?: compressionPatch

                val result = Compressor.compress(
                        context,
                        inputImageFile,
                        coroutineContext,
                        compressionOptions
                )
                onSuccess(result)
            } catch (e: Throwable) {
                onFailure?.invoke(e)
            }
        } ?: throw IllegalArgumentException("CoroutineScope is required.\nSee `observeOn` method.")
    }

    companion object Creator {
        /***
         * @param context [Context], required to create ImageCompressor
         * @return [ImageCompressor]
         */
        fun with(context: Context) = ImageCompressor(context)
    }
}