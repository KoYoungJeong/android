echo "Run Exif Tool!!!"
find . -path '*src/main/res/*' -name '*.png' -exec exiftool -overwrite_original -all= {} \;
find . -path '*src/main/res-crawrer/*' -name '*.png' -exec exiftool -overwrite_original -all= {} \;
