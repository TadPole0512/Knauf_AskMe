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
