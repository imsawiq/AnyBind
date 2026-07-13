$ErrorActionPreference = "Stop"

$versions = @(
    "1.21.1-1.21.8",
    "1.21.9-1.21.11",
    "26.1-26.1.2",
    "26.2"
)

foreach ($version in $versions) {
    Write-Host ""
    Write-Host "=== $version ===" -ForegroundColor Cyan
    & (Join-Path $PSScriptRoot "build-version.ps1") $version
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

Write-Host ""
Write-Host "Build script finished." -ForegroundColor Green
