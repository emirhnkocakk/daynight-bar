@echo off
set GRADLE_VERSION=8.10
set GRADLE_DISTRIBUTION_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_USER_HOME=%USERPROFILE%\.gradle

if not exist "%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin\*" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri '%GRADLE_DISTRIBUTION_URL%' -OutFile '%TEMP%\gradle-%GRADLE_VERSION%-bin.zip'"
    powershell -Command "Expand-Archive -Force -Path '%TEMP%\gradle-%GRADLE_VERSION%-bin.zip' -DestinationPath '%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin'"
)

for /d %%i in ("%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin\*") do (
    set GRADLE_HOME=%%i
)

if not defined GRADLE_HOME (
    echo Could not find Gradle installation.
    exit /b 1
)

set GRADLE_EXE=%GRADLE_HOME%\bin\gradle.bat
"%GRADLE_EXE%" %*