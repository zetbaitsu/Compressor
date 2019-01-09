Compressor
======
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Compressor-blue.svg?style=flat)](http://android-arsenal.com/details/1/3758)
<p align="center"><img src="https://raw.githubusercontent.com/zetbaitsu/Compressor/master/ss.png" width="50%" /></p>
Compressor is a lightweight and powerful android image compression library. Compressor will allow you to compress large photos into smaller sized photos with very less or negligible loss in quality of the image.

# Gradle
```groovy
dependencies {
    implementation 'id.zelory:compressor:2.1.1'
}
```
# Let's compress the image size!
#### Compress Image File
```java
compressedImageFile = new Compressor(this).compressToFile(actualImageFile);
```
#### Compress Image File to Bitmap
```java
compressedImageBitmap = new Compressor(this).compressToBitmap(actualImageFile);
```
### I want custom Compressor!
```java
compressedImage = new Compressor(this)
            .setMaxWidth(640)
            .setMaxHeight(480)
            .setQuality(75)
            .setCompressFormat(Bitmap.CompressFormat.WEBP)
            .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES).getAbsolutePath())
            .compressToFile(actualImage);
```
### Stay cool compress image asynchronously with RxJava!
```java
new Compressor(this)
        .compressToFileAsFlowable(actualImage)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) {
                compressedImage = file;
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                showError(throwable.getMessage());
            }
        });
```

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
