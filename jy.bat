@echo off
:: palisades.lakes (at) gmail (dot) com
:: 2018-12-12

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

set THRUPUT=-server -Xbatch -XX:+UseFMA
::set THRUPUT=-server -Xbatch 

::set XMX=-Xms29g -Xmx29g -Xmn11g 
set XMX=-Xms12g -Xmx12g -Xmn5g 

::set PROF=
set PROF=-agentpath:"C:\Program Files\YourKit Java Profiler 2018.04-b88\bin\win64\yjpagent.dll=_no_java_version_check"

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp lib/*

set JAVA_HOME=%JAVA11%
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% %THRUPUT% -ea -dsa %GC% %XMX% %TRACE% %OPENS% %CP% %*
::echo %CMD%
%CMD%
