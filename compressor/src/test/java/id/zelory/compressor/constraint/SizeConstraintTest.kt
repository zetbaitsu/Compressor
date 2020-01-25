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
class SizeConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when file size greater than max file size, constraint should not satisfied`() {
        // Given
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.length() } returns 2000
        val constraint = SizeConstraint(1000)

        // When + Then
        assertThat(constraint.isSatisfied(imageFile), equalTo(false))
    }

    @Test
    fun `when file size equal to max file size, constraint should satisfied`() {
        // Given
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.length() } returns 1000
        val constraint = SizeConstraint(1000)

        // When + Then
        assertThat(constraint.isSatisfied(imageFile), equalTo(true))
    }

    @Test
    fun `when file size less than max file size, constraint should satisfied`() {
        // Given
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.length() } returns 900
        val constraint = SizeConstraint(1000)

        // When + Then
        assertThat(constraint.isSatisfied(imageFile), equalTo(true))
    }

    @Test
    fun `when iteration less than max iteration, constraint should not satisfied`() {
        // Given
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.length() } returns 2000

        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val constraint = SizeConstraint(1000, maxIteration = 5)

        // When
        constraint.satisfy(imageFile)

        // Then
        assertThat(constraint.isSatisfied(imageFile), equalTo(false))
    }

    @Test
    fun `when iteration equal to max iteration, constraint should satisfied`() {
        // Given
        val imageFile = mockk<File>(relaxed = true)
        every { imageFile.length() } returns 2000

        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val constraint = SizeConstraint(1000, maxIteration = 5)

        // When
        repeat(5) {
            constraint.satisfy(imageFile)
        }

        // Then
        assertThat(constraint.isSatisfied(imageFile), equalTo(true))
    }

    @Test
    fun `when trying satisfy constraint, it should save image with calculated quality`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val imageFile = mockk<File>(relaxed = true)
        val stepSize = 10
        val quality = 100 - stepSize
        val constraint = SizeConstraint(200, stepSize = stepSize)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify { overWrite(imageFile, any(), any(), quality) }
    }

    @Test
    fun `when trying satisfy constraint but calculated quality less than min quality, it should use min quality`() {
        // Given
        mockkStatic("id.zelory.compressor.UtilKt")
        every { loadBitmap(any()) } returns mockk()
        every { overWrite(any(), any(), any(), any()) } returns mockk()

        val imageFile = mockk<File>(relaxed = true)
        val stepSize = 50
        val minQuality = 80
        val constraint = SizeConstraint(200, stepSize, minQuality = minQuality)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify { overWrite(imageFile, any(), any(), minQuality) }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.size(9000)

        // Then
        assertThat(compression.constraints.first(), isA<SizeConstraint>())
    }
}