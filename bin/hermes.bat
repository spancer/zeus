@echo off

SETLOCAL

if NOT DEFINED JAVA_HOME goto err

set SCRIPT_DIR=%~dp0
for %%I in ("%SCRIPT_DIR%..") do set HERMES_HOME=%%~dpfI


REM ***** JAVA options *****

if "%HERMES_MIN_MEM%" == "" (
set HERMES_MIN_MEM=256m
)

if "%HERMES_MAX_MEM%" == "" (
set HERMES_MAX_MEM=1g
)

if NOT "%HERMES_HEAP_SIZE%" == "" (
set HERMES_MIN_MEM=%HERMES_HEAP_SIZE%
set HERMES_MAX_MEM=%HERMES_HEAP_SIZE%
)

set JAVA_OPTS=%JAVA_OPTS% -Xms%HERMES_MIN_MEM% -Xmx%HERMES_MAX_MEM%

if NOT "%HERMES_HEAP_NEWSIZE%" == "" (
set JAVA_OPTS=%JAVA_OPTS% -Xmn%HERMES_HEAP_NEWSIZE%
)

if NOT "%HERMES_DIRECT_SIZE%" == "" (
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxDirectMemorySize=%HERMES_DIRECT_SIZE%
)

set JAVA_OPTS=%JAVA_OPTS% -Xss256k

REM Enable aggressive optimizations in the JVM
REM    - Disabled by default as it might cause the JVM to crash
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+AggressiveOpts

set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParNewGC
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC

set JAVA_OPTS=%JAVA_OPTS% -XX:CMSInitiatingOccupancyFraction=75
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCMSInitiatingOccupancyOnly

REM When running under Java 7
REM JAVA_OPTS=%JAVA_OPTS% -XX:+UseCondCardMark

REM GC logging options -- uncomment to enable
REM JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCDetails
REM JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCTimeStamps
REM JAVA_OPTS=%JAVA_OPTS% -XX:+PrintClassHistogram
REM JAVA_OPTS=%JAVA_OPTS% -XX:+PrintTenuringDistribution
REM JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCApplicationStoppedTime
REM JAVA_OPTS=%JAVA_OPTS% -Xloggc:/var/log/hermes/gc.log

REM Causes the JVM to dump its heap on OutOfMemory.
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
REM The path to the heap dump location, note directory must exists and have enough
REM space for a full heap dump.
REM JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=$HERMES_HOME/logs/heapdump.hprof

set HERMES_CLASSPATH=%HERMES_CLASSPATH%;%HERMES_HOME%/lib/${project.build.finalName}.jar;%HERMES_HOME%/lib/*
set HERMES_PARAMS=-Dhermes -Dhermes-foreground=yes -Dhermes.path.home="%HERMES_HOME%"

"%JAVA_HOME%\bin\java" %JAVA_OPTS% %HERMES_JAVA_OPTS% %HERMES_PARAMS% %* -cp "%HERMES_CLASSPATH%" "io.hermes.bootstrap.Bootstrap"
goto finally


:err
echo JAVA_HOME environment variable must be set!
pause


:finally

ENDLOCAL