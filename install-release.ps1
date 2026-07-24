#Requires -Version 5.1
<#
.SYNOPSIS
  Build a release APK and install it on a device already connected via ADB
  (USB or wireless debugging).

.USAGE
  .\install-release.ps1
  .\install-release.ps1 -NoLaunch
  .\install-release.ps1 -Serial 192.168.1.20:5555

.NOTES
  Prerequisites:
  - adb on PATH (Android platform-tools)
  - Device paired/connected: adb devices should list it as "device"
#>
param(
    [switch]$NoLaunch,
    [string]$Serial
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Root = $PSScriptRoot
$PackageId = "com.rykersoft.appmanager"
$ApkPath = Join-Path $Root "app\build\outputs\apk\release\app-release.apk"
$Gradlew = Join-Path $Root "gradlew.bat"

function Write-Step([string]$Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' was not found on PATH. Install Android platform-tools and try again."
    }
}

function Get-AdbArgs {
    if ($Serial) { @("-s", $Serial) } else { @() }
}

function Get-ReadyDevices {
    # Always return Object[] so .Count works under Set-StrictMode (single string has no Count).
    $ready = [System.Collections.Generic.List[string]]::new()
    $lines = @(& adb devices 2>&1 | Select-Object -Skip 1)
    foreach ($line in $lines) {
        $trimmed = ("$line").Trim()
        if (-not $trimmed) { continue }
        $parts = @($trimmed -split "\s+")
        if ($parts.Count -ge 2 -and $parts[1] -eq "device") {
            $ready.Add($parts[0]) | Out-Null
        }
    }
    return , $ready.ToArray()
}

Push-Location $Root
try {
    Assert-Command "adb"

    if (-not (Test-Path $Gradlew)) {
        throw "gradlew.bat not found at $Gradlew"
    }

    Write-Step "Checking ADB device"
    $devices = @(Get-ReadyDevices)
    if ($Serial) {
        if ($devices -notcontains $Serial) {
            throw "Device '$Serial' is not connected (or not in 'device' state). Connected: $($devices -join ', ')"
        }
        Write-Host "Using device: $Serial"
    } else {
        if (@($devices).Length -eq 0) {
            throw "No ADB device in 'device' state. Connect wireless debugging first (adb connect IP:PORT), then re-run."
        }
        if (@($devices).Length -gt 1) {
            Write-Host "Multiple devices connected:" -ForegroundColor Yellow
            $devices | ForEach-Object { Write-Host "  $_" }
            throw "Pass -Serial <id> to pick one (e.g. -Serial 192.168.1.20:5555)."
        }
        Write-Host "Using device: $($devices[0])"
    }

    $adbArgs = Get-AdbArgs

    Write-Step "Building release APK"
    & $Gradlew ":app:assembleRelease" --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle assembleRelease failed (exit $LASTEXITCODE)."
    }

    if (-not (Test-Path $ApkPath)) {
        throw "Release APK not found at $ApkPath"
    }
    $apk = Get-Item $ApkPath
    Write-Host ("APK: {0} ({1:N1} MB)" -f $apk.FullName, ($apk.Length / 1MB))

    Write-Step "Installing on device"
    & adb @adbArgs install -r --no-incremental $ApkPath
    if ($LASTEXITCODE -ne 0) {
        throw "adb install failed (exit $LASTEXITCODE)."
    }

    if (-not $NoLaunch) {
        Write-Step "Launching $PackageId"
        & adb @adbArgs shell monkey -p $PackageId -c android.intent.category.LAUNCHER 1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Install succeeded, but launch failed. Open the app manually." -ForegroundColor Yellow
        }
    }

    Write-Host ""
    Write-Host "Done. Release build installed." -ForegroundColor Green
}
finally {
    Pop-Location
}
