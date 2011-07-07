TOOL_NAME="ProgramBugduino"
TOOL_JAR="bugduino_tools.jar"

echo "symlink our directory to the right location in the arduino_src tree"
echo "check for dir/files we need, error if missing"
echo "call make file for our tool"

echo "check for ~/sketchbook/ dir."
if [ -d ~/sketchbook ]; then
	if [ ! -d ~/sketchbook/tools/${TOOL_NAME}/tool ]; then
		mkdir -p ~/sketchbook/tools/${TOOL_NAME}/tool 
	fi	
	echo "sketchbook dir exists. Do crap"
	cp ${TOOL_NAME}/tool/${TOOL_JAR}\
		 ~/sketchbook/tools/${TOOL_NAME}/tool/${TOOL_JAR}
	
else
	echo "no sketchbook dir to install in"
	exit -3 
fi 
