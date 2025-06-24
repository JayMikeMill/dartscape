@echo off
REM set REPO_PATH = https://github.com/JayMikeMill/Dartscape.git
REM get clone %REPO_PATH%


set /p commitMsg=Enter commit message: 

if not defined commitMsg set "commitMsg=(no message)"
echo commiting with message: "%commitMsg%"...

call git add .
call git commit -m "%commitMsg%"
call git push -u origin main

pause
