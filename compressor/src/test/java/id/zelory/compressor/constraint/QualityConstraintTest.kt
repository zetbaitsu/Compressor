package id.zelory.compressor.constraint

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
class QualityConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when satisfy function not yet invoked, constraint should not satisfied`() {
        // Given
        val constraint = QualityConstraint(mockk(relaxed = true))

        // When + Then
        assertThat(constraint.isSatisfied(mockk()), equalTo(false))
    }

    @Test
    fun `when satisfy function is invoked, constraint should satisfied`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val constraint = QualityConstraint(mockk(relaxed = true))

        // When
        constraint.satisfy(mockk(relaxed = true))

        // Then
        assertThat(constraint.isSatisfied(mockk()), equalTo(true))
    }

    @Test
    fun `when trying satisfy constraint, it should save image with provided quality`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val imageFile = mockk<File>(relaxed = true)
        val quality = 75
        val constraint = QualityConstraint(quality)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify { overWrite(imageFile, any(), any(), quality) }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.quality(90)

        // Then
        assertThat(compression.constraints.first(), isA<QualityConstraint>())
    }
}