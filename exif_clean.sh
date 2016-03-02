echo "Start Cleaning exif to Origins"
find . -path '*src/main/res/*' -name '*.png' -exec exiftool -overwrite_original -all= {} \;
echo "************************************"
echo "Start Cleaning exif to Crawrer"
find . -path '*src/main/res-crawrer/*' -name '*.png' -exec exiftool -overwrite_original -all= {} \;
