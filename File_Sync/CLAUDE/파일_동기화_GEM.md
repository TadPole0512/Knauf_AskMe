프로그램 도우미입니다.

요청하신 '두 폴더 비교 및 변경 파일 복사 프로그램' 개발을 위해, 제공해주신 '미디어 자동화 프로젝트 전용 지침'의 원칙(B, E, F, J 등)을 준수하여 실용적이고 안정적인 Python 스크립트를 작성했습니다.

먼저 작업 단계 계획을 제시하고, 이어서 즉시 실행 가능한 전체 코드와 사용 설명서를 제공합니다.

-----

## 🚀 작업 단계 계획 (Work Plan)

1.  **Phase 1: 기반 설계 (Foundation & CLI)**

      * `argparse` 라이브러리를 사용하여 CLI 인터페이스를 설계합니다. (지침 `F2`)
          * 필수 인자: `--source`, `--target`
          * 선택 인자: `--backup`, `--dry-run`, `--yes` (확인 건너뛰기), `--log-level` (지침 `F3`)
      * `logging` 모듈을 설정하여 세션 로그 파일을 생성합니다. (지침 `E1`, `E2`)
          * 로그 파일명은 `sync_YYYYMMDD_HHMMSS.log` 형식으로 합니다.
          * 콘솔과 파일에 동시 출력하며, 로그 레벨을 CLI 인자로 제어합니다.

2.  **Phase 2: 핵심 로직 - 비교 (Comparison Logic)**

      * `pathlib`를 사용하여 소스 및 타겟 폴더를 재귀적으로 스캔하는 함수(`scan_files`)를 구현합니다.
      * 파일 경로는 **상대 경로**를 기준으로 `dict`에 저장합니다. (Key: `relative_path`, Value: `os.stat_result`)
      * 두 폴더의 메타데이터 `dict`를 비교하는 함수(`compare_folders`)를 구현합니다.
          * **신규 (New):** 소스에만 존재
          * **갱신 (Updated):** 소스/타겟 모두 존재하나, 소스의 `mtime`이 더 최신이거나 `size`가 다름
      * 비교 결과를 `files_to_copy` (신규+갱신) 리스트와 `files_to_backup` (갱신) 리스트로 반환합니다.

3.  **Phase 3: 핵심 로직 - 실행 (Execution Logic)**

      * `perform_sync` 함수를 구현합니다.
      * **백업 로직 (지침 `C4` 변형):** `--backup` 플래그가 활성화되면, 타겟 폴더명에 타임스탬프를 붙인 별도 백업 폴더(`_backup_YYYYMMDD_HHMMSS`)를 생성합니다. 덮어쓰기 직전의 원본 파일을 *상대 경로 구조를 유지*하며 이곳에 복사합니다.
      * **복사 로직:** `shutil.copy2`를 사용하여 파일을 복사합니다. (`copy2`는 mtime 등 메타데이터를 보존하므로 다음 동기화 시 불필요한 재복사를 방지합니다.)
      * 복사 전 타겟의 상위 폴더가 존재하지 않으면 `os.makedirs(..., exist_ok=True)`로 생성합니다.
      * 각 파일 처리 시 `try-except`로 I/O 오류를 처리합니다. (지침 `A2` 원칙 적용)

4.  **Phase 4: 사용자 경험(UX) 및 안전장치 (UX & Safety)**

      * **Dry-run (지침 `F2`):** `--dry-run` 모드에서는 실제 파일 복사/백업 작업을 건너뛰고, 수행할 작업 목록만 로그로 출력합니다.
      * **미리보기 및 확인:** 실행할 작업 목록(신규 N개, 갱신 N개)을 요약하여 콘솔에 출력합니다.
      * `--dry-run`이나 `--yes` 플래그가 없으면, 사용자에게 `(y/n)` 확인을 받아야만 실제 복사를 진행합니다.

5.  **Phase 5: 최종 산출물 작성 (Documentation)**

      * 전체 코드를 `sync_folders.py`라는 단일 파일로 패키징합니다.
      * '사용 설명서' (설치, 실행 방법, 옵션)를 작성합니다.
      * '예제 시나리오'와 '체크리스트'를 작성하여 사용자가 쉽게 따라 할 수 있도록 안내합니다.

-----

