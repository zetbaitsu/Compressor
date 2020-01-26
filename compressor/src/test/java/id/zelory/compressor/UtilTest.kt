package id.zelory.compressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Test
import java.io.File

/**
 * Created on : January 25, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UtilTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `get compress format from file should return correct format`() {
        assertThat(File("a_file.png").compressFormat(), equalTo(Bitmap.CompressFormat.PNG))
        assertThat(File("a_file.webp").compressFormat(), equalTo(Bitmap.CompressFormat.WEBP))
        assertThat(File("a_file.jpg").compressFormat(), equalTo(Bitmap.CompressFormat.JPEG))
        assertThat(File("a_file.jpeg").compressFormat(), equalTo(Bitmap.CompressFormat.JPEG))
    }

    @Test
    fun `get extension from compress format should return correct extension`() {
        assertThat(Bitmap.CompressFormat.PNG.extension(), equalTo("png"))
        assertThat(Bitmap.CompressFormat.WEBP.extension(), equalTo("webp"))
        assertThat(Bitmap.CompressFormat.JPEG.extension(), equalTo("jpg"))
    }

    @Test
    fun `load bitmap should determine image rotation`() {
        // Given
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockk()
        mockkStatic("id.zelory.compressor.UtilKt")
        every { determineImageRotation(any(), any()) } returns mockk()

        // When
        loadBitmap(mockk(relaxed = true))

        // Then
        verify { determineImageRotation(any(), any()) }
    }

    @Test
    fun `decode sampled bitmap should decode with subsampling`() {
        // Given
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any(), any()) } returns mockk()
        mockkStatic("id.zelory.compressor.UtilKt")
        val inSampleSize = 2
        every { calculateInSampleSize(any(), any(), any()) } returns inSampleSize

        // When
        decodeSampledBitmapFromFile(mockk(relaxed = true), 100, 100)

        // Then
        verify {
            BitmapFactory.decodeFile(any(), match {
                it.inSampleSize == inSampleSize
            })
        }
    }

    @Test
    fun `when request half of resolution, it should return 2 in sample size`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 400, 400), equalTo(2))
    }

    @Test
    fun `when resolution requested greater than actual resolution, it should return 1 in sample size`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 1000, 1000), equalTo(1))
    }

    @Test
    fun `when partial resolution requested greater than actual resolution, it should return 1 in sample size`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 1000, 500), equalTo(1))
    }

    @Test
    fun `when resolution requested less than actual resolution but greater than of half it, it should return 1 in sample size`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 500, 500), equalTo(1))
    }

    @Test
    fun `when request 25% of resolution, it should return 4 in sample size`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 200, 200), equalTo(4))
    }

    @Test
    fun `when width 25% and height 50% of resolution, it should return min sample size (height)`() {
        // Given
        val options = BitmapFactory.Options().apply {
            outWidth = 800
            outHeight = 800
        }

        // When + Then
        assertThat(calculateInSampleSize(options, 200, 400), equalTo(2))
    }

    @Test
    fun `copy to cache should copy file to right folder`() {
        // Given
        val context = mockk<Context>(relaxed = true)
        every { context.cacheDir.path } returns "folder/"

        mockkStatic("kotlin.io.FilesKt__UtilsKt")
        every { any<File>().copyTo(any(), any(), any()) } returns mockk(relaxed = true)

        val source = File("image.jpg")

        // When
        copyToCache(context, File("image.jpg"))

        // Then
        verify {
            source.copyTo(File("folder/compressor/image.jpg"), true, any())
        }
    }

    @Test
    fun `overwrite should delete old file and save new bitmap`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { saveBitmap(any(), any(), any(), any()) } just Runs

        val imageFile = mockk<File>(relaxed = true)
        val bitmap = mockk<Bitmap>(relaxed = true)

        // When
        overWrite(imageFile, bitmap)

        // Then
        verify {
            imageFile.delete()
            saveBitmap(bitmap, imageFile, any(), any())
        }
    }

    @Test
    fun `overwrite with different format should save image with new format extension`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { saveBitmap(any(), any(), any(), any()) } just Runs

        val imageFile = File("image.jpg")
        val bitmap = mockk<Bitmap>(relaxed = true)

        // When
        val result = overWrite(imageFile, bitmap, Bitmap.CompressFormat.PNG)

        // Then
        assertThat(result.extension, equalTo("png"))
    }
}