# Publish VarinBloknot to GitHub (TimurNazmutdinov/VarinBloknot).
#   .\publish.ps1 content -Message "new scene"   # content only: manifest + commit + push (phones pick it up silently)
#   .\publish.ps1 app -Notes "what changed"      # release: takes release\VarinBloknot-<version>.apk made by scripts\build-apk.ps1,
#                                                #   commit, tag v<version>, push, GitHub Release with the APK
# Messages are ASCII on purpose: Windows PowerShell 5.1 reads a UTF-8 file without BOM as ANSI.
param(
    [Parameter(Mandatory = $true, Position = 0)][ValidateSet('app', 'content')][string]$Mode,
    [string]$Notes = '',
    [string]$Message = 'content update'
)
$ErrorActionPreference = 'Continue'   # git/gh write progress to stderr; failures are checked via exit codes
Set-Location $PSScriptRoot
$Repo = 'TimurNazmutdinov/VarinBloknot'
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Run($what, [scriptblock]$cmd) {
    & $cmd
    if ($LASTEXITCODE -ne 0) { throw "Failed: $what" }
}
function Git-Push($msg) {
    git add -A
    git diff --cached --quiet
    if ($LASTEXITCODE -ne 0) { Run 'git commit' { git commit -q -m $msg } }
}

if (-not (Test-Path .git)) {
    git init -b main | Out-Null
    git remote add origin "https://github.com/$Repo.git"
}

if ($Mode -eq 'content') {
    $r = & .\scripts\update-manifest.ps1
    Write-Host $r
    Git-Push "content: $Message"
    Run 'git push' { git push origin HEAD:main }
    Write-Host 'Content pushed. Phones take it on next start (at most once per 6 hours) or via "Check updates".'
    exit 0
}

# --- app ---
$p = @{}; Get-Content version.properties | ForEach-Object { if ($_ -match '^(\w+)=(.*)$') { $p[$Matches[1]] = $Matches[2] } }
$name = $p['VERSION_NAME']
$apk = "release\VarinBloknot-$name.apk"
if (-not (Test-Path $apk)) { throw "No $apk. Run scripts\build-apk.ps1 first." }
if (-not $Notes) { $Notes = "Version $name" }

Git-Push "release v$name"
Run 'git tag' { git tag "v$name" }
Run 'git push' { git push origin HEAD:main }
Run 'git push tag' { git push origin "v$name" }
$nf = Join-Path $env:TEMP 'varin-notes.txt'
[IO.File]::WriteAllText($nf, $Notes, $utf8)
Run 'gh release' { gh release create "v$name" $apk --repo $Repo --title "v$name" --notes-file $nf }
Write-Host "Release v$name published."
