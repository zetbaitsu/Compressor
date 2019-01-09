package id.zelory.compressor

import android.content.Context
import android.graphics.Bitmap
import rx.Observable
import java.io.File

/**
 * Created on   : 15/01/17
 * Author       : muhrifqii
 * Name         : Muhammad Rifqi Fatchurrahman Putra Danar
 * Github       : https://github.com/muhrifqii
 * LinkedIn     : https://linkedin.com/in/muhrifqii
 */

inline fun File.compressToFile(context: Context): File {
  return CompressorKt.getDefault(context).compressToFile(this)
}

inline fun File.compressToBitmap(context: Context): Bitmap {
  return CompressorKt.getDefault(context).compressToBitmap(this)
}

inline fun File.compressToFileAsObservable(context: Context): Observable<File> {
  return CompressorKt.getDefault(context).compressToFileAsObservable(this)
}

inline fun File.compressToBitmapAsObservable(context: Context): Observable<Bitmap> {
  return CompressorKt.getDefault(context).compressToBitmapAsObservable(this)
}

