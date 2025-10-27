import os, json, time, psutil, datetime, traceback, winreg

def now(): return datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def read_cfg(path):
    # BOM/무BOM 모두 허용
    with open(path, "r", encoding="utf-8-sig") as f:
        return json.load(f)

def log(msg, cfg):
    try:
        lp = cfg["paths"]["logs"]; os.makedirs(lp, exist_ok=True)
        with open(os.path.join(lp,"monitor.log"), "a", encoding="utf-8") as f:
            f.write(f"[{now()}] {msg}\n")
    except Exception:
        pass

def write_json(data, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    tmp = path + ".tmp"
    with open(tmp,"w",encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    os.replace(tmp, path)

def collect_report(cfg):
    mem = psutil.virtual_memory()
    total_gb = round(mem.total/1024/1024/1024, 2)
    used = round(mem.percent, 1)

    # Top 30 processes
    procs = []
    for p in psutil.process_iter(["pid","name","memory_info"]):
        try:
            mi = p.info["memory_info"]
            rss = mi.rss if mi else 0
            procs.append({"name": p.info["name"] or "?", "pid": p.info["pid"], "rss_mb": int(rss/1024/1024)})
        except Exception:
            pass
    procs.sort(key=lambda x:x["rss_mb"], reverse=True)
    top = procs[:30]

    # Startup (Run keys)
    startup=[]
    for hive, path in [(winreg.HKEY_CURRENT_USER, r"Software\Microsoft\Windows\CurrentVersion\Run"),
                       (winreg.HKEY_LOCAL_MACHINE, r"Software\Microsoft\Windows\CurrentVersion\Run")]:
        try:
            k = winreg.OpenKey(hive, path)
            i = 0
            while True:
                try:
                    name, _, _ = winreg.EnumValue(k, i)
                    startup.append({"name": name, "state": "Enabled"})
                    i += 1
                except OSError:
                    break
        except Exception:
            pass

    th = cfg["monitoring"]["thresholds"]
    if used >= th["emergency"]: lvl="EMERGENCY"
    elif used >= th["critical"]: lvl="CRITICAL"
    elif used >= th["caution"]:  lvl="CAUTION"
    elif used >= th["warning"]:  lvl="WARNING"
    else: lvl="INFO"

    doc = {
        "generated_at": now(),
        "summary": {"total_gb": total_gb, "percent": used, "level_text": lvl},
        "top_processes": top,
        "startup_programs": startup,
        "services": [],
        "temp_sizes": {}
    }
    write_json(doc, cfg["paths"]["report_json"])
    return used

def main():
    cfg = read_cfg(r"C:\Tools\ChatGPT\MemoryOptimizer\config.json")
    log("Monitor start", cfg)
    interval = int(cfg["monitoring"]["interval_seconds"])
    while True:
        try:
            collect_report(cfg)
            time.sleep(interval)
        except Exception as e:
            log("loop error: "+str(e), cfg)
            log(traceback.format_exc(), cfg)
            time.sleep(interval)

if __name__ == "__main__":
    main()