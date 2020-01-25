package id.zelory.compressor.constraint

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
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
class DestinationConstraintTest {

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `when destination is not equal with image file, constraint should not satisfied`() {
        // Given
        val constraint = DestinationConstraint(File("a_file.webp"))

        // When + Then
        assertThat(constraint.isSatisfied(File("another_file.png")), equalTo(false))
    }

    @Test
    fun `when destination is equal with image file, constraint should satisfied`() {
        // Given
        val constraint = DestinationConstraint(File("a_file.jpg"))

        // When + Then
        assertThat(constraint.isSatisfied(File("a_file.jpg")), equalTo(true))
    }

    @Test
    fun `when trying satisfy constraint, it should copy image to destination`() {
        // Given
        mockkStatic("kotlin.io.FilesKt__UtilsKt")
        every { any<File>().copyTo(any(), any(), any()) } returns mockk(relaxed = true)

        val imageFile = File("source.jpg")
        val destination = File("destination.jpg")
        val constraint = DestinationConstraint(destination)

        // When
        constraint.satisfy(imageFile)

        // Then
        verify { imageFile.copyTo(destination, true, any()) }
    }

    @Test
    fun `verify extension`() {
        // Given
        val compression = Compression()

        // When
        compression.destination(mockk())

        // Then
        assertThat(compression.constraints.first(), isA<DestinationConstraint>())
    }
}