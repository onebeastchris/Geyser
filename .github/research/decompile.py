#!/usr/bin/env python3
"""Download the Minecraft client jar for a version and decompile the classes relevant to
item/block interaction simulation. Used by .github/workflows/interaction-research.yml only;
nothing produced here is committed."""
import json
import os
import subprocess
import sys
import urllib.request
import zipfile

VERSION = sys.argv[1]
OUT = sys.argv[2]

PREFIXES = (
    "net/minecraft/world/item/",
    "net/minecraft/world/level/block/",
    "net/minecraft/world/InteractionResult",
    "net/minecraft/world/InteractionHand",
    "net/minecraft/world/entity/player/Player",
    "net/minecraft/world/entity/player/Abilities",
    "net/minecraft/world/entity/LivingEntity",
    "net/minecraft/world/entity/Entity.class",
    "net/minecraft/world/entity/EquipmentSlot",
    "net/minecraft/world/entity/vehicle/",
    "net/minecraft/world/entity/decoration/",
    "net/minecraft/world/entity/animal/",
    "net/minecraft/world/level/Level.class",
    "net/minecraft/world/level/BaseCommandBlock",
    "net/minecraft/world/level/GameType",
    "net/minecraft/world/level/border/WorldBorder",
    "net/minecraft/world/phys/BlockHitResult",
    "net/minecraft/world/ItemStackWithSlot",
    "net/minecraft/client/Minecraft.class",
    "net/minecraft/client/multiplayer/MultiPlayerGameMode",
    "net/minecraft/client/player/LocalPlayer",
    "net/minecraft/core/dispenser/",
    "net/minecraft/tags/BlockTags",
    "net/minecraft/tags/ItemTags",
)


def fetch(url, dest=None):
    with urllib.request.urlopen(url) as resp:
        data = resp.read()
    if dest:
        with open(dest, "wb") as f:
            f.write(data)
    return data


manifest = json.loads(fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
entry = next(v for v in manifest["versions"] if v["id"] == VERSION)
version_json = json.loads(fetch(entry["url"]))
os.makedirs(OUT, exist_ok=True)
client_jar = os.path.join(OUT, "client.jar")
fetch(version_json["downloads"]["client"]["url"], client_jar)

libs = []
for lib in version_json["libraries"]:
    art = lib.get("downloads", {}).get("artifact")
    if not art:
        continue
    path = os.path.join(OUT, "libs", art["path"].replace("/", "_"))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    fetch(art["url"], path)
    libs.append(path)

classes_dir = os.path.join(OUT, "classes")
with zipfile.ZipFile(client_jar) as jar:
    for name in jar.namelist():
        if name.endswith(".class") and name.startswith(PREFIXES):
            jar.extract(name, classes_dir)

vineflower = os.path.join(OUT, "vineflower.jar")
fetch("https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.1/vineflower-1.11.1.jar", vineflower)
src = os.path.join(OUT, "src")
os.makedirs(src, exist_ok=True)
cmd = ["java", "-Xmx6g", "-jar", vineflower, "-dgs=1", "-rsy=1", "-ind=    ", "-e=" + client_jar]
cmd += ["-e=" + lib for lib in libs]
cmd += [classes_dir, src]
subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL)
print("decompiled", sum(len(f) for _, _, f in os.walk(src)), "files")
