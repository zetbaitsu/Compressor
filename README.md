Compressor
======
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Compressor-blue.svg?style=flat)](http://android-arsenal.com/details/1/3758)
[![Build Status](https://travis-ci.org/zetbaitsu/Compressor.svg?branch=master)](https://travis-ci.org/zetbaitsu/Compressor)
[![codecov](https://codecov.io/gh/zetbaitsu/Compressor/branch/master/graph/badge.svg)](https://codecov.io/gh/zetbaitsu/Compressor)
<p align="center"><img src="https://raw.githubusercontent.com/zetbaitsu/Compressor/master/ss.png" width="50%" /></p>
Compressor is a lightweight and powerful android image compression library. Compressor will allow you to compress large photos into smaller sized photos with very less or negligible loss in quality of the image.

# Gradle
```groovy
dependencies {
    implementation 'id.zelory:compressor:3.0.1'
}
```
# Let's compress the image size!
#### Compress Image File
```kotlin
val compressedImageFile = Compressor.compress(context, actualImageFile)
```
#### Compress Image File to specific destination
```kotlin
val compressedImageFile = Compressor.compress(context, actualImageFile) {
    default()
    destination(myFile)
}
```
### I want custom Compressor!
#### Using default constraint and custom partial of it
```kotlin
val compressedImageFile = Compressor.compress(context, actualImageFile) {
    default(width = 640, format = Bitmap.CompressFormat.WEBP)
}
```
#### Full custom constraint
```kotlin
val compressedImageFile = Compressor.compress(context, actualImageFile) {
    resolution(1280, 720)
    quality(80)
    format(Bitmap.CompressFormat.WEBP)
    size(2_097_152) // 2 MB
}
```
#### Using your own custom constraint
```kotlin
class MyLowerCaseNameConstraint: Constraint {
    override fun isSatisfied(imageFile: File): Boolean {
        return imageFile.name.all { it.isLowerCase() }
    }

    override fun satisfy(imageFile: File): File {
        val destination = File(imageFile.parent, imageFile.name.toLowerCase())
        imageFile.renameTo(destination)
        return destination
    }
}

val compressedImageFile = Compressor.compress(context, actualImageFile) {
    constraint(MyLowerCaseNameConstraint()) // your own constraint
    quality(80) // combine with compressor constraint
    format(Bitmap.CompressFormat.WEBP)
}
```
#### You can create your own extension too
```kotlin
fun Compression.lowerCaseName() {
    constraint(MyLowerCaseNameConstraint())
}

val compressedImageFile = Compressor.compress(context, actualImageFile) {
    lowerCaseName() // your own extension
    quality(80) // combine with compressor constraint
    format(Bitmap.CompressFormat.WEBP)
}
```

### Compressor now is using Kotlin coroutines!
#### Calling Compressor should be done from coroutines scope
```kotlin
// e.g calling from activity lifecycle scope
lifecycleScope.launch {
    val compressedImageFile = Compressor.compress(context, actualImageFile)
}

// calling from global scope
GlobalScope.launch {
    val compressedImageFile = Compressor.compress(context, actualImageFile)
}
```
#### Run Compressor in main thread
```kotlin
val compressedImageFile = Compressor.compress(context, actualImageFile, Dispatchers.Main)
```

### Old version
Please read this [readme](https://github.com/zetbaitsu/Compressor/blob/master/README_v2.md)

License
-------
    Copyright (c) 2016 Zetra.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
