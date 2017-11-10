# ===========================================================
# Filename:    Makefile
# Description: makes Simple DSP applet
# Authors:     L.R. McFarland
# Language:    make
# Created:     19 August 2003
# ===========================================================


JVC = javac
JVCFLAGS = -sourcepath . -d . -classpath .:./plot.jar

all: sdspjar

SimpleDSPApplet: SimpleDSPApplet.java
	${JVC} ${JVCFLAGS} SimpleDSPApplet.java


sdspjar : SimpleDSPApplet
	jar cv -m SDSP.stub -f SDSP.jar *.class

clean :
	-rm *.class SDSP.jar
