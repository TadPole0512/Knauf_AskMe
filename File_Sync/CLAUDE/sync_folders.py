#!/usr/bin/env python3
"""
폴더 동기화 프로그램 (Folder Sync Utility)
버전: 1.0.0
작성일: 2025-10-27

기능:
- 두 폴더 비교 및 변경 파일 감지
- 선택적 파일 복사 (소스 → 타겟)
- 로그 기록 및 안전장치

요구사항: Python 3.8+
"""

import os
import shutil
import argparse
import logging
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Tuple, Optional
import sys


# ===== 설정 영역 =====
class Config:
    """프로그램 설정"""
    LOG_FORMAT = '%(asctime)s - %(levelname)s - %(message)s'
    DATE_FORMAT = '%Y-%m-%d %H:%M:%S'
    BUFFER_SIZE = 1024 * 1024  # 1MB 버퍼


# ===== 핵심 로직 =====
class FileInfo:
    """파일 메타데이터 저장"""
    def __init__(self, path: Path, base_dir: Path):
        self.abs_path = path
        self.rel_path = path.relative_to(base_dir)
        stat = path.stat()
        self.mtime = stat.st_mtime
        self.size = stat.st_size
    
    def __repr__(self) -> str:
        return f"FileInfo({self.rel_path}, {self.size}B, {datetime.fromtimestamp(self.mtime)})"


class FolderScanner:
    """폴더 스캔 및 파일 목록 생성"""
    
    def __init__(self, root_path: Path):
        self.root = root_path.resolve()
        if not self.root.exists():
            raise FileNotFoundError(f"폴더를 찾을 수 없습니다: {self.root}")
        if not self.root.is_dir():
            raise NotADirectoryError(f"디렉토리가 아닙니다: {self.root}")
    
    def scan(self) -> Dict[Path, FileInfo]:
        """
        모든 파일 스캔
        
        Returns:
            {상대경로: FileInfo} 딕셔너리
        """
        files = {}
        try:
            for entry in self.root.rglob('*'):
                if entry.is_file():
                    try:
                        file_info = FileInfo(entry, self.root)
                        files[file_info.rel_path] = file_info
                    except (OSError, PermissionError) as e:
                        logging.warning(f"파일 접근 실패 (건너뜀): {entry} - {e}")
        except Exception as e:
            logging.error(f"폴더 스캔 중 오류: {e}")
            raise
        
        return files