## 1️⃣ 전체 코드 (sync\_folders.py)

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
sync_folders.py

두 폴더(소스, 타겟)를 비교하여 변경된 파일(신규, 갱신)만
소스에서 타겟으로 복사하는 CLI 스크립트입니다.

요구사항: Python 3.8+

실행 예시:
  # 1. 드라이런 (미리보기): 어떤 파일이 복사될지 확인
  python sync_folders.py --source "C:\Projects" --target "D:\Backup" --dry-run

  # 2. 실제 동기화 (백업 옵션 포함):
  python sync_folders.py --source "C:\Projects" --target "D:\Backup" --backup

  # 3. 확인 프롬프트 없이 즉시 실행:
  python sync_folders.py --source "C:\Projects" --target "D:\Backup" --yes
"""

import argparse
import logging
import os
import shutil
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple, Set

# --- 설정 영역 (지침 B1) ---
TIMESTAMP_FORMAT = "%Y%m%d_%H%M%S"
LOG_FORMAT = "%(asctime)s [%(levelname)-7s] %(message)s"
# -------------------------

def setup_logging(log_level: str) -> None:
    """
    콘솔 및 세션 로그 파일에 대한 로깅을 설정합니다. (지침 E1, E2)
    """
    session_time = datetime.now().strftime(TIMESTAMP_FORMAT)
    log_filename = f"sync_{session_time}.log"
    
    # 로그 레벨 문자열을 logging 상수로 변환
    numeric_level = getattr(logging, log_level.upper(), logging.INFO)
    
    # 루트 로거 설정
    logging.basicConfig(
        level=numeric_level,
        format=LOG_FORMAT,
        handlers=[
            logging.FileHandler(log_filename, encoding='utf-8'),
            logging.StreamHandler(sys.stdout) # 콘솔에도 출력
        ]
    )
    logging.info(f"로그 레벨 '{log_level}'로 설정됨. 로그 파일: {log_filename}")

def scan_files(root_dir: Path) -> Dict[Path, os.stat_result]:
    """
    지정된 폴더를 재귀적으로 스캔하여
    {상대 경로: stat 정보} 딕셔너리를 반환합니다.
    """
    logging.info(f"폴더 스캔 중... (경로: {root_dir})")
    file_meta = {}
    try:
        for path in root_dir.rglob("*"):
            if path.is_file():
                # root_dir 기준의 상대 경로를 Key로 사용 (지침 J1)
                relative_path = path.relative_to(root_dir)
                file_meta[relative_path] = path.stat()
    except FileNotFoundError:
        logging.error(f"오류: 스캔할 폴더를 찾을 수 없습니다: {root_dir}")
        return {}
    except PermissionError:
        logging.error(f"오류: 폴더에 접근할 권한이 없습니다: {root_dir}")
        return {}
        
    logging.info(f"스캔 완료. 총 {len(file_meta)}개의 파일 발견.")
    return file_meta

def compare_folders(
    source_meta: Dict[Path, os.stat_result],
    target_meta: Dict[Path, os.stat_result]
) -> Tuple[List[Path], List[Path]]:
    """
    소스와 타겟의 메타데이터 딕셔너리를 비교합니다.
    반환: (복사할 파일 목록, 백업할 파일 목록)
    """
    files_to_copy: List[Path] = []   # 신규 + 갱신
    files_to_backup: List[Path] = [] # 갱신 (덮어쓰기 대상)
    
    source_paths: Set[Path] = set(source_meta.keys())
    target_paths: Set[Path] = set(target_meta.keys())

    # 1. 신규 파일 (소스 O, 타겟 X)
    new_files = source_paths - target_paths
    files_to_copy.extend(list(new_files))
    
    # 2. 갱신 파일 (소스 O, 타겟 O, 하지만 다름)
    common_files = source_paths.intersection(target_paths)
    for rel_path in common_files:
        source_stat = source_meta[rel_path]
        target_stat = target_meta[rel_path]

        # 변경 감지 기준: 수정 시간(mtime)이 더 최신이거나 파일 크기(size)가 다름
        is_updated = (
            source_stat.st_mtime > target_stat.st_mtime or
            source_stat.st_size != target_stat.st_size
        )

        if is_updated:
            files_to_copy.append(rel_path)
            files_to_backup.append(rel_path) # 갱신 파일은 백업 대상

    # 참고: 타겟에만 있는 파일(삭제 대상)은 이 스크립트의 범위 밖임 (단방향 동기화)
    return sorted(files_to_copy), sorted(files_to_backup)

def perform_sync(
    source_dir: Path,
    target_dir: Path,
    files_to_copy: List[Path],
    files_to_backup: List[Path],
    backup_dir: Path = None,
    is_dry_run: bool = False
) -> None:
    """
    파일 복사 및 백업 작업을 수행합니다. (지침 C3, C4)
    """
    total_files = len(files_to_copy)
    logging.info(f"동기화 시작... (총 {total_files}개 파일)")

    if is_dry_run:
        logging.warning("[DRY-RUN] 실제 파일 작업을 수행하지 않습니다.")

    for i, rel_path in enumerate(files_to_copy, 1):
        source_file = source_dir / rel_path
        target_file = target_dir / rel_path
        
        prefix = f"[{i}/{total_files}]"
        
        try:
            # 1. 백업 (필요시)
            if backup_dir and rel_path in files_to_backup:
                backup_target = backup_dir / rel_path
                log_msg = f"{prefix} [백업] {target_file} -> {backup_target}"
                logging.info(log_msg)
                
                if not is_dry_run:
                    # 백업 폴더의 상위 디렉토리 생성 (지침 J3: 'Why' 설명)
                    # 원본의 폴더 구조를 백업 폴더 내에 그대로 유지하기 위함
                    backup_target.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(target_file, backup_target)

            # 2. 복사
            log_msg = f"{prefix} [복사] {source_file} -> {target_file}"
            logging.info(log_msg)

            if not is_dry_run:
                # 타겟 폴더의 상위 디렉토리 생성
                target_file.parent.mkdir(parents=True, exist_ok=True)
                
                # shutil.copy2 사용: mtime 등 메타데이터 보존이 중요
                # (보존하지 않으면 다음 실행 시 동일 파일을 '갱신'으로 오인)
                shutil.copy2(source_file, target_file)

        except (IOError, OSError) as e:
            logging.error(f"{prefix} [실패] {rel_path} 처리 중 오류: {e}")
        except Exception as e:
            logging.critical(f"{prefix} [치명적 오류] {rel_path} 처리 중 예기치 않은 오류: {e}")

    logging.info(f"동기화 완료. {total_files}개 파일 처리됨.")

def create_backup_dir(target_dir: Path, session_time: str) -> Path:
    """
    타겟 폴더와 동일한 위치에 타임스탬프 기반 백업 폴더를 생성합니다.
    (예: D:\Backup -> D:\Backup_backup_20251027_093000)
    """
    # 지침 C4 (격리 폴더) 원칙을 응용
    backup_dir_name = f"{target_dir.name}_backup_{session_time}"
    backup_dir = target_dir.parent / backup_dir_name
    
    try:
        backup_dir.mkdir(parents=True, exist_ok=True)
        logging.info(f"백업 폴더 생성됨: {backup_dir}")
        return backup_dir
    except PermissionError:
        logging.error(f"백업 폴더 생성 실패 (권한 없음): {backup_dir}")
        return None
    except OSError as e:
        logging.error(f"백업 폴더 생성 실패: {e}")
        return None

def main():
    """
    메인 진입점. CLI 파싱 및 전체 워크플로우 제어. (지침 B1, F2)
    """
    parser = argparse.ArgumentParser(
        description="두 폴더를 비교하여 변경된 파일을 소스에서 타겟으로 복사합니다.",
        epilog="예: python sync_folders.py --source D:\\Source --target E:\\Target --backup"
    )
    
    # 필수 그룹 (지침 F2)
    req = parser.add_argument_group('필수 인자')
    req.add_argument(
        '-s', '--source', 
        type=Path, 
        required=True, 
        help="원본 소스 폴더 경로"
    )
    req.add_argument(
        '-t', '--target', 
        type=Path, 
        required=True, 
        help="복사 대상 타겟 폴더 경로"
    )

    # 옵션 그룹
    opt = parser.add_argument_group('선택 인자')
    opt.add_argument(
        '-b', '--backup', 
        action='store_true', 
        help="활성화 시, 덮어쓰기되는 파일을 별도 백업 폴더에 저장합니다."
    )
    opt.add_argument(
        '-y', '--yes', 
        action='store_true', 
        help="실행 전 묻는 확인 프롬프트를 건너뜁니다."
    )
    opt.add_argument(
        '--log-level', 
        default='INFO', 
        choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'],
        help="로그 상세 수준을 설정합니다. (기본값: INFO)"
    )
    
    # 모드 (지침 F2: Mutually Exclusive)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument(
        '-d', '--dry-run', 
        action='store_true', 
        help="[안전 모드] 실제 파일 작업을 수행하지 않고, 수행될 작업 목록만 출력합니다."
    )

    args = parser.parse_args()

    # --- 1. 로깅 설정 ---
    setup_logging(args.log_level)
    session_time = datetime.now().strftime(TIMESTAMP_FORMAT)
    logging.info(f"--- 동기화 세션 시작 ({session_time}) ---")
    logging.info(f"소스: {args.source}")
    logging.info(f"타겟: {args.target}")
    logging.info(f"옵션: Dry-Run={args.dry_run}, Backup={args.backup}, Skip-Confirm={args.yes}")

    # --- 2. 경로 유효성 검사 ---
    if not args.source.is_dir():
        logging.error(f"소스 폴더를 찾을 수 없습니다: {args.source}")
        sys.exit(1)
    if not args.target.exists():
        logging.warning(f"타겟 폴더가 존재하지 않습니다. 새로 생성합니다: {args.target}")
        try:
            args.target.mkdir(parents=True, exist_ok=True)
        except PermissionError:
            logging.error(f"타겟 폴더 생성 실패 (권한 없음): {args.target}")
            sys.exit(1)
    elif not args.target.is_dir():
        logging.error(f"타겟 경로가 폴더가 아닙니다: {args.target}")
        sys.exit(1)

    # --- 3. 비교 ---
    source_meta = scan_files(args.source)
    target_meta = scan_files(args.target)
    
    files_to_copy, files_to_backup = compare_folders(source_meta, target_meta)
    
    num_new = len(files_to_copy) - len(files_to_backup)
    num_updated = len(files_to_backup)

    if not files_to_copy:
        logging.info("--- 이미 최신 상태입니다. 동기화할 파일이 없습니다. ---")
        sys.exit(0)

    # --- 4. 미리보기 및 확인 ---
    print("\n" + "="*50)
    print("         <<< 동기화 작업 미리보기 >>>")
    print(f"  - 신규 파일 (복사): {num_new} 개")
    print(f"  - 갱신 파일 (덮어쓰기): {num_updated} 개")
    print(f"  - 총 작업량: {len(files_to_copy)} 개")
    print("="*50 + "\n")
    
    if args.log_level == 'DEBUG':
        logging.debug("--- 복사 대상 상세 목록 (DEBUG) ---")
        for f in files_to_copy:
            status = "갱신" if f in files_to_backup else "신규"
            logging.debug(f"  [{status}] {f}")
        logging.debug("--- 상세 목록 끝 ---")

    if args.dry_run:
        logging.warning("[DRY-RUN] 모드입니다. 작업을 시뮬레이션합니다.")
    elif not args.yes:
        try:
            confirm = input(f"작업을 진행하시겠습니까? (y/n): ").strip().lower()
            if confirm not in ('y', 'yes'):
                logging.warning("--- 사용자가 작업을 취소했습니다. ---")
                sys.exit(0)
        except KeyboardInterrupt:
            logging.warning("\n--- 사용자가 작업을 중단했습니다. (Ctrl+C) ---")
            sys.exit(1)

    # --- 5. 백업 폴더 준비 ---
    backup_dir = None
    if args.backup and not args.dry_run:
        if not files_to_backup:
            logging.info("백업 옵션이 활성화되었으나 갱신(덮어쓰기)할 파일이 없습니다.")
        else:
            backup_dir = create_backup_dir(args.target, session_time)
            if backup_dir is None:
                logging.error("백업 디렉토리 생성에 실패하여 동기화를 중단합니다.")
                sys.exit(1)

    # --- 6. 실행 ---
    perform_sync(
        args.source,
        args.target,
        files_to_copy,
        files_to_backup,
        backup_dir,
        args.dry_run
    )
    
    logging.info("--- 모든 작업이 성공적으로 완료되었습니다. ---")

# --- 메인 실행 (지침 B3) ---
if __name__ == "__main__":
    main()
```

-----

## 2️⃣ 사용 설명서

### 설치 방법

이 스크립트는 Python 3.8 이상의 표준 라이브러리만 사용하므로 **별도 설치가 필요 없습니다.**

1.  위 코드를 `sync_folders.py`라는 이름의 파일로 저장합니다.
2.  터미널(CMD, PowerShell 또는 Bash)에서 Python 버전을 확인합니다.
    ```bash
    python --version
    # 또는
    python3 --version

    # Python 3.8.0 이상이 표시되어야 합니다.
    ```

### 실행 방법

터미널을 열고 `sync_folders.py` 파일이 있는 위치로 이동한 뒤 다음 명령어를 사용합니다.

```bash
# 기본 도움말 확인
python sync_folders.py --help

# [필수] 기본 실행 (소스 경로와 타겟 경로 지정)
# (경로에 공백이 있으면 반드시 따옴표로 감싸야 합니다.)
python sync_folders.py --source "C:\My Documents" --target "D:\Backups\Docs"

# [권장] Dry-Run: 실제 복사 없이 미리보기
python sync_folders.py --source "/Users/Me/Projects" --target "/Volumes/USB/Backup" --dry-run

# [안전] 백업 옵션: 덮어쓰기되는 파일을 별도 백업 폴더에 보관
python sync_folders.py --source "C:\Source" --target "D:\Target" --backup

# [자동화] 확인 프롬프트 없이 즉시 실행 (주의!)
python sync_folders.py --source "C:\Source" --target "D:\Target" --yes

# [디버그] 상세 로그 출력
python sync_folders.py --source "C:\Source" --target "D:\Target" --log-level DEBUG
```

### 로그 파일 확인

스크립트를 실행한 폴더에 `sync_YYYYMMDD_HHMMSS.log` 형식의 로그 파일이 생성됩니다. 동기화 중 발생한 모든 작업 내역과 오류를 이 파일에서 확인할 수 있습니다.

-----

## 3️⃣ 예제 시나리오

### 시나리오 A: 개발 프로젝트 백업 (Windows)

  * **목적:** `C:\Projects\MyApp` 폴더의 변경 사항을 `D:\Backup\MyApp`으로 동기화. 덮어쓰는 파일은 안전하게 백업.
  * **상황:**
      * `C:\Projects\MyApp\main.py` (수정됨)
      * `C:\Projects\MyApp\utils.py` (신규 생성)
      * `C:\Projects\MyApp\README.md` (변경 없음)
  * **명령:**
    ```bash
    python sync_folders.py --source "C:\Projects\MyApp" --target "D:\Backup\MyApp" --backup
    ```
  * **실행 결과:**
    1.  스크립트가 `main.py` (갱신), `utils.py` (신규) 2개 파일을 감지합니다.
    2.  `작업을 진행하시겠습니까? (y/n):` 프롬프트가 표시됩니다.
    3.  `y` 입력 시:
          * `D:\Backup_backup_20251027_093600` 같은 백업 폴더가 생성됩니다.
          * `D:\Backup\MyApp\main.py` (기존 파일)이 백업 폴더로 복사됩니다.
          * `C:\Projects\MyApp\main.py` -\> `D:\Backup\MyApp\main.py` (덮어쓰기)
          * `C:\Projects\MyApp\utils.py` -\> `D:\Backup\MyApp\utils.py` (신규 복사)
    4.  `README.md`는 변경이 없으므로 건너뜁니다.
    5.  로그 파일(`sync_...log`)에 모든 처리 내역이 기록됩니다.

### 시나리오 B: USB 메모리로 문서 동기화 (macOS/Linux)

  * **목적:** 내 문서 폴더를 USB 메모리로 빠르고 안전하게 동기화.

  * **상황:** USB를 방금 꽂았고, 어떤 파일이 변경되었는지 확실하지 않음.

  * **명령 (1단계 - 미리보기):**

    ```bash
    python3 sync_folders.py --source "/Users/MyUser/Documents" --target "/Volumes/MyUSB/Docs_Backup" --dry-run
    ```

  * **실행 결과 (1단계):**

      * 콘솔에 `[DRY-RUN]` 태그와 함께 복사/백업될 파일 목록이 로그로 출력됩니다.
      * `신규 파일: 15 개`, `갱신 파일: 4 개`와 같은 요약 정보가 표시됩니다.
      * 실제 파일은 전혀 변경되지 않습니다.

  * **명령 (2단계 - 실제 실행):**

    ```bash
    python3 sync_folders.py --source "/Users/MyUser/Documents" --target "/Volumes/MyUSB/Docs_Backup"
    ```

  * **실행 결과 (2단계):**

      * 미리보기에서 확인한 19개 파일에 대한 복사/덮어쓰기 작업을 진행할지 묻습니다.
      * `y` 입력 시 작업이 수행됩니다.

-----

## 4️⃣ 체크리스트

### ✅ 실행 전 준비

  * [ ] Python 3.8 이상이 설치되어 있습니까?
  * [ ] 소스 폴더(`--source`) 경로가 정확합니까?
  * [ ] 타겟 폴더(`--target`) 경로가 정확합니까?
  * [ ] 타겟 폴더(또는 드라이브)에 쓰기 권한이 있습니까?
  * [ ] 타겟 드라이브에 복사할 파일 용량만큼 충분한 저장 공간이 있습니까?

### ✅ 실행 중

  * [ ] (최초 실행 시) `--dry-run` 옵션으로 미리보기를 먼저 실행했습니까?
  * [ ] 미리보기 화면에 표시된 '신규' 및 '갱신' 파일 수가 예상과 일치합니까?
  * [ ] 중요한 파일이 덮어쓰기(갱신)될 경우, `--backup` 옵션을 활성화했습니까?
  * [ ] `(y/n)` 확인 프롬프트에서 `y`를 누르기 전 다시 한번 검토했습니까?

### ✅ 실행 후

  * [ ] 콘솔 마지막에 "모든 작업이 성공적으로 완료되었습니다." 메시지가 표시되었습니까?
  * [ ] 오류 메시지(ERROR, CRITICAL)가 표시되지 않았습니까?
  * [ ] (오류 발생 시) 생성된 `sync_...log` 파일을 열어 오류 원인을 확인했습니까?
  * [ ] 타겟 폴더에 파일이 정상적으로 복사/갱신되었는지 무작위로 몇 개 확인했습니까?
  * [ ] (`--backup` 사용 시) 백업 폴더가 생성되었고, 갱신된 파일의 원본이 그 안에 있습니까?


---------------------------------------------------------------------


소스 폴더 : c:\staybymeerp-Intellij\
대상 폴더 : c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\

위의 소스, 대상 폴더로 수정해줘


---------------------------------------------------------------------

네, 알겠습니다.

제공해주신 경로를 반영하여 `sync_folders.py` 스크립트의 실행 예제를 수정했습니다. Windows 경로에 포함된 백슬래시(`\`)와 공백(지금은 없지만)을 안전하게 처리하기 위해 **경로 전체를 큰따옴표(`"`)로 감싸는 것**이 좋습니다.

-----

## 💾 수정된 실행 예제

지정한 소스 및 대상 폴더를 위한 명령어입니다.

### 1\. (권장) Dry-Run: 미리보기

먼저 **`--dry-run`** 옵션으로 어떤 파일이 복사될지 시뮬레이션합니다.

```bash
python sync_folders.py --source "c:\staybymeerp-Intellij" --target "c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij" --dry-run
```

### 2\. 기본 동기화 실행

미리보기 결과가 정상이면, `--dry-run`을 빼고 실제 동기화를 실행합니다.

```bash
python sync_folders.py --source "c:\staybymeerp-Intellij" --target "c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij"
```

### 3\. 백업 옵션 포함 실행

대상 폴더의 파일이 덮어쓰기 되기 전에 별도 폴더에 백업하려면 **`--backup`** 옵션을 추가합니다.

```bash
python sync_folders.py --source "c:\staybymeerp-Intellij" --target "c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij" --backup
```

-----

**참고:**

  * 명령줄에서는 경로 끝에 붙는 백슬래시(`\`)는 입력하지 않아도 `pathlib` 라이브러리가 정상적으로 인식합니다. 따옴표로 감싸는 것이 더 중요합니다.