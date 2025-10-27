#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
sync_folders.py
- One-way safe sync: copy only NEW/UPDATED files from SOURCE to TARGET
- Platforms: Windows / macOS / Linux (Python 3.8+)
- Dependencies: standard library only
"""
from __future__ import annotations

import argparse
import hashlib
import logging
import os
import shutil
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Dict, Iterable, List, Optional, Tuple

# ---------------------------
# Data Models
# ---------------------------

@dataclass
class FileMeta:
    rel: Path
    size: int
    mtime: float  # epoch seconds

@dataclass
class PlanItem:
    rel: Path
    src: Path
    dst: Path
    reason: str   # NEW | DIFF_SIZE | NEWER_MTIME | HASH_DIFF
    size: int

@dataclass
class PlanResult:
    items: List[PlanItem]
    total_bytes: int

# ---------------------------
# Logging Helpers
# ---------------------------

def setup_logger(log_path: Optional[Path]) -> logging.Logger:
    logger = logging.getLogger("sync")
    logger.setLevel(logging.INFO)
    logger.handlers.clear()

    fmt = logging.Formatter("[%(asctime)s][%(levelname)s] %(message)s")
    sh = logging.StreamHandler(sys.stdout)
    sh.setFormatter(fmt)
    logger.addHandler(sh)

    if log_path:
        log_path.parent.mkdir(parents=True, exist_ok=True)
        fh = logging.FileHandler(log_path, encoding="utf-8")
        fh.setFormatter(fmt)
        logger.addHandler(fh)

    return logger

# ---------------------------
# Core Functions
# ---------------------------

def iter_files(base: Path) -> Iterable[Path]:
    for p in base.rglob("*"):
        if p.is_file():
            yield p

def get_meta(base: Path) -> Dict[Path, FileMeta]:
    meta: Dict[Path, FileMeta] = {}
    base = base.resolve()
    for f in iter_files(base):
        rel = f.relative_to(base)
        st = f.stat()
        meta[rel] = FileMeta(rel=rel, size=st.st_size, mtime=st.st_mtime)
    return meta

def hash_file(p: Path, algo: str = "sha256", chunk: int = 1024 * 1024) -> str:
    h = hashlib.new(algo)
    with p.open("rb") as fp:
        while True:
            b = fp.read(chunk)
            if not b:
                break
            h.update(b)
    return h.hexdigest()

def plan_changes(
    source: Path,
    target: Path,
    use_hash: Optional[str] = None,
    mtime_epsilon: float = 1.0,
    include_glob: Optional[str] = None,
    exclude_glob: Optional[str] = None,
) -> PlanResult:
    src_meta = get_meta(source)
    tgt_meta = get_meta(target) if target.exists() else {}

    def included(rel: Path) -> bool:
        s = str(rel).replace("\\", "/")
        if include_glob and not Path(s).match(include_glob):
            return False
        if exclude_glob and Path(s).match(exclude_glob):
            return False
        return True

    items: List[PlanItem] = []
    total = 0

    for rel, sm in src_meta.items():
        if not included(rel):
            continue
        src_path = source / rel
        dst_path = target / rel
        tm = tgt_meta.get(rel)

        if tm is None:
            reason = "NEW"
        else:
            # size check first
            if sm.size != tm.size:
                reason = "DIFF_SIZE"
            # then mtime (favor strictly newer by epsilon)
            elif (sm.mtime - tm.mtime) > mtime_epsilon:
                reason = "NEWER_MTIME"
            elif use_hash:
                # Only if requested and previous checks same
                if not dst_path.exists():
                    reason = "NEW"  # rare race
                else:
                    h1 = hash_file(src_path, use_hash)
                    h2 = hash_file(dst_path, use_hash)
                    if h1 != h2:
                        reason = "HASH_DIFF"
                    else:
                        continue
            else:
                continue

        items.append(PlanItem(rel=rel, src=src_path, dst=dst_path, reason=reason, size=sm.size))
        total += sm.size

    return PlanResult(items=items, total_bytes=total)

# ---------------------------
# Apply with Backup & Rollback
# ---------------------------

class SyncEngine:
    def __init__(self, logger: logging.Logger):
        self.logger = logger
        self.rollback_stack: List[Tuple[str, Path, Optional[Path]]] = []
        # tuples of (op, target_path, backup_path_if_any)
        # op: "CREATE" (delete on rollback) | "OVERWRITE" (restore from backup)

    def _backup_file(self, dst: Path, backup_root: Path) -> Optional[Path]:
        rel = dst
        # derive rel under backup root same structure
        # try to keep relative path consistent to original target root
        # Here we store under backup_root/<drive_or_root_trimmed>/<rel>
        backup_path = backup_root / rel
        backup_path.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(dst, backup_path)
        return backup_path

    def apply(
        self,
        plan: PlanResult,
        backup: bool,
        target_root: Path,
        dry_run: bool = False,
        progress_cb: Optional[Callable[[int, int, PlanItem], None]] = None,
        backup_root: Optional[Path] = None,
    ) -> None:
        total = len(plan.items)
        if total == 0:
            self.logger.info("No changes to apply.")
            return

        if backup and not backup_root:
            ts = time.strftime("%Y%m%d_%H%M%S")
            backup_root = target_root.parent / f"_backup_{target_root.name}_{ts}"
        if backup and backup_root:
            self.logger.info(f"Backup folder: {backup_root}")

        applied = 0
        try:
            for idx, it in enumerate(plan.items, start=1):
                if progress_cb:
                    progress_cb(idx, total, it)
                dst_parent = it.dst.parent
                if not dry_run:
                    dst_parent.mkdir(parents=True, exist_ok=True)

                    if it.dst.exists():
                        backup_path = None
                        if backup:
                            backup_path = self._backup_file(it.dst, backup_root / it.rel.parent if backup_root else it.dst.parent)
                            self.logger.info(f"Backup -> {backup_path}")
                        self.rollback_stack.append(("OVERWRITE", it.dst, backup_path))
                    else:
                        self.rollback_stack.append(("CREATE", it.dst, None))

                    shutil.copy2(it.src, it.dst)
                self.logger.info(f"[{idx}/{total}] {it.reason:11s}  {str(it.rel)}  ({it.size} bytes)")
                applied += 1

            self.logger.info("Apply completed successfully.")
        except Exception as e:
            self.logger.error(f"ERROR during apply: {e}")
            self._rollback()
            raise

    def _rollback(self):
        self.logger.warning("Rolling back changes...")
        # reverse order
        for op, dst, backup_path in reversed(self.rollback_stack):
            try:
                if op == "CREATE":
                    if dst.exists():
                        dst.unlink()
                        self.logger.info(f"Rollback: deleted created {dst}")
                elif op == "OVERWRITE":
                    if backup_path and backup_path.exists():
                        shutil.copy2(backup_path, dst)
                        self.logger.info(f"Rollback: restored {dst} from {backup_path}")
            except Exception as e:
                self.logger.error(f"Rollback failed for {dst}: {e}")
        self.logger.warning("Rollback finished.")

# ---------------------------
# CLI
# ---------------------------

def human_bytes(n: int) -> str:
    for unit in ["B", "KB", "MB", "GB", "TB"]:
        if n < 1024:
            return f"{n:.1f}{unit}" if unit != "B" else f"{n}{unit}"
        n /= 1024
    return f"{n:.1f}PB"

def parse_args(argv: Optional[List[str]] = None) -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Copy only changed files from SOURCE to TARGET safely (mtime/size, optional hash)."
    )
    p.add_argument("--source", "-s", required=True, type=Path, help="Source folder (A)")
    p.add_argument("--target", "-t", required=True, type=Path, help="Target folder (B)")
    p.add_argument("--hash", choices=["md5", "sha256"], help="Optional content hash check (slower)")
    p.add_argument("--backup", action="store_true", help="Backup overwritten files under _backup_<target>_YYYYMMDD_HHMMSS")
    p.add_argument("--yes", "-y", action="store_true", help="Do not prompt, proceed automatically")
    p.add_argument("--plan-only", action="store_true", help="Only show plan (no changes)")
    p.add_argument("--include", help="Glob to include (e.g., '**/*.py')", default=None)
    p.add_argument("--exclude", help="Glob to exclude (e.g., '**/*.log')", default=None)
    p.add_argument("--log", type=Path, help="Log file path (e.g., ./logs/sync.log)")
    p.add_argument("--mtime-eps", type=float, default=1.0, help="MTime epsilon seconds (default 1.0)")
    return p.parse_args(argv)

def main(argv: Optional[List[str]] = None) -> int:
    args = parse_args(argv)
    logger = setup_logger(args.log)

    src: Path = args.source
    dst: Path = args.target

    if not src.exists() or not src.is_dir():
        logger.error(f"Source not found or not a directory: {src}")
        return 2

    logger.info(f"SOURCE: {src}")
    logger.info(f"TARGET: {dst}")
    logger.info(f"Criteria: size/mtime (epsilon={args.mtime_eps}s){' + ' + args.hash if args.hash else ''}")
    if args.include: logger.info(f"Include: {args.include}")
    if args.exclude: logger.info(f"Exclude: {args.exclude}")

    logger.info("Scanning and planning...")
    plan = plan_changes(
        source=src,
        target=dst,
        use_hash=args.hash,
        mtime_epsilon=args.mtime_eps,
        include_glob=args.include,
        exclude_glob=args.exclude,
    )

    logger.info(f"Planned changes: {len(plan.items)} files, total {human_bytes(plan.total_bytes)}")
    # Print preview table
    preview_limit = 20
    for i, it in enumerate(plan.items[:preview_limit], 1):
        logger.info(f" - {it.reason:11s} {str(it.rel)} ({human_bytes(it.size)})")
    if len(plan.items) > preview_limit:
        logger.info(f" ... and {len(plan.items) - preview_limit} more")

    if args.plan_only:
        logger.info("Plan-only mode. No changes applied.")
        return 0

    if not args.yes:
        ans = input("Proceed to apply these changes? [y/N]: ").strip().lower()
        if ans != "y":
            logger.info("Aborted by user.")
            return 1

    engine = SyncEngine(logger)
    try:
        engine.apply(
            plan=plan,
            backup=args.backup,
            target_root=dst,
            dry_run=False,
            backup_root=None,
        )
    except Exception:
        logger.error("Sync failed (rolled back where possible). See log for details.")
        return 3

    logger.info("Sync finished successfully.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
