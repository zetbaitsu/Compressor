package id.zelory.compressor

import android.graphics.Bitmap
import id.zelory.compressor.constraint.DefaultConstraint
import id.zelory.compressor.constraint.FormatConstraint
import id.zelory.compressor.constraint.QualityConstraint
import id.zelory.compressor.constraint.ResolutionConstraint
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created on : January 25, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CompressorTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("id.zelory.compressor.UtilKt")
        every { copyToCache(any(), any()) } returns mockk(relaxed = true)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `compress with default specs should execute default constraint`() = testDispatcher.runBlockingTest {
        // Given
        mockkConstructor(DefaultConstraint::class)
        var executedConstraint = 0
        every { anyConstructed<DefaultConstraint>().isSatisfied(any()) } answers {
            executedConstraint > 0
        }
        every { anyConstructed<DefaultConstraint>().satisfy(any()) } answers {
            executedConstraint++
            mockk(relaxed = true)
        }

        // When
        Compressor.compress(mockk(relaxed = true), mockk(relaxed = true), testDispatcher)

        // Then
        verify {
            anyConstructed<DefaultConstraint>().isSatisfied(any())
            anyConstructed<DefaultConstraint>().satisfy(any())
        }
    }

    @Test
    fun `compress with custom specs should execute all constraint provided`() = testDispatcher.runBlockingTest {
        // Given
        mockkConstructor(ResolutionConstraint::class)
        mockkConstructor(QualityConstraint::class)
        mockkConstructor(FormatConstraint::class)

        var executedConstraint = 0
        every { anyConstructed<ResolutionConstraint>().isSatisfied(any()) } answers {
            executedConstraint > 0
        }
        every { anyConstructed<ResolutionConstraint>().satisfy(any()) } answers {
            executedConstraint++
            mockk(relaxed = true)
        }

        every { anyConstructed<QualityConstraint>().isSatisfied(any()) } answers {
            executedConstraint > 1
        }
        every { anyConstructed<QualityConstraint>().satisfy(any()) } answers {
            executedConstraint++
            mockk(relaxed = true)
        }

        every { anyConstructed<FormatConstraint>().isSatisfied(any()) } answers {
            executedConstraint > 2
        }
        every { anyConstructed<FormatConstraint>().satisfy(any()) } answers {
            executedConstraint++
            mockk(relaxed = true)
        }

        // When
        Compressor.compress(mockk(relaxed = true), mockk(relaxed = true), testDispatcher) {
            resolution(100, 100)
            quality(75)
            format(Bitmap.CompressFormat.PNG)
        }

        // Then
        verify {
            anyConstructed<ResolutionConstraint>().isSatisfied(any())
            anyConstructed<ResolutionConstraint>().satisfy(any())
            anyConstructed<QualityConstraint>().isSatisfied(any())
            anyConstructed<QualityConstraint>().satisfy(any())
            anyConstructed<FormatConstraint>().isSatisfied(any())
            anyConstructed<FormatConstraint>().satisfy(any())
        }
    }
}