<#
.SYNOPSIS
환경 설정 및 사용자 정의 파일을 지정된 폴더에 백업합니다.

.DESCRIPTION
VScode, Notepad++, Total Commander, DBeaver 설정, 브라우저 즐겨찾기 등을
'c:\Tools\backup\set_env\YYYYMMDD_HHMMSS' 형식의 폴더에 복사합니다.
대상 폴더가 이미 존재하면 (1), (2)와 같이 괄호 숫자를 추가하여 중복을 방지합니다.

.NOTES
프로젝트 지침 A. PowerShell 스크립트 개발 표준 v1.0 준수.
오류 처리 계층(A2) 및 변수 네이밍(J1) 적용.

.PARAMETER TargetRoot
백업 파일을 저장할 루트 디렉토리입니다. 기본값은 'c:\Tools\backup\set_env'.
#>
[CmdletBinding()]
param(
    [string]$TargetRoot = "c:\Tools\backup\set_env",
    [switch]$Force = $false
)

# A1. 스크립트 헤더 필수 요소 및 A2. 오류 처리 - 레벨 1: 전역 설정
$ErrorActionPreference = "Stop"

# J1. 변수 네이밍 규칙
$DateStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BaseBackupDir = Join-Path -Path $TargetRoot -ChildPath $DateStamp
$BackupDir = $BaseBackupDir

# 백업 대상 정의 (PowerShell과 Python 스크립트에서 참조하기 쉬운 CONFIG 영역 역할)
# $env:USERPROFILE (사용자 홈), $env:LOCALAPPDATA, $env:APPDATA 사용
$Sources = @(
    # VSCode (설정/확장 메타데이터)
    @{ Path = "$env:APPDATA\Code\User"; Desc = "VSCode User Settings" },
    # Notepad++ (설정 파일)
    @{ Path = "$env:APPDATA\Notepad++"; Desc = "Notepad++ Settings" },
    # Total Commander (Wincmd.ini 등, 환경마다 다를 수 있으므로 일반적인 경로 지정)
    @{ Path = "$env:APPDATA\GHISLER"; Desc = "Total Commander Settings" },
    # DBeaver (워크스페이스 설정/스크립트 등)
    @{ Path = "$env:APPDATA\DBeaverData\workspace6"; Desc = "DBeaver Workspace" },
    # Chrome 즐겨찾기/그룹 (UserData\Default 폴더 내 Bookmarks 파일)
    @{ Path = "$env:LOCALAPPDATA\Google\Chrome\User Data\Default\Bookmarks"; Desc = "Chrome Bookmarks" },
    # Edge 즐겨찾기/그룹 (Edge\User Data\Default 폴더 내 Bookmarks 파일)
    @{ Path = "$env:LOCALAPPDATA\Microsoft\Edge\User Data\Default\Bookmarks"; Desc = "Edge Bookmarks" },
    # etc...
    # 기타 설정 파일, 매크로 등 추가
    @{ Path = "$env:USERPROFILE\Documents\Comet"; Desc = "Comet Settings Example" }
)

function Get-UniqueBackupPath {
    param(
        [string]$BasePath
    )
    $Counter = 0
    $UniquePath = $BasePath
    
    # 윈도우 스타일의 중복 폴더명 (날짜_시간 (1)) 생성 로직
    while (Test-Path -Path $UniquePath -PathType Container) {
        $Counter++
        $UniquePath = "$BasePath ($Counter)"
    }
    return $UniquePath
}

function Global-Main {
    # A2. 오류 처리 - 레벨 2: 함수별 try-catch-finally
    try {
        # 1. 대상 폴더 경로 설정 및 생성 (중복 처리)
        Write-Verbose "백업 대상 루트: $TargetRoot"
        $Script:BackupDir = Get-UniqueBackupPath -BasePath $BaseBackupDir
        
        Write-Host "✅ 백업 폴더 생성: $Script:BackupDir" -ForegroundColor Green
        
        # Force 옵션이 없는 경우, Dry-Run 역할로 경로만 표시하고 종료할 수 있음
        if (-not $Force) {
            Write-Warning "경고: -Force 스위치가 없어 실제 파일 복사는 진행되지 않습니다. 실제 백업을 위해 -Force를 추가하십시오."
            return
        }
        
        New-Item -Path $Script:BackupDir -ItemType Directory -Force | Out-Null
        
        # 2. 파일 및 폴더 복사
        foreach ($Source in $Sources) {
            $SourcePath = $Source.Path
            $DestPath = Join-Path -Path $Script:BackupDir -ChildPath (Split-Path -Path $SourcePath -Leaf)
            
            if (Test-Path -Path $SourcePath) {
                Write-Host "처리 중: $($Source.Desc)..."
                
                # Copy-Item을 사용하여 파일/폴더 모두 처리 (폴더는 -Recurse)
                # 대상 경로가 폴더인지 파일인지 확인
                if ((Get-Item -Path $SourcePath).PSIsContainer) {
                    Write-Verbose "폴더 복사: $SourcePath -> $DestPath"
                    Copy-Item -Path $SourcePath -Destination $DestPath -Recurse -Force
                } else {
                    Write-Verbose "파일 복사: $SourcePath -> $Script:BackupDir"
                    # 파일의 경우, BackupDir 바로 아래로 복사
                    Copy-Item -Path $SourcePath -Destination $Script:BackupDir -Force
                }
                Write-Host "✔ 복사 완료: $($Source.Desc)" -ForegroundColor Cyan
            } else {
                Write-Warning "❌ 경고: 경로를 찾을 수 없습니다 - $($Source.Path)"
            }
        }
        
        Write-Host ""
        Write-Host "🎉 백업이 성공적으로 완료되었습니다!" -ForegroundColor Yellow
        Write-Host "저장 위치: $Script:BackupDir" -ForegroundColor Yellow

    } catch {
        # A2. 오류 처리 - 레벨 2: 함수별 try-catch-finally
        Write-Error "💥 치명적인 오류 발생: $($_.Exception.Message)"
        Write-Warning "프로그램을 종료합니다."
    } finally {
        # A2. 오류 처리 - 레벨 2: 리소스 정리 (현재 스크립트에서는 특별한 정리 작업은 없음)
        Write-Verbose "백업 스크립트 실행 종료."
    }
}

# F3. 디버그 모드 전환 지원 (Verbose/Debug 매개변수를 활용)
Global-Main