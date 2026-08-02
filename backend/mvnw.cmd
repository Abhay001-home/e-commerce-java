@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM This batch file is used to start Maven with the Maven Wrapper.
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_OPTS=-Xms256m -Xmx1024m

set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

for /f "usebackq tokens=1,2 delims==" %%a in (".mvn\wrapper\maven-wrapper.properties") do (
    if "%%a"=="distributionUrl" set DISTRIBUTION_URL=%%b
)

"%JAVA_HOME%\bin\java.exe" -jar %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
