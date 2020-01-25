package id.zelory.compressor.constraint

import android.graphics.Bitmap
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import id.zelory.compressor.loadBitmap
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
class FormatConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when extension is not equal with format, constraint should not satisfied`() {
        // Given
        val constraint = FormatConstraint(Bitmap.CompressFormat.JPEG)

        // When + Then
        assertThat(constraint.isSatisfied(File("a_file.webp")), equalTo(false))
    }

    @Test
    fun `when extension is equal with format, constraint should satisfied`() {
        // Given
        val constraint = FormatConstraint(Bitmap.CompressFormat.WEBP)

        // When + Then
        assertThat(constraint.isSatisfied(File("a_file.webp")), equalTo(true))
    }

    @Test
    fun `when trying satisfy constraint, it should save image with selected format`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val imageFile = mockk<File>()
        val format = Bitmap.CompressFormat.PNG
        val constraint = FormatConstraint(format)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify { overWrite(imageFile, any(), format, any()) }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.format(Bitmap.CompressFormat.PNG)

        // Then
        assertThat(compression.constraints.first(), isA<FormatConstraint>())
    }
}