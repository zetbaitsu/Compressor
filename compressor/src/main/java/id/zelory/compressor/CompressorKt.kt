package id.zelory.compressor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import rx.Observable
import rx.functions.Func0
import java.io.File

/**
 * Created on   : 15/01/17
 * Author       : muhrifqii
 * Name         : Muhammad Rifqi Fatchurrahman Putra Danar
 * Github       : https://github.com/muhrifqii
 * LinkedIn     : https://linkedin.com/in/muhrifqii
 */
class CompressorKt private constructor(private val context: Context) {

  companion object {
    /**
     * Create a custom compressor
     */
    fun create(context: Context, init: Builder.() -> Unit) = Builder(context, init).build()

    /**
     * Get the default compressor
     */
    fun getDefault(context: Context): CompressorKt {
      if (INSTANCE == null) {
        synchronized(CompressorKt::class.java) {
          if (INSTANCE == null) {
            INSTANCE = CompressorKt(context)
          }
        }
      }
      return INSTANCE!!
    }

    @Volatile private var INSTANCE: CompressorKt? = null
  }

  //max width and height values of the compressed image is taken as 612x816
  private var maxWidth = 612.0f
  private var maxHeight = 816.0f
  private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
  private var bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888
  private var quality = 80
  private var destinationDirectoryPath: String = context.cacheDir.path + File.pathSeparator + FileUtil.FILES_PATH
  private var fileNamePrefix: String? = null
  private var fileName: String? = null

  fun compressToFile(file: File?): File {
    return ImageUtil.compressImage(context, Uri.fromFile(file), maxWidth, maxHeight,
        compressFormat, bitmapConfig, quality, destinationDirectoryPath,
        fileNamePrefix, fileName)
  }

  fun compressToBitmap(file: File?): Bitmap {
    return ImageUtil.getScaledBitmap(context, Uri.fromFile(file), maxWidth, maxHeight, bitmapConfig)
  }

  fun compressToFileAsObservable(file: File?): Observable<File> {
    return Observable.defer { Observable.just(compressToFile(file)) }
  }

  fun compressToBitmapAsObservable(file: File?): Observable<Bitmap> {
    return Observable.defer { Observable.just(compressToBitmap(file)) }
  }

  /**
   * Builder pattern in a fancy functional way
   */
  class Builder private constructor() {
    private lateinit var compressor: CompressorKt

    constructor(context: Context, init: Builder.() -> Unit) : this() {
      compressor = CompressorKt(context)
      init()
    }

    fun maxWidth(init: Builder.() -> Float) = apply { compressor.maxWidth = init() }

    fun maxHeight(init: Builder.() -> Float) = apply { compressor.maxHeight = init() }

    fun compressFormat(init: Builder.() -> Bitmap.CompressFormat) =
        apply { compressor.compressFormat = init() }

    fun bitmapConfig(init: Builder.() -> Bitmap.Config) = apply { compressor.bitmapConfig = init() }

    fun quality(init: Builder.() -> Int) = apply { compressor.quality = init() }

    fun destinationDirectoryPath(init: Builder.() -> String) =
        apply { compressor.destinationDirectoryPath = init() }

    fun fileNamePrefix(init: Builder.() -> String) = apply { compressor.fileNamePrefix = init() }

    fun fileName(init: Builder.() -> String) = apply { compressor.fileName = init() }

    fun build(): CompressorKt {
      return compressor
    }
  }
}