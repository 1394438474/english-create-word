$env:Path = "C:\dev-tools\mysql-8.0.39-winx64\bin;" + $env:Path
$OutputEncoding = [System.Text.Encoding]::UTF8
$proc = Start-Process -FilePath "mysqldump.exe" -ArgumentList @(
    "--default-character-set=utf8mb4","-uroot","-proot",
    "--single-transaction","--routines","--triggers",
    "--add-drop-table","--set-gtid-purged=OFF","smartvocab"
) -RedirectStandardOutput "db\smartvocab.sql" -RedirectStandardError "db\dump_err.log" -NoNewWindow -Wait
Write-Host "exit:" $proc.ExitCode
$f = Get-Item "db\smartvocab.sql"
Write-Host ("Dump: " + $f.Length + " bytes")
if (Test-Path "db\dump_err.log") { Get-Content "db\dump_err.log" }
