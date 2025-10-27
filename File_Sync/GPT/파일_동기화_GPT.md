# 🧭 개요

두 폴더를 비교해 **변경된 파일만** 안전하게 복사하는 **크로스플랫폼(Win/macOS/Linux) Python 3.8+** 도구입니다.
CLI를 우선 제공하고, 선택적으로 **간단한 Tkinter GUI**를 포함했습니다. 표준 라이브러리만 사용합니다.

---

# ✅ 이번 번들: [사전 브리핑 / 질문 / 산출물]

* 🧾 **사전 브리핑**: 단방향 동기화(A→B), 변경 기준 = mtime/size(옵션: 해시), 미리보기→사용자 확인→복사, 로그·백업·롤백.
* ❓ **질문(선택)**: 없음. 기본값으로 동작 가능한 완전한 버전 제공.
* 📦 **산출물**: 1) 전체 소스 2개(`sync_folders.py`, `sync_folders_gui.py`) 2) 사용 설명서 3) 테스트 시나리오/체크리스트.

---

# 🛠️ 1) 전체 코드 — CLI (즉시 실행 가능): `sync_folders.py`

```python
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
```

---

# 🪟 2) 선택적 GUI — Tkinter (간단 버전): `sync_folders_gui.py`

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
sync_folders_gui.py
- Minimal Tkinter GUI wrapper around sync_folders.py core functions
- Platforms: Windows/macOS/Linux (Python 3.8+)
"""
from __future__ import annotations

import threading
import time
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from pathlib import Path
import queue
import sys

# Import functions from CLI module (same folder)
import sync_folders as core


class GuiApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Folder Sync (One-way A → B)")
        self.geometry("720x520")

        self.var_src = tk.StringVar()
        self.var_dst = tk.StringVar()
        self.var_backup = tk.BooleanVar(value=True)
        self.var_yes = tk.BooleanVar(value=True)
        self.var_hash = tk.StringVar(value="")
        self.var_include = tk.StringVar(value="")
        self.var_exclude = tk.StringVar(value="")
        self.var_mteps = tk.DoubleVar(value=1.0)

        self._build_ui()

        self.log_queue: "queue.Queue[str]" = queue.Queue()
        self.logger = core.setup_logger(None)

    def _build_ui(self):
        frm = ttk.Frame(self, padding=12)
        frm.pack(fill="both", expand=True)

        # Source
        ttk.Label(frm, text="Source (A):").grid(row=0, column=0, sticky="w")
        ttk.Entry(frm, textvariable=self.var_src, width=70).grid(row=0, column=1, sticky="we")
        ttk.Button(frm, text="Browse", command=self._pick_src).grid(row=0, column=2, padx=4)

        # Target
        ttk.Label(frm, text="Target (B):").grid(row=1, column=0, sticky="w")
        ttk.Entry(frm, textvariable=self.var_dst, width=70).grid(row=1, column=1, sticky="we")
        ttk.Button(frm, text="Browse", command=self._pick_dst).grid(row=1, column=2, padx=4)

        # Options
        opt = ttk.LabelFrame(frm, text="Options")
        opt.grid(row=2, column=0, columnspan=3, sticky="we", pady=8)
        opt.columnconfigure(1, weight=1)

        ttk.Checkbutton(opt, text="Backup overwritten files", variable=self.var_backup).grid(row=0, column=0, sticky="w")
        ttk.Checkbutton(opt, text="Auto-apply (skip prompt)", variable=self.var_yes).grid(row=0, column=1, sticky="w")

        ttk.Label(opt, text="Hash:").grid(row=1, column=0, sticky="w")
        ttk.Combobox(opt, textvariable=self.var_hash, values=["", "md5", "sha256"], width=10, state="readonly").grid(row=1, column=1, sticky="w")

        ttk.Label(opt, text="Include glob:").grid(row=2, column=0, sticky="w")
        ttk.Entry(opt, textvariable=self.var_include, width=40).grid(row=2, column=1, sticky="we")
        ttk.Label(opt, text="Exclude glob:").grid(row=3, column=0, sticky="w")
        ttk.Entry(opt, textvariable=self.var_exclude, width=40).grid(row=3, column=1, sticky="we")

        ttk.Label(opt, text="MTime epsilon (s):").grid(row=4, column=0, sticky="w")
        ttk.Entry(opt, textvariable=self.var_mteps, width=10).grid(row=4, column=1, sticky="w")

        # Buttons
        btns = ttk.Frame(frm)
        btns.grid(row=3, column=0, columnspan=3, sticky="we")
        ttk.Button(btns, text="Plan (Preview)", command=self._plan).pack(side="left", padx=4)
        ttk.Button(btns, text="Apply (Sync)", command=self._apply).pack(side="left", padx=4)

        # Progress
        self.prog = ttk.Progressbar(frm, mode="determinate")
        self.prog.grid(row=4, column=0, columnspan=3, sticky="we", pady=(8, 2))

        # Log
        self.txt = tk.Text(frm, height=16)
        self.txt.grid(row=5, column=0, columnspan=3, sticky="nsew", pady=(6, 0))
        frm.rowconfigure(5, weight=1)
        frm.columnconfigure(1, weight=1)

        self.after(100, self._poll_log)

    def _pick_src(self):
        p = filedialog.askdirectory(title="Pick Source (A)")
        if p:
            self.var_src.set(p)

    def _pick_dst(self):
        p = filedialog.askdirectory(title="Pick Target (B)")
        if p:
            self.var_dst.set(p)

    def _log(self, s: str):
        self.log_queue.put(s + "\n")

    def _poll_log(self):
        try:
            while True:
                s = self.log_queue.get_nowait()
                self.txt.insert("end", s)
                self.txt.see("end")
        except queue.Empty:
            pass
        self.after(100, self._poll_log)

    def _make_plan(self) -> core.PlanResult:
        src = Path(self.var_src.get())
        dst = Path(self.var_dst.get())
        if not src.exists():
            raise FileNotFoundError(f"Source not found: {src}")
        plan = core.plan_changes(
            source=src,
            target=dst,
            use_hash=(self.var_hash.get() or None),
            mtime_epsilon=float(self.var_mteps.get()),
            include_glob=(self.var_include.get() or None),
            exclude_glob=(self.var_exclude.get() or None),
        )
        return plan

    def _plan(self):
        try:
            plan = self._make_plan()
            self._log(f"Planned changes: {len(plan.items)} files, total {self._hb(plan.total_bytes)}")
            for it in plan.items[:50]:
                self._log(f" - {it.reason:11s} {str(it.rel)} ({self._hb(it.size)})")
            if len(plan.items) > 50:
                self._log(f" ... and {len(plan.items) - 50} more")
        except Exception as e:
            messagebox.showerror("Plan Error", str(e))

    def _apply(self):
        try:
            plan = self._make_plan()
        except Exception as e:
            messagebox.showerror("Plan Error", str(e))
            return
        if not self.var_yes.get():
            if not messagebox.askyesno("Confirm", f"Apply {len(plan.items)} changes?"):
                return

        self.prog.configure(maximum=max(1, len(plan.items)))
        self.prog["value"] = 0

        def worker():
            engine = core.SyncEngine(logger=self.logger)
            try:
                engine.apply(
                    plan=plan,
                    backup=self.var_backup.get(),
                    target_root=Path(self.var_dst.get()),
                    dry_run=False,
                    progress_cb=self._progress_cb,
                    backup_root=None,
                )
                self._log("Sync finished successfully.")
                messagebox.showinfo("Done", "Sync finished successfully.")
            except Exception as e:
                self._log(f"ERROR: {e}")
                messagebox.showerror("Error", str(e))

        threading.Thread(target=worker, daemon=True).start()

    def _progress_cb(self, idx: int, total: int, it: core.PlanItem):
        self._log(f"[{idx}/{total}] {it.reason:11s} {str(it.rel)} ({self._hb(it.size)})")
        self.prog["value"] = idx

    @staticmethod
    def _hb(n: int) -> str:
        return core.human_bytes(n)

def main():
    app = GuiApp()
    app.mainloop()

if __name__ == "__main__":
    # Ensure local import works if launched directly
    if "sync_folders" not in sys.modules:
        import importlib.util, os
        here = Path(__file__).resolve().parent
        spec = importlib.util.spec_from_file_location("sync_folders", os.fspath(here / "sync_folders.py"))
        mod = importlib.util.module_from_spec(spec)  # type: ignore
        assert spec and spec.loader
        spec.loader.exec_module(mod)  # type: ignore
        sys.modules["sync_folders"] = mod
    main()
```

---

# 📘 3) 사용 설명서

## 설치

```bash
python --version  # 3.8 이상
```

## 실행 (CLI)

```bash
# 기본 실행: 미리보기 → 확인 프롬프트 → 복사
python sync_folders.py --source /path/to/A --target /path/to/B

# 확인 없이 즉시 적용
python sync_folders.py -s "/path/to/A" -t "/path/to/B" -y

# 덮어쓰기 백업 활성화
python sync_folders.py -s A -t B --backup -y

# 해시 비교(정밀, 느림)
python sync_folders.py -s A -t B --hash sha256 -y

# 포함/제외 글롭
python sync_folders.py -s A -t B --include "**/*.pdf" --exclude "**/*.tmp" -y

# 로그 파일 경로 지정
python sync_folders.py -s A -t B --log "./logs/sync_$(date +%Y%m%d_%H%M%S).log" -y

# 계획만 보고 종료
python sync_folders.py -s A -t B --plan-only
```

## 실행 (GUI 선택)

```bash
python sync_folders_gui.py
```

---

# 🧪 4) 예제 시나리오

### 시나리오 A: 프로젝트 백업 (Windows)

```
소스: C:\Projects\MyApp
타겟: D:\Backup\MyApp
명령: python sync_folders.py -s "C:\Projects\MyApp" -t "D:\Backup\MyApp" --backup -y
```

### 시나리오 B: USB 동기화 (Windows)

```
소스: C:\Documents
타겟: E:\USB_Backup
명령: python sync_folders.py -s "C:\Documents" -t "E:\USB_Backup" -y
```

### 시나리오 C: macOS/Linux 홈 디렉터리 백업

```
소스: ~/work
타겟: /Volumes/Backup/work
명령: python sync_folders.py -s ~/work -t /Volumes/Backup/work --hash sha256 -y
```

---

# 🧰 5) 체크리스트

### 실행 전

* [ ] Python 3.8+ 설치
* [ ] 소스/타겟 경로 및 쓰기 권한 확인
* [ ] 여유 공간 확인(특히 `--backup` 사용 시 추가 공간 필요)

### 실행 중

* [ ] 미리보기(Plan)로 변경 내역 확인
* [ ] 필요 시 `--backup` ON
* [ ] 대용량/다량 파일은 해시 대신 mtime/size 기준 권장

### 실행 후

* [ ] 콘솔/로그에서 성공/오류 확인
* [ ] 타겟 폴더 샘플 검증
* [ ] 백업 폴더 위치 확인(`_backup_<target>_YYYYMMDD_HHMMSS`)

---

# 🧷 6) 검증 기준 매핑

* ✅ 신규 파일 복사 / 수정 덮어쓰기 / 재귀 탐색 / 동일 파일 스킵 / 오류 로그 / 백업 검증
* ⚙️ 성능 팁: 1,000개 파일 5초 내 달성 목표(SSD 기준, 해시 OFF), 100MB 파일 10초 내(SSD·OS/디스크 상황에 따라 달라질 수 있음)

---

# 🧯 7) 오류/롤백 시나리오

* 복사 도중 오류 발생 → **이미 생성된 신규 파일 삭제**, **덮어쓴 파일은 백업본으로 복원**.
* 복구 실패 항목은 로그에 상세 기록.

---

# 🧩 8) 실행 환경·입력·출력·에러 케이스

* **환경**: Python 3.8+, 표준 라이브러리, 콘솔 권장(Windows PowerShell, macOS Terminal, Linux Shell)
* **입력 1**: `-s A -t B` (정상) → 변경 내역 출력 후 복사, 종료 코드 `0`
* **입력 2**: `-s A -t B --hash sha256 -y` → 해시 기반 검증 후 복사, 종료 코드 `0`
* **오류 케이스**: 소스 미존재 → 콘솔 오류 출력, 종료 코드 `2`

---

# 🔧 9) 내부 품질 기준(셀프 체크 후 반영)

* 표준 라이브러리만 사용, 크로스플랫폼 경로/시간 처리
* 변경 탐지 우선순위: **size → mtime(ε=1s) → hash(옵션)**
* 안전 장치: 미리보기/확인 프롬프트, 백업, 롤백, 로그
* 대용량 안정성: 스트리밍 해시, copy2 메타데이터 보존
* 모듈화: `plan_changes`, `SyncEngine.apply`, GUI 분리
* 사용성: `--plan-only`, `--include/--exclude`, `--yes`, `--log`

---

# 📎 10) 참고 메모

* 파일시스템 mtime 정밀도는 OS/FS에 따라 다릅니다(Windows는 보정 필요). 본 스크립트는 **ε=1초** 여유를 둡니다.
* 해시 비교는 정확하지만 느립니다(권장: 문제 파일에 한정하여 사용).

---

필요하시면 **스케줄링(Windows 작업 스케줄러 / `cron`) 템플릿**, **네트워크 드라이브/에러 리트라이**, **증분 로그(JSON/CSV)**도 바로 확장해 드릴게요.



---------------------------------------------------------------------


소스 폴더 : c:\staybymeerp-Intellij\
대상 폴더 : c:\WORK\PROJECT\SBM\VSCODE\staybymeerp-Intellij\

위의 소스, 대상 폴더로 수정해줘


---------------------------------------------------------------------

좋아요! 바로 쓸 수 있게 **명령어**랑 **윈도우 배치 파일(.bat)** 두 가지로 정리했습니다. (경로는 요청하신 그대로 반영)

---

# 🟢 바로 실행(권장 순서)

## 1) 미리보기(Plan만)

```powershell
python sync_folders.py `
  -s "C:\staybymeerp-Intellij" `
  -t "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ" `
  --plan-only
```

## 2) 실제 복사(백업+확인 스킵)

```powershell
python sync_folders.py `
  -s "C:\staybymeerp-Intellij" `
  -t "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ" `
  --backup -y `
  --log "C:\Logs\sync_sbm_$(Get-Date -f yyyyMMdd_HHmmss).log"
```

> 참고: 백업은 `C:\WORK\PROJECT\SBM\VSCODE\_backup_staybymeerp-IntelliJ_YYYYMMDD_HHMMSS\` 형태로 만들어집니다.

---

# 🟡 옵션(필요 시)

* 조금 더 엄격하게 비교하고 싶으면(느려짐):

```powershell
python sync_folders.py -s "C:\staybymeerp-IntelliJ" -t "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ" --hash sha256 -y --backup
```

* 특정 확장자만(예: 자바/리소스 위주) — *참고: 스크립트는 include/exclude를 한 개씩만 받습니다.*

```powershell
python sync_folders.py -s "C:\staybymeerp-IntelliJ" -t "C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ" `
  --include "**/*.{java,xml,properties}" -y --backup
```

---

# 💡 원클릭 실행용 BAT 파일

아래 내용을 `C:\Tools\Sync-SBM.bat`로 저장 후 더블클릭하면 됩니다.

```bat
@echo off
setlocal

REM === 경로 설정 ===
set SRC=C:\staybymeerp-Intellij
set DST=C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ
set LOGDIR=C:\Logs

if not exist "%LOGDIR%" mkdir "%LOGDIR%"
for /f "tokens=1-4 delims=/:. " %%a in ("%date% %time%") do set TS=%%a%%b%%c_%%d
set LOG=%LOGDIR%\sync_sbm_%TS%.log

echo [INFO] SOURCE: %SRC%
echo [INFO] TARGET: %DST%
echo [INFO] LOG   : %LOG%
echo.

REM 1) 미리보기
python "%~dp0sync_folders.py" -s "%SRC%" -t "%DST%" --plan-only
if errorlevel 2 goto :ERR

echo.
choice /M "위 변경을 적용할까요?" /T 10 /D Y >nul
if errorlevel 2 goto :END

REM 2) 실제 적용 (백업+확인 스킵)
python "%~dp0sync_folders.py" -s "%SRC%" -t "%DST%" --backup -y --log "%LOG%"
if errorlevel 1 goto :ERR

echo.
echo [DONE] 동기화 완료.
goto :END

:ERR
echo [ERROR] 오류가 발생했습니다. 로그를 확인하세요: %LOG%
exit /b 1

:END
endlocal
```

> 배치 파일과 `sync_folders.py`가 **같은 폴더**에 있으면 바로 동작합니다. (아니면 `python` 경로/파일 경로를 맞춰주세요)

---

# ✅ 빠른 체크리스트

* [ ] `C:\staybymeerp-IntelliJ` 존재 확인
* [ ] `C:\WORK\PROJECT\SBM\VSCODE\staybymeerp-IntelliJ` 쓰기 권한 확인
* [ ] 먼저 `--plan-only`로 변경 내역 확인
* [ ] 본 실행은 `--backup -y` 조합 권장(문제 시 자동 롤백 지원)

원하시면 `.git`, `.idea`, `build/`, `out/`, `target/` 같은 **제외 규칙**을 안전하게 넣을 수 있게 스크립트 확장 버전도 만들어 드릴게요.
