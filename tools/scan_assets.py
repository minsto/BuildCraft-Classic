import json
import os

root = r"C:\Users\chopi\Downloads\buildcraft-all-8.0.0.jar_Decompiler.com\assets"
parents = set()
forge = 0
vanilla = 0
parse_fail = 0
for dp, dns, fns in os.walk(root):
    for name in fns:
        if not name.endswith(".json"):
            continue
        path = os.path.join(dp, name)
        text = open(path, encoding="utf-8").read()
        if "forge_marker" in text or '"forge:' in text:
            forge += 1
        if "minecraft:blocks/" in text or "minecraft:items/" in text:
            vanilla += 1
        try:
            data = json.loads(text)
        except Exception:
            parse_fail += 1
            continue
        parent = data.get("parent") if isinstance(data, dict) else None
        if isinstance(parent, str):
            parents.add(parent)
data = r"C:\Users\chopi\Downloads\buildcraft-all-8.0.0.jar_Decompiler.com\data"
print("--- data top ---")
for name in os.listdir(data):
    print(name)
print("forge-ish", forge)
print("vanilla-path", vanilla)
print("parse-fail", parse_fail)
for parent in sorted(parents):
    print(parent)
