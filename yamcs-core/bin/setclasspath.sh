#!/bin/sh

if [ -z "$YAMCS_HOME" ]; then
  echo "The YAMCS_HOME variable must be set before running this script"
  exit 1
fi

# Add all necessary jars
CLASSPATH="$YAMCS_HOME/lib/*:$YAMCS_HOME/lib/ext/*"

# Add etc directory to load config resources
CLASSPATH=$YAMCS_HOME/etc:$CLASSPATH:$YAMCS_HOME
# Add the lib directory to load the xtce derived values
CLASSPATH=$CLASSPATH:$YAMCS_HOME/lib

export CLASSPATH

if [ -d "$JAVA_HOME" ]; then
  _RUNJAVA="$JAVA_HOME/bin/java"
else
  _RUNJAVA=java
fi
