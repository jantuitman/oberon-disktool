rm -rf classes/disktool
javac -sourcepath src/ src/*.java -d classes/
jar -cvfm disktool.jar manifest -C classes/ .