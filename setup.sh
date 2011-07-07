
if [ !  -d arduino_src ] ; then
	mkdir arduino_src
fi
cd arduino_src
echo "feching arduino source code from git"
git clone 'git://github.com/arduino/Arduino.git'
echo "making softlinks to our tool"
echo "//TODO: make softlinks"
cd ..

