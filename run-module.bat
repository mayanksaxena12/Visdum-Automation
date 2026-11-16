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
if /I "%MODULE%"=="datastream"     set SHEET=Data Streams
if /I "%MODULE%"=="data"           set SHEET=Data Streams
if /I "%MODULE%"=="plans"          set SHEET=Plans
if /I "%MODULE%"=="plan"           set SHEET=Plans
if /I "%MODULE%"=="createplan"     set SHEET=Plans
if /I "%MODULE%"=="ratetable"      set SHEET=Rate Table
if /I "%MODULE%"=="ratetables"     set SHEET=Rate Table
if /I "%MODULE%"=="rate"           set SHEET=Rate Table
if /I "%MODULE%"=="assignplans"    set SHEET=Assign Plans
if /I "%MODULE%"=="assignplan"     set SHEET=Assign Plans
if /I "%MODULE%"=="esign"          set SHEET=E-Sign

if /I "%MODULE%"=="org" goto run_org
if /I "%MODULE%"=="organization" goto run_org
if /I "%MODULE%"=="organizations" goto run_org

if /I "%MODULE%"=="fep" goto run_fep
if /I "%MODULE%"=="force-end" goto run_fep
if /I "%MODULE%"=="forceend" goto run_fep

if /I "%MODULE%"=="export" (
    echo ======================================================================
    echo  Running AG-Grid Right-Click Excel Export Suite Across All Modules
    echo  Suite: testng-export.xml ^| Class: tests.export.AgGridExportAllModulesTest
    echo  Modules: Users, Teams, Departments, Raw Data, Deal Credits, Rate Tables, Plans, Assign Plans
    echo ======================================================================
    mvn test -DsuiteXmlFile=testng-export.xml %EXTRA_ARGS%
    exit /b %ERRORLEVEL%
)

:run_org
echo ======================================================================
echo  Running Organization Management & Creation Suite
echo  Suite: testng-organization.xml ^| Class: tests.organization.CreateOrganizationTest
echo ======================================================================
mvn test -DsuiteXmlFile=testng-organization.xml %EXTRA_ARGS%
exit /b %ERRORLEVEL%

:run_fep
if /I "%MODULE%"=="fep" (
    echo ======================================================================
    echo  Running Force End Plan 120 Test Cases Suite (V20-1717)
    echo  Suite: testng-force-end.xml ^| File: src/test/resources/Force_End_Plan_120_Test_Cases.xlsx
    echo ======================================================================
    mvn test -DsuiteXmlFile=testng-force-end.xml %EXTRA_ARGS%
    exit /b %ERRORLEVEL%
)
if /I "%MODULE%"=="force-end" (
    echo ======================================================================
    echo  Running Force End Plan 120 Test Cases Suite (V20-1717)
    echo  Suite: testng-force-end.xml ^| File: src/test/resources/Force_End_Plan_120_Test_Cases.xlsx
    echo ======================================================================
    mvn test -DsuiteXmlFile=testng-force-end.xml %EXTRA_ARGS%
    exit /b %ERRORLEVEL%
)
if /I "%MODULE%"=="forceend" (
    echo ======================================================================
    echo  Running Force End Plan 120 Test Cases Suite (V20-1717)
    echo  Suite: testng-force-end.xml ^| File: src/test/resources/Force_End_Plan_120_Test_Cases.xlsx
    echo ======================================================================
    mvn test -DsuiteXmlFile=testng-force-end.xml %EXTRA_ARGS%
    exit /b %ERRORLEVEL%
)

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
    echo Available modules: login, users, team, departments, resources, rawdata, datastreams, plans, ratetable, assignplans, esign, export, all
    exit /b 1
)

echo ======================================================================
echo  Running Module: %MODULE% from Excel Sheet: '%SHEET%'
echo  Suite: testng-excel.xml ^| File: src/test/resources/manual-testcases.xlsx
echo  (Only rows where Run = Yes in Excel are executed)
echo ======================================================================

mvn test -DsuiteXmlFile=testng-excel.xml -Dexcel.sheet="%SHEET%" %EXTRA_ARGS%
