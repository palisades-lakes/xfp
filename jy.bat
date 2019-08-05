@echo off
:: palisades.lakes (at) gmail (dot) com
:: 2019-08-05

::set GC=-XX:+AggressiveHeap -XX:+UseStringDeduplication 
set GC=

set TRACE=
::set TRACE=-XX:+PrintGCDetails -XX:+TraceClassUnloading -XX:+TraceClassLoading

set THRUPUT=-server -Xbatch -XX:+UseFMA
::set THRUPUT=-server -Xbatch 

::set XMX=-Xms29g -Xmx29g -Xmn11g 
set XMX=-Xms12g -Xmx12g -Xmn5g 

::set PROF=
set PROF=-agentpath:"C:\Program Files\YourKit Java Profiler 2019.8-b109\bin\win64\yjpagent.dll"

set OPENS=--add-opens java.base/java.lang=ALL-UNNAMED
set CP=-cp lib/*

set JAVA_HOME=%JAVA11%
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% %THRUPUT% -ea -dsa %GC% %XMX% %TRACE% %PROF% %OPENS% %CP% %*
::echo %CMD%
%CMD%
