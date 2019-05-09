#!/bin/sh

# change work directory
cd "$(dirname "$0")"

# default JVM options
jvm_options=`java -cp alienchain.jar org.alienchain.JvmOptions --gui`

# start kernel
java ${jvm_options} -cp alienchain.jar org.alienchain.Main --gui "$@"
