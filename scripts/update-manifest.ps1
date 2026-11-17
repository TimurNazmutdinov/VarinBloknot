# Recalculate content\manifest.json (sha256 of every file in content\).
# contentVersion grows only when some file really changed. Prints CHANGED or SAME.
$ErrorActionPreference = 'Stop'
$root = Join-Path (Split-Path $PSScriptRoot -Parent) 'content'
$mPath = Join-Path $root 'manifest.json'
$utf8 = New-Object System.Text.UTF8Encoding($false)

$ver = 0; $minApp = '0.1.0'; $oldJson = ''
if (Test-Path $mPath) {
    $old = Get-Content $mPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $ver = [int]$old.contentVersion
    if ($old.minAppVersion) { $minApp = $old.minAppVersion }
    $oldJson = ($old.files | ForEach-Object { "$($_.path)=$($_.sha256)" }) -join ';'
}
$files = Get-ChildItem $root -Recurse -File | Where-Object { $_.Name -ne 'manifest.json' } | Sort-Object FullName | ForEach-Object {
    [ordered]@{ path = $_.FullName.Substring($root.Length + 1).Replace('\', '/'); sha256 = (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower() }
}
$newJson = ($files | ForEach-Object { "$($_.path)=$($_.sha256)" }) -join ';'
if ($newJson -eq $oldJson) { Write-Host "SAME: contentVersion $ver"; return }
$m = [ordered]@{ contentVersion = $ver + 1; minAppVersion = $minApp; files = @($files) }
[IO.File]::WriteAllText($mPath, (($m | ConvertTo-Json -Depth 5) + "`n"), $utf8)
Write-Host "CHANGED: contentVersion -> $($ver + 1)"
