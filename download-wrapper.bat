@echo off
echo Downloading Gradle wrapper JAR...
echo.

if not exist "gradle\wrapper" mkdir "gradle\wrapper"

curl -L -o "gradle\wrapper\gradle-wrapper.jar" "https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar"

if %errorlevel% equ 0 (
    echo.
    echo Download complete!
    echo You can now run: gradlew build
) else (
    echo.
    echo Download failed. Please check your internet connection.
)

pause
