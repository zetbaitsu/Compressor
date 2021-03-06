package id.zelory.compressor

import android.content.Context
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * Created on : January 22, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
object Compressor {
    suspend fun compress(
            context: Context,
            imageFile: File,
            coroutineContext: CoroutineContext = Dispatchers.IO,
            compressionPatch: Compression.() -> Unit = { default() }
    ) = withContext(coroutineContext) {
        val compression = Compression().apply(compressionPatch)
        val mergedConstraints = mergeConstraints(compression.constraints, imageFile)
        val destinationFile = getDestinationFile(mergedConstraints)
        var result = copyToCache(context, imageFile, destinationFile)
        mergedConstraints.forEach { constraint ->
            while (constraint.isSatisfied(result).not()) {
                result = constraint.satisfy(result)
            }
        }
        return@withContext result
    }

    private fun getDestinationFile(constraints: MutableList<Constraint>): File? {
        constraints.forEach {
            if (it is DestinationConstraint) {
                constraints.remove(it)
                return it.getDestination()
            }
        }
        return null
    }

    /**
     * need merge constraints for improving compress speed
     * 1）merge multi ResolutionConstraints and QualityConstraints and FormatConstraints
     * (without DestinationConstraint and SizeConstraint) into only one DefaultConstraint.
     * 2）SizeConstraint must be moved to the last pos since it can reduce 
     *    the number of compress times in this type of SizeConstraint.
     */
    private fun mergeConstraints(constraints: MutableList<Constraint>, imageFile: File): MutableList<Constraint> {
        val size = getImageDimension(imageFile)
        var (width: Int, height: Int) = size.run { get(0) to get(1) }
        var quality = 100
        var format = imageFile.compressFormat()
        val resConstraints = mutableListOf<Constraint>()
        var sizeConstraint: SizeConstraint? = null
        val visited = hashSetOf<Class<Constraint>>()

        for (i in constraints.size - 1 downTo 0) {
            when (val it = constraints[i]) {
                is ResolutionConstraint -> {
                    width = it.getWidth()
                    height = it.getHeight()
                }
                is QualityConstraint -> {
                    quality = it.getQuality()
                }
                is FormatConstraint -> {
                    format = it.getFormat()
                }
                is DefaultConstraint -> {
                    width = it.getWidth()
                    height = it.getHeight()
                    quality = it.getQuality()
                    format = it.getFormat()
                }
                is SizeConstraint -> sizeConstraint = it
                else -> run {
                    // DestinationConstraint or SizeConstraint
                    if (visited.contains(it.javaClass)) {
                        return@run
                    }
                    visited.add(it.javaClass)
                    resConstraints.add(it)
                }
            }
        }

        resConstraints.add(DefaultConstraint(width, height, format, quality))
        sizeConstraint?.let {
            resConstraints.add(it)
        }
        return resConstraints
    }
}