# Loads the repo-root .env file into the current process's environment,
# then runs the Spring Boot backend. This is the normal way to start the
# backend locally — no manual `export`/`$env:` steps needed.

$envFile = Join-Path $PSScriptRoot "..\.env"

if (-not (Test-Path $envFile)) {
    Write-Error "No .env file found at $envFile. Copy .env.example to .env at the repo root and fill in real values first."
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $pair = $line -split "=", 2
        if ($pair.Length -eq 2) {
            $name = $pair[0].Trim()
            $value = $pair[1].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

Write-Host "Loaded environment from $envFile" -ForegroundColor Green
Set-Location $PSScriptRoot
mvn spring-boot:run
