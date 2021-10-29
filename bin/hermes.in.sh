#!/bin/sh

HERMHERMES_CLASSPATH=$HERMES_CLASSPATH:$HERMHERMES_HOME/lib/${project.build.finalName}.jar:$HERMES_HOME/lib/*

if [ "x$HERMES_MIN_MEM" = "x" ]; then
    HERMES_MIN_MEM=256m
fi
if [ "x$HERMES_MAX_MEM" = "x" ]; then
    HERMES_MAX_MEM=1g
fi
if [ "x$HERMES_HEAP_SIZE" != "x" ]; then
    HERMES_MIN_MEM=$HERMES_HEAP_SIZE
    HERMES_MAX_MEM=$HERMES_HEAP_SIZE
fi

# min and max heap sizes should be set to the same value to avoid
# stop-the-world GC pauses during resize, and so that we can lock the
# heap in memory on startup to prevent any of it from being swapped
# out.
JAVA_OPTS="$JAVA_OPTS -Xms${HERMES_MIN_MEM}"
JAVA_OPTS="$JAVA_OPTS -Xmx${HERMES_MAX_MEM}"

# new generation
if [ "x$HERMES_HEAP_NEWSIZE" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -Xmn${HERMES_HEAP_NEWSIZE}"
fi

# max direct memory
if [ "x$HERMES_DIRECT_SIZE" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -XX:MaxDirectMemorySize=${HERMES_DIRECT_SIZE}"
fi

# reduce the per-thread stack size
JAVA_OPTS="$JAVA_OPTS -Xss256k"

# set to headless, just in case
JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"

# Force the JVM to use IPv4 stack
# JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC"

JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=75"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSInitiatingOccupancyOnly"

# When running under Java 7
#JAVA_OPTS="$JAVA_OPTS -XX:+UseCondCardMark"

# GC logging options -- uncomment to enable
# JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
# JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
# JAVA_OPTS="$JAVA_OPTS -XX:+PrintClassHistogram"
# JAVA_OPTS="$JAVA_OPTS -XX:+PrintTenuringDistribution"
# JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
# JAVA_OPTS="$JAVA_OPTS -Xloggc:/var/log/hermes/gc.log"

# Causes the JVM to dump its heap on OutOfMemory.
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
# The path to the heap dump location, note directory must exists and have enough
# space for a full heap dump.
#JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=$HERMES_HOME/logs/heapdump.hprof"