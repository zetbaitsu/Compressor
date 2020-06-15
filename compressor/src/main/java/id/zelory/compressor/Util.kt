package id.zelory.compressor

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created on : January 24, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
private val separator = File.separator

private fun cachePath(context: Context) = "${context.cacheDir.path}${separator}compressor$separator"

fun File.compressFormat() = when (extension.toLowerCase()) {
    "png" -> Bitmap.CompressFormat.PNG
    "webp" -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}

fun Bitmap.CompressFormat.extension() = when (this) {
    Bitmap.CompressFormat.PNG -> "png"
    Bitmap.CompressFormat.WEBP -> "webp"
    else -> "jpg"
}

fun loadBitmap(imageFile: File) = BitmapFactory.decodeFile(imageFile.absolutePath).run {
    determineImageRotation(imageFile, this)
}

fun decodeSampledBitmapFromFile(imageFile: File, reqWidth: Int, reqHeight: Int): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile.absolutePath, this)

        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        inJustDecodeBounds = false
        BitmapFactory.decodeFile(imageFile.absolutePath, this)
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun determineImageRotation(imageFile: File, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
    val matrix = Matrix()
    when (orientation) {
        6 -> matrix.postRotate(90f)
        3 -> matrix.postRotate(180f)
        8 -> matrix.postRotate(270f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

internal fun copyToCache(context: Context, imageFile: File): File {
    return imageFile.copyTo(File("${cachePath(context)}${imageFile.name}"), true)
}

fun copyToCache(context: Context, srcFileUri: Uri): File {
    val cacheFile = File("${cachePath(context)}${getFileName(context, srcFileUri)}")
    cacheFile.parentFile.mkdirs()
    if (cacheFile.exists()) {
        cacheFile.delete()
    }
    cacheFile.createNewFile()
    cacheFile.deleteOnExit()
    val fd = context.contentResolver.openFileDescriptor(srcFileUri, "r")
    val inputStream = ParcelFileDescriptor.AutoCloseInputStream(fd)
    val outputStream = FileOutputStream(cacheFile)
    inputStream.use {
        outputStream.use {
            inputStream.copyTo(outputStream)
        }
    }
    return cacheFile
}

fun getFileName(context: Context, uri: Uri) : String {
    val resolver = context.contentResolver
    val cursor = resolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME
    ), null, null, null
    )
    cursor.use {
        val nameIndex = it!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            return it.getString(nameIndex)
        } else {
            val prefix = "IMG_" + SimpleDateFormat(
                    "yyyyMMdd_",
                    Locale.getDefault()
            ).format(Date()) + System.nanoTime()
            return when (val fileMimeType = resolver.getType(uri)) {
                "image/jpg", "image/jpeg" -> {
                    "$prefix.jpeg"
                }
                "image/png" -> {
                    "$prefix.png"
                }
                else -> {
                    throw IllegalStateException("$fileMimeType fallback display name not supported")
                }
            }
        }
    }
}

fun overWrite(imageFile: File, bitmap: Bitmap, format: Bitmap.CompressFormat = imageFile.compressFormat(), quality: Int = 100): File {
    val result = if (format == imageFile.compressFormat()) {
        imageFile
    } else {
        File("${imageFile.absolutePath.substringBeforeLast(".")}.${format.extension()}")
    }
    imageFile.delete()
    saveBitmap(bitmap, result, format, quality)
    return result
}

fun saveBitmap(bitmap: Bitmap, destination: File, format: Bitmap.CompressFormat = destination.compressFormat(), quality: Int = 100) {
    destination.parentFile?.mkdirs()
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(destination.absolutePath)
        bitmap.compress(format, quality, fileOutputStream)
    } finally {
        fileOutputStream?.run {
            flush()
            close()
        }
    }
}