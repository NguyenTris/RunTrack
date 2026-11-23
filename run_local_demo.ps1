<#
.SYNOPSIS
    Khởi chạy nhanh toàn bộ hệ thống RunTrack (Backend + Web-Admin) trên máy Local không cần Docker.
.DESCRIPTION
    Tự động thiết lập môi trường Python/Node (nếu chưa có), nạp dữ liệu mẫu (admin@runtrack.app / Admin@123),
    và khởi chạy đồng thời FastAPI Backend (port 8000) cùng React Web-Admin (port 5173).
#>
param(
    [int]$BackendPort = 8000,
    [int]$WebPort = 5173
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$RootDir = $PSScriptRoot
$BackendDir = Join-Path $RootDir "runtrack_backend"
$WebDir = Join-Path $RootDir "runtrack_web_admin"
$VenvPython = Join-Path $BackendDir ".venv\Scripts\python.exe"

Write-Host "=== [1/4] Kiểm tra môi trường Python Backend ===" -ForegroundColor Cyan
if (-not (Test-Path $VenvPython)) {
    Write-Host "Chưa có .venv, đang tạo môi trường ảo Python..." -ForegroundColor Yellow
    python -m venv (Join-Path $BackendDir ".venv")
    & $VenvPython -m pip install -r (Join-Path $BackendDir "requirements.txt")
}

Write-Host "=== [2/4] Kiểm tra môi trường Node.js Web-Admin ===" -ForegroundColor Cyan
if (-not (Test-Path (Join-Path $WebDir "node_modules"))) {
    Write-Host "Chưa có node_modules, đang chạy npm install..." -ForegroundColor Yellow
    Push-Location $WebDir
    npm install
    Pop-Location
}

Write-Host "=== [3/4] Khởi tạo DB SQLite & nạp dữ liệu mẫu (seed.py) ===" -ForegroundColor Cyan
$env:DATABASE_URL = "sqlite:///./runtrack_demo.db"
$env:AUTO_SEED_DEMO = "true"
Push-Location $BackendDir
& $VenvPython seed.py
Pop-Location

Write-Host "=== [4/4] Khởi chạy Backend (:$BackendPort) và Web-Admin (:$WebPort) ===" -ForegroundColor Green
$BackendCmd = "Set-Location '$BackendDir'; `$env:DATABASE_URL='sqlite:///./runtrack_demo.db'; `$env:AUTO_SEED_DEMO='true'; & '$VenvPython' -m uvicorn main:app --host 127.0.0.1 --port $BackendPort"
$WebCmd = "Set-Location '$WebDir'; `$env:VITE_API_BASE_URL='http://127.0.0.1:$BackendPort'; npm run dev -- --host 127.0.0.1 --port $WebPort"

Start-Process powershell -ArgumentList "-NoExit", "-Command", $BackendCmd
Start-Process powershell -ArgumentList "-NoExit", "-Command", $WebCmd

Write-Host ""
Write-Host "Hệ thống RunTrack đã khởi chạy thành công!" -ForegroundColor Green
Write-Host "  - Web-Admin:    http://127.0.0.1:$WebPort"
Write-Host "  - Swagger Docs: http://127.0.0.1:$BackendPort/docs"
Write-Host "  - Tài khoản Admin:  admin@runtrack.app / Admin@123"
Write-Host "  - Tài khoản Runner: runner1@runtrack.app / Runner@123"
