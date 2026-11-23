param(
    [switch]$SkipTests
)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
& powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Users\LOG\.gemini\config\skills\trust-pyramid\scripts\agy-verify.ps1" -ProjectPath $PSScriptRoot -Profile "web-js-ui" @PSBoundParameters
exit $LASTEXITCODE
