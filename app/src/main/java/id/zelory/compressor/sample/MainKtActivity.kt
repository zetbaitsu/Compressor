package id.zelory.compressor.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.WEBP
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import id.zelory.compressor.Compressor
import id.zelory.compressor.CompressorKt
import id.zelory.compressor.FileUtil
import id.zelory.compressor.compressToBitmap
import id.zelory.compressor.compressToFile
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.Random

/**
 * Created on   : 15/01/17
 * Author       : muhrifqii
 * Name         : Muhammad Rifqi Fatchurrahman Putra Danar
 * Github       : https://github.com/muhrifqii
 * LinkedIn     : https://linkedin.com/in/muhrifqii
 */
class MainKtActivity : AppCompatActivity() {
  private val PICK_IMAGE_REQUEST = 1

  private lateinit var actualImageView: ImageView
  private lateinit var compressedImageView: ImageView
  private lateinit var actualSizeTextView: TextView
  private lateinit var compressedSizeTextView: TextView
  private var actualImage: File? = null
  private var compressedImage: File? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    actualImageView = findViewById(R.id.actual_image) as ImageView
    compressedImageView = findViewById(R.id.compressed_image) as ImageView
    actualSizeTextView = findViewById(R.id.actual_size) as TextView
    compressedSizeTextView = findViewById(R.id.compressed_size) as TextView

    actualImageView.setBackgroundColor(getRandomColor())
    clearImage()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
      if (data == null) {
        showError("Failed to open picture!")
        return
      }
      try {
        actualImage = FileUtil.from(this, data.data)
        actualImageView.setImageBitmap(BitmapFactory.decodeFile(actualImage!!.absolutePath))
        actualSizeTextView.text = String.format("Size : %s",
            getReadableFileSize(actualImage!!.length()))
        clearImage()
      } catch (e: IOException) {
        showError("Failed to read picture data!")
        e.printStackTrace()
      }

    }
  }

  fun chooseImage(view: View) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "image/*"
    startActivityForResult(intent, PICK_IMAGE_REQUEST)
  }

  fun compressImage(view: View) {
    if (actualImage == null) {
      showError("Please choose an image!")
    } else {

      // Compress image in main thread
//      compressedImage = actualImage!!.compressToFile(this)
//      setCompressedImage()

      // Compress image to bitmap in main thread
//      compressedImageView.setImageBitmap(actualImage!!.compressToBitmap(this))

      // Compress image using RxJava in background thread

      CompressorKt.getDefault(this)
          .compressToFileAsObservable(actualImage)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({
            compressedImage = it
            setCompressedImage()
          }) { showError(it.message) }
    }
  }

  fun customCompressImage(view: View) {
    if (actualImage == null) {
      showError("Please choose an image!")
    } else {
      // Compress image in main thread using custom Compressor
      compressedImage = CompressorKt.create(this) {
        maxWidth { 640f }
        maxHeight { 480f }
        quality { 75 }
        compressFormat { WEBP }
      }.compressToFile(actualImage)
      setCompressedImage()

      Compressor.Builder(this).setMaxWidth(640f).build().compressToFile(actualImage)

      // Compress image using RxJava in background thread with custom Compressor
//      CompressorKt.create(this) {
//        maxWidth { 640f }
//        maxHeight { 480f }
//        quality { 75 }
//        compressFormat { WEBP }
//      }.compressToFileAsObservable(actualImage)
//          .subscribeOn(Schedulers.io())
//          .observeOn(AndroidSchedulers.mainThread())
//          .subscribe ({
//            compressedImage = it
//            setCompressedImage()
//          },{
//            showError(it.message)
//          })
    }
  }

  private fun setCompressedImage() {
    compressedImageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage!!.absolutePath))
    compressedSizeTextView.text =
        "Size : %s".format(getReadableFileSize(compressedImage!!.length()))

    Toast.makeText(this, "Compressed image save in " + compressedImage!!.path,
        Toast.LENGTH_LONG).show()
    Log.d("Compressor", "Compressed image save in " + compressedImage!!.path)
  }

  private fun clearImage() {
    actualImageView.setBackgroundColor(getRandomColor())
    compressedImageView.setImageDrawable(null)
    compressedImageView.setBackgroundColor(getRandomColor())
    compressedSizeTextView.text = "Size : -"
  }

  fun showError(errorMessage: String?) {
    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
  }

  private fun getRandomColor(): Int {
    val rand = Random()
    return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))
  }

  fun getReadableFileSize(size: Long): String {
    if (size <= 0) {
      return "0"
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
  }
}