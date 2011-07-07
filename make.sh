TOOL_NAME="ProgramBugduino"
TOOL_JAR="bugduino_tools.jar"

set -e

CMD=git
which $CMD &> /dev/null || { echo "Please apt-get install $CMD and re-run the script."; exit 1; }

echo "copy directory to the right location in the arduino_src tree"
cp -R ${TOOL_NAME} ./arduino_src/Arduino/build/shared/tools/ 
START_DIR=`pwd`

echo "build Arduino jars, so we can comile against them"
cd arduino_src/Arduino/build/
ant build
echo "Arduino build will copy /shared ${TOOL_NAmE} into linux work dir for us"
cd  linux/work/tools/${TOOL_NAME}
echo "running our tools make.sh script"
chmod 777 make.sh
./make.sh

if [ ! -e ${TOOL_JAR} ] ; then
	echo "Jar of tool ${TOOL_JAR} not build"
	return -5;
fi

echo "copying tool to root build dir"
cp ${TOOL_JAR} ${START_DIR} 
echo ${START_DIR}
cd ${START_DIR}

echo "running installBugduinoTool.sh"
./installBugduinoTool.sh ${TOOL_JAR}
