package id.zelory.compressor.constraint

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.mockk
import org.junit.Test

/**
 * Created on : January 25, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CompressionTest {
    @Test
    fun `add constraint should save it to constraint list`() {
        // Given
        val compression = Compression()

        // When
        compression.constraint(mockk())
        compression.constraint(mockk())

        // Then
        assertThat(compression.constraints.size, equalTo(2))
    }
}