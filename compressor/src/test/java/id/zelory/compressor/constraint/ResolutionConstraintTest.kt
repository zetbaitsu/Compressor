package id.zelory.compressor.constraint

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import id.zelory.compressor.calculateInSampleSize
import id.zelory.compressor.decodeSampledBitmapFromFile
import id.zelory.compressor.determineImageRotation
import id.zelory.compressor.overWrite
import io.mockk.every
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
class ResolutionConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when sampled size is greater than 1, constraint should not satisfied`() {
        // Given
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any(), any()) } returns mockk()
        mockkStatic("id.zelory.compressor.UtilKt")
        every { calculateInSampleSize(any(), any(), any()) } returns 2

        val constraint = ResolutionConstraint(100, 100)

        // When + Then
        assertThat(constraint.isSatisfied(mockk(relaxed = true)), equalTo(false))
    }

    @Test
    fun `when sampled size is equal 1, constraint should satisfied`() {
        // Given
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any(), any()) } returns mockk()
        mockkStatic("id.zelory.compressor.UtilKt")
        every { calculateInSampleSize(any(), any(), any()) } returns 1

        val constraint = ResolutionConstraint(100, 100)

        // When + Then
        assertThat(constraint.isSatisfied(mockk(relaxed = true)), equalTo(true))
    }

    @Test
    fun `when trying satisfy constraint, it should subsampling image and overwrite file`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")

        val sampledBitmap = mockk<Bitmap>(relaxed = true)
        every { decodeSampledBitmapFromFile(any(), any(), any()) } returns sampledBitmap

        val rotatedBitmap = mockk<Bitmap>(relaxed = true)
        every { determineImageRotation(any(), any()) } returns rotatedBitmap

        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val imageFile = mockk<File>(relaxed = true)
        val (width, height) = (1280 to 720)
        val constraint = ResolutionConstraint(width, height)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify {
            decodeSampledBitmapFromFile(imageFile, width, height)
            determineImageRotation(imageFile, sampledBitmap)
            overWrite(imageFile, rotatedBitmap, any(), any())
        }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.resolution(100, 100)

        // Then
        assertThat(compression.constraints.first(), isA<ResolutionConstraint>())
    }
}