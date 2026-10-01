param (
    [Parameter(Mandatory = $false, Position = 0)]
    [string]$Module = "all",

    [Parameter(Mandatory = $false, Position = 1)]
    [string]$Arg1 = "",

    [Parameter(Mandatory = $false, Position = 2)]
    [string]$Arg2 = "",

    [Parameter(Mandatory = $false)]
    [switch]$Headless,

    [Parameter(Mandatory = $false)]
    [switch]$Force,

    [Parameter(Mandatory = $false)]
    [string]$File = "src/test/resources/manual-testcases.xlsx"
)

$isHeadless = $Headless.IsPresent -or ($Arg1 -match "(?i)^-?-?headless$") -or ($Arg2 -match "(?i)^-?-?headless$")
$isForce = $Force.IsPresent -or ($Arg1 -match "(?i)^-?-?force$") -or ($Arg2 -match "(?i)^-?-?force$")

$sheetMap = @{
    "login"          = "Login"
    "employees"      = "Users"
    "users"          = "Users"
    "user"           = "Users"
    "teams"          = "Team"
    "team"           = "Team"
    "departments"    = "Departments"
    "department"     = "Departments"
    "resources"      = "Resources"
    "resource"       = "Resources"
    "refreshcolumns" = "Refresh Columns"
    "rawdata"        = "Refresh Columns"
    "datastreams"    = "Data Streams"
    "datastream"     = "Data Streams"
    "data"           = "Data Streams"
    "plans"          = "Plans"
    "plan"           = "Plans"
    "createplan"     = "Plans"
    "ratetable"      = "Rate Table"
    "ratetables"     = "Rate Table"
    "rate"           = "Rate Table"
    "assignplans"    = "Assign Plans"
    "assignplan"     = "Assign Plans"
    "esign"          = "E-Sign"
}

$moduleKey = $Module.ToLower()

if ($moduleKey -eq "export") {
    Write-Host "======================================================================" -ForegroundColor Cyan
    Write-Host " Running AG-Grid Right-Click Excel Export Suite Across All Modules" -ForegroundColor Green
    Write-Host " Suite: testng-export.xml | Class: tests.export.AgGridExportAllModulesTest" -ForegroundColor Cyan
    Write-Host " Modules: Users, Teams, Departments, Raw Data, Deal Credits, Rate Tables, Plans, Assign Plans" -ForegroundColor Yellow
    Write-Host "======================================================================" -ForegroundColor Cyan

    $mvnArgs = @("test", "-DsuiteXmlFile=testng-export.xml")
    if ($isHeadless) { $mvnArgs += "-Dheadless=true" }
    mvn @mvnArgs
    exit $LASTEXITCODE
}

if ($moduleKey -eq "all") {
    Write-Host "======================================================================" -ForegroundColor Cyan
    Write-Host " Running ALL Test Cases marked 'Run = Yes' across Excel Workbook" -ForegroundColor Green
    Write-Host " Suite: testng-excel.xml | File: $File" -ForegroundColor Cyan
    Write-Host "======================================================================" -ForegroundColor Cyan

    $mvnArgs = @("test", "-DsuiteXmlFile=testng-excel.xml", "-Dexcel.file=$File")
    if ($isHeadless) { $mvnArgs += "-Dheadless=true" }
    if ($isForce) { $mvnArgs += "-Dexcel.force=true" }
    mvn @mvnArgs
    exit $LASTEXITCODE
}

if (-not $sheetMap.ContainsKey($moduleKey)) {
    Write-Host "Unknown module: '$Module'" -ForegroundColor Red
    Write-Host "Available Excel modules:" -ForegroundColor Yellow
    $sheetMap.Keys | Sort-Object | ForEach-Object { Write-Host "  - $_" }
    Write-Host "  - all"
    exit 1
}

$targetSheet = $sheetMap[$moduleKey]
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Running Module: $Module from Excel Sheet: '$targetSheet'" -ForegroundColor Green
Write-Host " Suite: testng-excel.xml | File: $File" -ForegroundColor Cyan
Write-Host " (Only rows where Run = Yes in Excel are executed)" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Cyan

$mvnArgs = @("test", "-DsuiteXmlFile=testng-excel.xml", "-Dexcel.file=$File", "-Dexcel.sheet=$targetSheet")
if ($isHeadless) { $mvnArgs += "-Dheadless=true" }
if ($isForce) { $mvnArgs += "-Dexcel.force=true" }

mvn @mvnArgs
