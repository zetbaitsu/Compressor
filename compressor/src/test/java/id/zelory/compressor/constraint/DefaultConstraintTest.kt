package id.zelory.compressor.constraint

import android.graphics.Bitmap
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
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
class DefaultConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when satisfy function not yet invoked, constraint should not satisfied`() {
        // Given
        val constraint = DefaultConstraint()

        // When + Then
        assertThat(constraint.isSatisfied(mockk()), equalTo(false))
    }

    @Test
    fun `when satisfy function is invoked, constraint should satisfied`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { decodeSampledBitmapFromFile(any(), any(), any()) } returns mockk(relaxed = true)
        every { determineImageRotation(any(), any()) } returns mockk(relaxed = true)
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val constraint = DefaultConstraint()

        // When
        constraint.satisfy(mockk(relaxed = true))

        // Then
        assertThat(constraint.isSatisfied(mockk()), equalTo(true))
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
        val format = Bitmap.CompressFormat.JPEG
        val quality = 80
        val constraint = DefaultConstraint(width, height, format, quality)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify {
            decodeSampledBitmapFromFile(imageFile, width, height)
            determineImageRotation(imageFile, sampledBitmap)
            overWrite(imageFile, rotatedBitmap, format, quality)
        }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.default()

        // Then
        assertThat(compression.constraints.first(), isA<DefaultConstraint>())
    }
}