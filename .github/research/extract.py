#!/usr/bin/env python3
"""Print interaction-relevant parts of decompiled sources, grouped into sections so each CI job
log stays small. Usage: extract.py <src-dir> <section> [shard] [shards]"""
import os
import re
import sys

SRC, SECTION = sys.argv[1], sys.argv[2]
SHARD = int(sys.argv[3]) if len(sys.argv) > 3 else 0
SHARDS = int(sys.argv[4]) if len(sys.argv) > 4 else 1

BLOCK_METHODS = ("useItemOn", "useWithoutItem")
ITEM_METHODS = ("use", "useOn", "interactLivingEntity", "overrideStackedOnOther", "overrideOtherStackedOnMe")


def read(rel):
    with open(os.path.join(SRC, rel), encoding="utf-8") as f:
        return f.read()


def strip_imports(text):
    return "\n".join(l for l in text.splitlines() if not l.startswith(("import ", "package ")))


def method_bodies(text, names):
    """Return the source of every method declaration with one of the given names."""
    out = []
    pattern = re.compile(r"^( {4}|\t)[\w<>\[\], ?@.]*\b(" + "|".join(names) + r")\(", re.M)
    for m in pattern.finditer(text):
        start = text.rfind("\n", 0, m.start()) + 1
        brace = text.find("{", m.end())
        semi = text.find(";", m.end())
        if brace == -1 or (semi != -1 and semi < brace):
            continue
        depth, i = 0, brace
        while i < len(text):
            if text[i] == "{":
                depth += 1
            elif text[i] == "}":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        out.append(text[start:i + 1])
    return out


def java_files(prefix):
    root = os.path.join(SRC, prefix)
    for dirpath, _, files in os.walk(root):
        for f in sorted(files):
            if f.endswith(".java"):
                yield os.path.relpath(os.path.join(dirpath, f), SRC)


def overriding(prefix, names):
    result = []
    for rel in sorted(java_files(prefix)):
        text = read(rel)
        if method_bodies(text, names):
            result.append(rel)
    return result


def dump_full(files):
    files = [f for i, f in enumerate(files) if i % SHARDS == SHARD]
    for rel in files:
        print("=" * 20, rel)
        print(strip_imports(read(rel)))


if SECTION == "inventory":
    for prefix, names in (("net/minecraft/world/level/block", BLOCK_METHODS), ("net/minecraft/world/item", ITEM_METHODS)):
        for rel in overriding(prefix, names):
            text = read(rel)
            ext = re.search(r"class \w+(?:<[^>]*>)? extends (\w+)", text)
            found = sorted({n for n in names if method_bodies(text, (n,))})
            print(rel, "extends", ext.group(1) if ext else "-", found)
elif SECTION == "flow":
    for rel in ("net/minecraft/client/multiplayer/MultiPlayerGameMode.java", "net/minecraft/world/InteractionResult.java"):
        print("=" * 20, rel)
        print(strip_imports(read(rel)))
    mc = read("net/minecraft/client/Minecraft.java")
    print("=" * 20, "Minecraft#startUseItem")
    print("\n".join(method_bodies(mc, ("startUseItem", "continueAttack", "startAttack"))))
    for rel, names in (
        ("net/minecraft/client/player/LocalPlayer.java", ("isSecondaryUseActive", "isShiftKeyDown", "isHandsBusy", "isUsingItem", "startUsingItem", "getUsedItemHand")),
        ("net/minecraft/world/entity/player/Player.java", ("mayBuild", "mayUseItemAt", "isSecondaryUseActive", "hasInfiniteMaterials", "canUseGameMasterBlocks", "blockActionRestricted", "getCooldowns", "isSpectator", "isCreative", "canEat", "getItemBySlot", "interactOn")),
        ("net/minecraft/world/entity/LivingEntity.java", ("startUsingItem", "getItemInHand", "getUsedItemHand")),
        ("net/minecraft/world/level/block/state/BlockBehaviour.java", ("useItemOn", "useWithoutItem", "canBeReplaced")),
        ("net/minecraft/world/item/ItemStack.java", ("use", "useOn", "isItemEnabled", "getUseDuration", "interactLivingEntity", "canPlaceOnBlockInAdventureMode", "canBreakBlockInAdventureMode")),
        ("net/minecraft/world/item/Item.java", ("use", "useOn", "getUseDuration", "getUseAnimation", "interactLivingEntity")),
        ("net/minecraft/world/item/ItemCooldowns.java", ("isOnCooldown", "getCooldownGroup", "addCooldown")),
    ):
        print("=" * 20, rel)
        print("\n".join(method_bodies(read(rel), names)))
    for rel in ("net/minecraft/world/item/BlockItem.java", "net/minecraft/world/item/context/BlockPlaceContext.java",
                "net/minecraft/world/item/context/UseOnContext.java", "net/minecraft/world/item/component/Consumable.java",
                "net/minecraft/world/item/equipment/Equippable.java", "net/minecraft/world/item/component/BlocksAttacks.java",
                "net/minecraft/world/item/component/KineticWeapon.java", "net/minecraft/world/item/component/UseCooldown.java",
                "net/minecraft/world/item/component/UseEffects.java"):
        if os.path.exists(os.path.join(SRC, rel)):
            print("=" * 20, rel)
            print(strip_imports(read(rel)))
elif SECTION == "blocks":
    dump_full(overriding("net/minecraft/world/level/block", BLOCK_METHODS))
elif SECTION == "items":
    dump_full(overriding("net/minecraft/world/item", ITEM_METHODS))
elif SECTION == "file":
    print(strip_imports(read(sys.argv[3])))
else:
    raise SystemExit("unknown section " + SECTION)
