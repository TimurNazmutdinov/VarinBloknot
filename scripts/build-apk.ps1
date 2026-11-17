# Build a signed release APK -> release\VarinBloknot-<version>.apk (+ size, SHA-256, signature check).
#   powershell -ExecutionPolicy Bypass -File scripts\build-apk.ps1            # build the version from version.properties
#   powershell -ExecutionPolicy Bypass -File scripts\build-apk.ps1 -Bump patch # first +1 (patch | minor | major), then build
# Messages are ASCII on purpose: Windows PowerShell 5.1 reads a UTF-8 file without BOM as ANSI.
param([ValidateSet('', 'patch', 'minor', 'major')][string]$Bump = '')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not $env:JAVA_HOME) { $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr' }
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk" }
# Without this Java cannot open its internal channel on this PC and Gradle does not start.
New-Item -ItemType Directory -Force C:\tmp | Out-Null
$env:JAVA_TOOL_OPTIONS = '-Djdk.net.unixdomain.tmpdir=C:\tmp'

$props = Join-Path $env:USERPROFILE '.gradle\gradle.properties'
if (-not (Select-String -Path $props -Pattern '^VARIN_STORE_FILE=' -Quiet -ErrorAction SilentlyContinue)) {
    throw "No VARIN_STORE_FILE and other signing properties in $props (see docs/RELEASE.md)."
}

$utf8 = New-Object System.Text.UTF8Encoding($false)
$vp = Join-Path (Get-Location) 'version.properties'
$p = @{}; Get-Content $vp | ForEach-Object { if ($_ -match '^(\w+)=(.*)$') { $p[$Matches[1]] = $Matches[2] } }
if ($Bump) {
    $v = $p['VERSION_NAME'].Split('.') | ForEach-Object { [int]$_ }
    switch ($Bump) {
        'major' { $v = @(($v[0] + 1), 0, 0) }
        'minor' { $v = @($v[0], ($v[1] + 1), 0) }
        default { $v = @($v[0], $v[1], ($v[2] + 1)) }
    }
    $p['VERSION_NAME'] = $v -join '.'
    $p['VERSION_CODE'] = [string]([int]$p['VERSION_CODE'] + 1)
    [IO.File]::WriteAllText($vp, "VERSION_NAME=$($p['VERSION_NAME'])`nVERSION_CODE=$($p['VERSION_CODE'])`n", $utf8)
}
$version = $p['VERSION_NAME']
Write-Host "== Version $version (code $($p['VERSION_CODE']))" -ForegroundColor Cyan

# Refresh content\manifest.json (embedded fallback content must match content\)
& (Join-Path $PSScriptRoot 'update-manifest.ps1')

Write-Host '== gradle assembleRelease' -ForegroundColor Cyan
.\gradlew.bat --stop | Out-Null
.\gradlew.bat assembleRelease --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Stopped at step: gradle' }

$built = Get-ChildItem app\build\outputs\apk\release\*.apk | Select-Object -First 1
if (-not $built) { throw 'Build output not found.' }
New-Item -ItemType Directory -Force release | Out-Null
$out = "release\VarinBloknot-$version.apk"
Copy-Item $built.FullName $out -Force

$size = [math]::Round((Get-Item $out).Length / 1MB, 2)
$hash = (Get-FileHash $out -Algorithm SHA256).Hash
$bt = Get-ChildItem "$env:ANDROID_HOME\build-tools" -Directory | Sort-Object Name -Descending | Select-Object -First 1
$ErrorActionPreference = 'Continue'   # apksigner prints "Picked up JAVA_TOOL_OPTIONS" to stderr
$sig = & "$($bt.FullName)\apksigner.bat" verify --print-certs $out 2>&1 | ForEach-Object { "$_" } | Select-String 'certificate SHA-256 digest' | Select-Object -First 1
Write-Host "Done: $out ($size MB)"
Write-Host "SHA-256: $hash" -ForegroundColor Green
Write-Host "Signature: $sig"
