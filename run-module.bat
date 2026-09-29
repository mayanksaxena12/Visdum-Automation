@echo off
setlocal enabledelayedexpansion

set MODULE=%1
if "%MODULE%"=="" set MODULE=all

set EXTRA_ARGS=
shift

:parse_args
if "%~1"=="" goto done_args
if /I "%~1"=="force"      set EXTRA_ARGS=!EXTRA_ARGS! -Dexcel.force=true
if /I "%~1"=="-force"     set EXTRA_ARGS=!EXTRA_ARGS! -Dexcel.force=true
if /I "%~1"=="--force"    set EXTRA_ARGS=!EXTRA_ARGS! -Dexcel.force=true
if /I "%~1"=="headless"   set EXTRA_ARGS=!EXTRA_ARGS! -Dheadless=true
if /I "%~1"=="-headless"  set EXTRA_ARGS=!EXTRA_ARGS! -Dheadless=true
if /I "%~1"=="--headless" set EXTRA_ARGS=!EXTRA_ARGS! -Dheadless=true
shift
goto parse_args
:done_args

set SHEET=
if /I "%MODULE%"=="login"          set SHEET=Login
if /I "%MODULE%"=="employees"      set SHEET=Users
if /I "%MODULE%"=="users"          set SHEET=Users
if /I "%MODULE%"=="user"           set SHEET=Users
if /I "%MODULE%"=="teams"          set SHEET=Team
if /I "%MODULE%"=="team"           set SHEET=Team
if /I "%MODULE%"=="departments"    set SHEET=Departments
if /I "%MODULE%"=="department"     set SHEET=Departments
if /I "%MODULE%"=="resources"      set SHEET=Resources
if /I "%MODULE%"=="resource"       set SHEET=Resources
if /I "%MODULE%"=="refreshcolumns" set SHEET=Refresh Columns
if /I "%MODULE%"=="rawdata"        set SHEET=Refresh Columns
if /I "%MODULE%"=="datastreams"    set SHEET=Data Streams
if /I "%MODULE%"=="plans"          set SHEET=Plans
if /I "%MODULE%"=="assignplans"    set SHEET=Assign Plans
if /I "%MODULE%"=="esign"          set SHEET=E-Sign

if /I "%MODULE%"=="all" (
    echo ======================================================================
    echo  Running ALL Test Cases marked 'Run = Yes' across Excel Workbook
    echo  Suite: testng-excel.xml ^| File: src/test/resources/manual-testcases.xlsx
    echo ======================================================================
    mvn test -DsuiteXmlFile=testng-excel.xml %EXTRA_ARGS%
    exit /b %ERRORLEVEL%
)

if "%SHEET%"=="" (
    echo Unknown module: %MODULE%
    echo Available Excel modules: login, users, team, departments, resources, rawdata, plans, assignplans, esign, all
    exit /b 1
)

echo ======================================================================
echo  Running Module: %MODULE% from Excel Sheet: '%SHEET%'
echo  Suite: testng-excel.xml ^| File: src/test/resources/manual-testcases.xlsx
echo  (Only rows where Run = Yes in Excel are executed)
echo ======================================================================

mvn test -DsuiteXmlFile=testng-excel.xml -Dexcel.sheet="%SHEET%" %EXTRA_ARGS%
