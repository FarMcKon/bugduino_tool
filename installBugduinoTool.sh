TOOL_NAME="ProgramBugduino"
TOOL_JAR="bugduino_tools.jar"
TOOL_JAR_SRC=${TOOL_NAME}/tool/${TOOL_JAR}

if [ $# -lt 1 ] ; then
	echo 'usage: installBugduinoTool.sh <jar_file>' 
	echo "assiming this is running in the build dir. Specify a tool src  otherwise"
else
	TOOL_JAR_SRC=$1
fi


echo "check for ~/sketchbook/ dir."
if [ -d ~/sketchbook ]; then
	if [ ! -d ~/sketchbook/tools/${TOOL_NAME}/tool ]; then
		mkdir -p ~/sketchbook/tools/${TOOL_NAME}/tool 
	fi	
	echo "sketchbook dir exists. Trying to install tool jar"
	if [ ! -e ${TOOL_JAR_SRC} ]; then
		echo "no such source jar: ${TOOL_JAR_SRC} "
		exit -5;
	fi
	cp ${TOOL_JAR_SRC} ~/sketchbook/tools/${TOOL_NAME}/tool/${TOOL_JAR}
	
else
	echo "no sketchbook dir to install in"
	exit -3 
fi
