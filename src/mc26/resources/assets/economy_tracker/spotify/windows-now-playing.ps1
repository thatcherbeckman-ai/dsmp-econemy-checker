# Local read-only Windows media information. No accounts, network, playback control or audio capture.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)

function Send-Frame($Frame) {
    $Frame.v = 1
    [Console]::Out.WriteLine(($Frame | ConvertTo-Json -Compress -Depth 4))
    [Console]::Out.Flush()
}

try {
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType=WindowsRuntime]
    $propertiesType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType=WindowsRuntime]
    $asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
        $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetGenericArguments().Length -eq 1 -and $_.GetParameters().Length -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
    } | Select-Object -First 1
    if ($null -eq $asTask) { throw 'WinRT unavailable' }
    function Await-Media($Operation, [Type]$ResultType) {
        $task = $asTask.MakeGenericMethod($ResultType).Invoke($null, @($Operation))
        if (-not $task.Wait(5000)) { throw 'Media request timed out' }
        return $task.Result
    }
    $owner = [System.Diagnostics.Process]::GetProcessById([int]$env:THATCHACAT_SPOTIFY_PARENT_PID)
    $null = $owner.Handle # Hold this process identity so a reused PID cannot keep the helper alive.
    $manager = Await-Media ($managerType::RequestAsync()) $managerType
} catch {
    Send-Frame @{ state = 'error' }
    exit
}

while (-not $owner.HasExited) {
    try {
        $selected = $null
        foreach ($session in $manager.GetSessions()) {
            $source = [string]$session.SourceAppUserModelId
            if ($source -notmatch '^(?i:spotify(?:\.exe)?|SpotifyAB\.SpotifyMusic_[a-z0-9]+!Spotify)$') { continue }
            $state = [string]$session.GetPlaybackInfo().PlaybackStatus
            if ($state -ne 'Playing' -and $state -ne 'Paused') { continue }
            if ($null -eq $selected) { $selected = $session }
            if ($state -eq 'Playing') { $selected = $session; break }
        }
        if ($null -eq $selected) {
            Send-Frame @{ state = 'idle' }
        } else {
            $properties = Await-Media ($selected.TryGetMediaPropertiesAsync()) $propertiesType
            $timeline = $selected.GetTimelineProperties()
            $duration = [Math]::Max(0, ($timeline.EndTime - $timeline.StartTime).TotalMilliseconds)
            $position = [Math]::Max(0, ($timeline.Position - $timeline.StartTime).TotalMilliseconds)
            $title = [string]$properties.Title
            $artist = [string]$properties.Artist
            if ($title.Length -gt 512) { $title = $title.Substring(0, 512) }
            if ($artist.Length -gt 512) { $artist = $artist.Substring(0, 512) }
            Send-Frame @{
                state = ([string]$selected.GetPlaybackInfo().PlaybackStatus).ToLowerInvariant()
                source = [string]$selected.SourceAppUserModelId
                title = $title; artist = $artist
                durationMs = [long]$duration; positionMs = [long]$position
            }
        }
    } catch {
        Send-Frame @{ state = 'error' }
    }
    Start-Sleep -Milliseconds 1500
}
