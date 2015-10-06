Toss Android Client
==============


Tech Stack
---------

Required
---------

1. Install Java8 JDK
2. Install Java6 or 7 JDK
3. Install Android SDK 14
4. Install Android SDK 23+
5. Install Android Support Library
6. Android Studio 1.3+

### gradle.properties

```spin
java8_home=/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home
java6_home=/Library/Java/Home
inhouse_version=2
```

### before install

* remove exif to image resource

```
find . -path '*src/main/res/*' -name '*.png' -exec exiftool -overwrite_original -all= {} \;
```

### String Resource Crawler

1. install Python
2. install pip
3. install openpyxl