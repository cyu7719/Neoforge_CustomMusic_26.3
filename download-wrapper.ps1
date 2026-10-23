# Download Gradle Wrapper JAR
$url = "https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle\wrapper\gradle-wrapper.jar"

Write-Host "Downloading Gradle wrapper JAR..." -ForegroundColor Green
New-Item -ItemType Directory -Force -Path "gradle\wrapper" | Out-Null

try {
    Invoke-WebRequest -Uri $url -OutFile $output
    Write-Host "Download complete!" -ForegroundColor Green
    Write-Host "You can now run: gradlew build" -ForegroundColor Cyan
} catch {
    Write-Host "Download failed. Please download manually from:" -ForegroundColor Red
    Write-Host $url -ForegroundColor Yellow
    Write-Host "Save to: $output" -ForegroundColor Yellow
}

pause
