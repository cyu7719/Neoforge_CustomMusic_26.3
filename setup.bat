@echo off
echo Setting up Gradle Wrapper...
echo.
echo Please ensure you have Gradle installed on your system.
echo If not, download from: https://gradle.org/install/
echo.
pause

gradle wrapper --gradle-version 8.8

echo.
echo Wrapper setup complete! You can now use gradlew.bat
pause
