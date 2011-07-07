#!/bin/sh

# The pde.jar file may be buried inside the .app file on Mac OS X.
PDE=`find ../.. -name pde.jar`

if [ ! -d bin ]; then
	mkdir bin
fi

javac -target 1.5 \
  -cp "../../lib/core.jar:$PDE" \
  -d bin \
  src/ProgramBugduino.java

cd bin && zip -r ../bugduino_tools.jar * && cd ..