class FileSynchronizer:
    """파일 동기화 메인 클래스"""
    
    def __init__(self, source: Path, target: Path, dry_run: bool = False):
        self.source = Path(source).resolve()
        self.target = Path(target).resolve()
        self.dry_run = dry_run
        
        # 로깅 설정
        log_file = self.target / f"sync_log_{datetime.now():%Y%m%d_%H%M%S}.txt"
        logging.basicConfig(
            level=logging.INFO,
            format=Config.LOG_FORMAT,
            handlers=[
                logging.FileHandler(log_file, encoding='utf-8'),
                logging.StreamHandler(sys.stdout)
            ]
        )
        
        logging.info(f"=== 동기화 세션 시작 ===")
        logging.info(f"소스: {self.source}")
        logging.info(f"타겟: {self.target}")
        logging.info(f"모드: {'시뮬레이션' if dry_run else '실제 복사'}")
    
    def analyze_changes(self) -> Tuple[List[Path], List[Path]]:
        """
        변경된 파일 분석
        
        Returns:
            (신규 파일 목록, 수정된 파일 목록)
        """
        logging.info("📁 폴더 스캔 중...")
        
        source_scanner = FolderScanner(self.source)
        source_files = source_scanner.scan()
        
        target_scanner = FolderScanner(self.target)
        target_files = target_scanner.scan()
        
        logging.info(f"소스: {len(source_files)}개 파일")
        logging.info(f"타겟: {len(target_files)}개 파일")
        
        # 변경 파일 분류
        new_files = []
        modified_files = []
        
        for rel_path, src_info in source_files.items():
            if rel_path not in target_files:
                # 타겟에 없음 → 신규
                new_files.append(rel_path)
            else:
                tgt_info = target_files[rel_path]
                # 수정 시간 또는 크기가 다름 → 수정됨
                if (src_info.mtime > tgt_info.mtime or 
                    src_info.size != tgt_info.size):
                    modified_files.append(rel_path)
        
        return new_files, modified_files
    
    def display_changes(self, new_files: List[Path], modified_files: List[Path]) -> None:
        """변경 파일 목록 출력"""
        print("\n" + "="*60)
        print("📋 변경 파일 요약")
        print("="*60)
        
        if new_files:
            print(f"\n🆕 신규 파일 ({len(new_files)}개):")
            for path in new_files[:10]:  # 최대 10개만 표시
                print(f"  + {path}")
            if len(new_files) > 10:
                print(f"  ... 외 {len(new_files) - 10}개")
        
        if modified_files:
            print(f"\n🔄 수정된 파일 ({len(modified_files)}개):")
            for path in modified_files[:10]:
                src_size = (self.source / path).stat().st_size
                tgt_size = (self.target / path).stat().st_size
                print(f"  ↻ {path}")
                print(f"     소스: {self._format_size(src_size)} | "
                      f"타겟: {self._format_size(tgt_size)}")
            if len(modified_files) > 10:
                print(f"  ... 외 {len(modified_files) - 10}개")
        
        if not new_files and not modified_files:
            print("\n✅ 변경 사항 없음 - 두 폴더가 동일합니다.")
        
        print("\n" + "="*60)
    
    def copy_files(self, file_list: List[Path]) -> Tuple[int, int]:
        """
        파일 복사 실행
        
        Returns:
            (성공 개수, 실패 개수)
        """
        success_count = 0
        fail_count = 0
        
        for i, rel_path in enumerate(file_list, 1):
            src_file = self.source / rel_path
            tgt_file = self.target / rel_path
            
            print(f"[{i}/{len(file_list)}] 복사 중: {rel_path}")
            
            try:
                if self.dry_run:
                    logging.info(f"[시뮬레이션] {rel_path}")
                else:
                    # 타겟 디렉토리 생성
                    tgt_file.parent.mkdir(parents=True, exist_ok=True)
                    
                    # 안전한 복사 (임시 파일 사용)
                    tmp_file = tgt_file.with_suffix('.tmp')
                    shutil.copy2(src_file, tmp_file)
                    tmp_file.replace(tgt_file)  # 원자적 교체
                    
                    logging.info(f"✓ 복사 완료: {rel_path}")
                
                success_count += 1
                
            except Exception as e:
                logging.error(f"✗ 복사 실패: {rel_path} - {e}")
                fail_count += 1
        
        return success_count, fail_count
    
    def run(self) -> None:
        """동기화 실행"""
        try:
            # 1. 변경 분석
            new_files, modified_files = self.analyze_changes()
            all_changes = new_files + modified_files
            
            # 2. 미리보기
            self.display_changes(new_files, modified_files)
            
            if not all_changes:
                logging.info("동기화 완료 (변경 없음)")
                return
            
            # 3. 사용자 확인
            if not self.dry_run:
                confirm = input(f"\n총 {len(all_changes)}개 파일을 복사하시겠습니까? (y/N): ")
                if confirm.lower() != 'y':
                    print("❌ 취소되었습니다.")
                    logging.info("사용자 취소")
                    return
            
            # 4. 복사 실행
            print("\n🚀 파일 복사 시작...")
            success, fail = self.copy_files(all_changes)
            
            # 5. 결과 출력
            print("\n" + "="*60)
            print("📊 동기화 결과")
            print("="*60)
            print(f"✅ 성공: {success}개")
            print(f"❌ 실패: {fail}개")
            print("="*60)
            
            logging.info(f"동기화 완료 - 성공: {success}, 실패: {fail}")
            
        except KeyboardInterrupt:
            print("\n\n⚠️  사용자에 의해 중단되었습니다.")
            logging.warning("사용자 중단 (Ctrl+C)")
        except Exception as e:
            logging.error(f"치명적 오류: {e}", exc_info=True)
            raise
    
    @staticmethod
    def _format_size(size: int) -> str:
        """파일 크기 포맷팅"""
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024:
                return f"{size:.1f}{unit}"
            size /= 1024
        return f"{size:.1f}TB"


# ===== CLI 인터페이스 =====
def main():
    parser = argparse.ArgumentParser(
        description='두 폴더를 비교하여 변경된 파일을 동기화합니다.',
        epilog='예시: python sync_folders.py --source ./project --target ./backup',
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    
    parser.add_argument(
        '--source', '-s',
        type=str,
        required=True,
        help='소스 폴더 경로 (복사할 파일이 있는 폴더)'
    )
    
    parser.add_argument(
        '--target', '-t',
        type=str,
        required=True,
        help='타겟 폴더 경로 (파일이 복사될 폴더)'
    )
    
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='실제로 복사하지 않고 시뮬레이션만 실행'
    )
    
    args = parser.parse_args()
    
    # 실행
    try:
        syncer = FileSynchronizer(
            source=args.source,
            target=args.target,
            dry_run=args.dry_run
        )
        syncer.run()
    except Exception as e:
        print(f"\n❌ 오류 발생: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()