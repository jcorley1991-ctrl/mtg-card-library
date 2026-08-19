$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Split-Path -Parent $MyInvocation.MyCommand.Path))
$prefix = 'http://127.0.0.1:8765/'
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add($prefix)

function Get-ContentType([string]$path) {
  switch ([IO.Path]::GetExtension($path).ToLowerInvariant()) {
    '.html' { 'text/html; charset=utf-8' }
    '.js'   { 'application/javascript; charset=utf-8' }
    '.css'  { 'text/css; charset=utf-8' }
    '.json' { 'application/json; charset=utf-8' }
    '.svg'  { 'image/svg+xml' }
    '.png'  { 'image/png' }
    '.jpg'  { 'image/jpeg' }
    '.jpeg' { 'image/jpeg' }
    '.webp' { 'image/webp' }
    default { 'application/octet-stream' }
  }
}

try {
  $listener.Start()
  Write-Host "MTG Card Library is running at $prefix"
  Write-Host 'Your browser will open automatically.'
  Write-Host 'Keep this window open while using the app. Close it to stop the local server.'
  Start-Process $prefix

  while ($listener.IsListening) {
    $context = $listener.GetContext()
    try {
      $relative = [Uri]::UnescapeDataString($context.Request.Url.AbsolutePath.TrimStart('/'))
      if ([string]::IsNullOrWhiteSpace($relative)) { $relative = 'index.html' }
      $candidate = Join-Path $root ($relative -replace '/', [IO.Path]::DirectorySeparatorChar)
      $fullPath = [IO.Path]::GetFullPath($candidate)

      if (-not $fullPath.StartsWith($root, [StringComparison]::OrdinalIgnoreCase)) {
        $context.Response.StatusCode = 403
      }
      elseif (Test-Path -LiteralPath $fullPath -PathType Leaf) {
        $bytes = [IO.File]::ReadAllBytes($fullPath)
        $context.Response.StatusCode = 200
        $context.Response.ContentType = Get-ContentType $fullPath
        $context.Response.Headers['Cache-Control'] = 'no-store'
        $context.Response.ContentLength64 = $bytes.Length
        $context.Response.OutputStream.Write($bytes, 0, $bytes.Length)
      }
      else {
        $context.Response.StatusCode = 404
      }
    }
    catch {
      $context.Response.StatusCode = 500
    }
    finally {
      $context.Response.OutputStream.Close()
    }
  }
}
finally {
  if ($listener.IsListening) { $listener.Stop() }
  $listener.Close()
}
