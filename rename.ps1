$oldPackage = "com.mihai.android17helloworld"
$newPackage = "com.mihai.dailyhabit"

# Get all source files
$files = Get-ChildItem -Path . -Recurse -Include *.kt, *.kts, *.xml -File

foreach ($f in $files) {
    $content = Get-Content -Path $f.FullName -Raw
    if ($content -match [regex]::Escape($oldPackage)) {
        Write-Host "Modifying $($f.FullName)"
        $newContent = $content -replace [regex]::Escape($oldPackage), $newPackage
        Set-Content -Path $f.FullName -Value $newContent -NoNewline
    }
}
