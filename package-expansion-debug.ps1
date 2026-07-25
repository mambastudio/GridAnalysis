param(
    [ValidateSet("msi", "exe", "app-image")]
    [string] $Type = "msi",
    [string] $AppVersion = "1.0.0"
)

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$targetDir = Join-Path $projectRoot "target"
$inputDir = Join-Path $targetDir "jpackage-input"
$installerDir = Join-Path $targetDir "installer"
$icon = Join-Path $projectRoot "src\main\resources\icons\grid-analysis.ico"
$jpackage = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\jpackage.exe" } else { $null }

if (-not $jpackage -or -not (Test-Path -LiteralPath $jpackage)) {
    $jpackage = Get-ChildItem -Path (Join-Path $env:ProgramFiles "Java\jdk*\bin\jpackage.exe") -File |
        Sort-Object FullName -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not (Test-Path -LiteralPath $jpackage)) {
    throw "jpackage.exe was not found. Set JAVA_HOME to a full JDK (JDK 25 is recommended)."
}
if (-not (Test-Path -LiteralPath $icon)) {
    throw "Installer icon not found: $icon"
}

# These directories contain generated packaging output only. Recreate them so
# removed or upgraded dependencies cannot linger in a later installer.
foreach ($directory in @($inputDir, $installerDir)) {
    if (Test-Path -LiteralPath $directory) {
        $resolvedDirectory = (Resolve-Path -LiteralPath $directory).Path
        if (-not $resolvedDirectory.StartsWith($targetDir, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to remove a packaging directory outside target: $resolvedDirectory"
        }
        Remove-Item -LiteralPath $resolvedDirectory -Recurse -Force
    }
}

& mvn package dependency:copy-dependencies "-Dmaven.test.skip=true" "-DoutputDirectory=$inputDir"
if ($LASTEXITCODE -ne 0) { throw "Maven build failed with exit code $LASTEXITCODE." }

New-Item -ItemType Directory -Force -Path $inputDir, $installerDir | Out-Null
$applicationJar = Get-ChildItem -LiteralPath $targetDir -Filter "grid-analysis-*.jar" -File |
    Where-Object Name -NotLike "original-*" |
    Select-Object -First 1
if (-not $applicationJar) { throw "The GridAnalysis application JAR was not produced." }
Copy-Item -LiteralPath $applicationJar.FullName -Destination $inputDir -Force

$arguments = @(
    "--type", $Type,
    "--name", "Grid Analysis Expansion Debugger",
    "--description", "Interactive Hagrid expansion and ray-traversal laboratory",
    "--vendor", "GridAnalysis",
    "--app-version", $AppVersion,
    "--dest", $installerDir,
    "--input", $inputDir,
    "--main-jar", $applicationJar.Name,
    "--main-class", "gridanalysis.ExpansionDebugLauncher",
    "--icon", $icon
)

if ($Type -in @("msi", "exe")) {
    $arguments += @(
        "--win-dir-chooser",
        "--win-menu",
        "--win-menu-group", "Grid Analysis",
        "--win-shortcut"
    )
}

& $jpackage @arguments
if ($LASTEXITCODE -ne 0) { throw "jpackage failed with exit code $LASTEXITCODE." }

Write-Host "Package created in $installerDir"
