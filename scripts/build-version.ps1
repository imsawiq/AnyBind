param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Version,

    [switch]$NoClean
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$dist = Join-Path $root "dist"

$targets = @{
    "base" = @{
        Path = $root
        Label = "fabric-1.21.1-1.21.8"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 1.21.1-1.21.8 compatibility adapters."
    }
    "1.21.1-1.21.8" = @{
        Path = $root
        Label = "fabric-1.21.1-1.21.8"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 1.21.1-1.21.8 compatibility adapters."
    }
    "1.21.8" = @{
        Path = $root
        Label = "fabric-1.21.1-1.21.8"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 1.21.1-1.21.8 compatibility adapters."
    }
    "1.21.9-1.21.11" = @{
        Path = (Join-Path $root "versions\1.21.9-1.21.11")
        Label = "fabric-1.21.9-1.21.11"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 1.21.9-1.21.11 compatibility adapters."
    }
    "1.21.9" = @{
        Path = (Join-Path $root "versions\1.21.9-1.21.11")
        Label = "fabric-1.21.9-1.21.11"
        Buildable = $true
        Note = "Alias for the 1.21.9-1.21.11 source set."
    }
    "1.21.10" = @{
        Path = (Join-Path $root "versions\1.21.9-1.21.11")
        Label = "fabric-1.21.9-1.21.11"
        Buildable = $true
        Note = "Alias for the 1.21.9-1.21.11 source set."
    }
    "1.21.11" = @{
        Path = (Join-Path $root "versions\1.21.9-1.21.11")
        Label = "fabric-1.21.9-1.21.11"
        Buildable = $true
        Note = "Alias for the 1.21.9-1.21.11 source set."
    }
    "26.1-26.1.2" = @{
        Path = (Join-Path $root "versions\26.1-26.1.2")
        Label = "fabric-26.1-26.1.2"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 26.1-26.1.2 compatibility adapters."
    }
    "26.1" = @{
        Path = (Join-Path $root "versions\26.1-26.1.2")
        Label = "fabric-26.1-26.1.2"
        Buildable = $true
        Note = "Alias for the 26.1-26.1.2 source set."
    }
    "26.1.1" = @{
        Path = (Join-Path $root "versions\26.1-26.1.2")
        Label = "fabric-26.1-26.1.2"
        Buildable = $true
        Note = "Alias for the 26.1-26.1.2 source set."
    }
    "26.1.2" = @{
        Path = (Join-Path $root "versions\26.1-26.1.2")
        Label = "fabric-26.1-26.1.2"
        Buildable = $true
        Note = "Alias for the 26.1-26.1.2 source set."
    }
    "26.2" = @{
        Path = (Join-Path $root "versions\26.2")
        Label = "fabric-26.2"
        Buildable = $true
        Note = "Shared implementation with the Minecraft 26.2 compatibility adapters."
    }
}

if ($Version -eq "all") {
    & (Join-Path $PSScriptRoot "build-all.ps1")
    exit $LASTEXITCODE
}

if (-not $targets.ContainsKey($Version)) {
    Write-Host "Unknown version: $Version" -ForegroundColor Red
    $availableVersions = ($targets.Keys | Sort-Object) -join ", "
    Write-Host "Available versions: $availableVersions"
    exit 1
}

$target = $targets[$Version]

if (-not (Test-Path $target.Path)) {
    Write-Host "Version path does not exist: $($target.Path)" -ForegroundColor Red
    exit 1
}

if (-not $target.Buildable) {
    Write-Host "Skipping ${Version}: $($target.Note)" -ForegroundColor Yellow
    exit 0
}

New-Item -ItemType Directory -Force $dist | Out-Null

$gradle = Join-Path $target.Path "gradlew.bat"
if (-not (Test-Path $gradle)) {
    Write-Host "Gradle wrapper not found: $gradle" -ForegroundColor Red
    exit 1
}

$gradleArgs = @()
if (-not $NoClean) {
    $gradleArgs += "clean"
}
$gradleArgs += "build"
$gradleArgs += "--no-daemon"

Write-Host "Building $Version ($($target.Label))" -ForegroundColor Cyan
Push-Location $target.Path
try {
    & $gradle @gradleArgs
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
finally {
    Pop-Location
}

$jars = Get-ChildItem -Path (Join-Path $target.Path "build\libs") -Filter "*.jar" -File
if (-not $jars) {
    Write-Host "No jars produced for $Version." -ForegroundColor Red
    exit 1
}

Get-ChildItem -Path $dist -Filter "*-$($target.Label).jar" -File -ErrorAction SilentlyContinue | Remove-Item -Force

foreach ($jar in $jars) {
    $nameWithoutExt = [System.IO.Path]::GetFileNameWithoutExtension($jar.Name)
    $outName = "$nameWithoutExt-$($target.Label)$($jar.Extension)"
    Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $dist $outName) -Force
    Write-Host "Copied dist\$outName" -ForegroundColor Green
}
