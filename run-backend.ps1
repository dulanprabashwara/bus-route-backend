# Helper script to start Spring Boot Backend with .env environment variables

$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot"

if (Test-Path ".env") {
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split("=", 2)
            if ($parts.Count -eq 2) {
                $name = $parts[0].Trim()
                $val = $parts[1].Trim()
                [System.Environment]::SetEnvironmentVariable($name, $val, "Process")
            }
        }
    }
} else {
    Write-Host "Warning: .env file not found. Make sure DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD environment variables are set." -ForegroundColor Yellow
}

Write-Host "Starting Bus Route Backend API on http://localhost:8080..." -ForegroundColor Green
.\mvnw spring-boot:run
