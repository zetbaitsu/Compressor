package id.zelory.compressor.constraint

import java.io.File

/**
 * Created on : January 25, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DestinationConstraint(private val destination: File) : Constraint {
    override fun isSatisfied(imageFile: File): Boolean {
        return imageFile.absolutePath == destination.absolutePath
    }

    override fun satisfy(imageFile: File): File {
        if (!destination.exists()) {
            destination.createNewFile()
        }
        return imageFile.copyTo(destination, true)
    }
}

fun Compression.destination(destination: File) {
    constraint(DestinationConstraint(destination))
}