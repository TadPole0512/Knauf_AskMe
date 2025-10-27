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