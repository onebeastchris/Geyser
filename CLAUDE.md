# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## AI Policy

All AI-assisted contributions must follow the organization-wide AI policy: https://github.com/GeyserMC/.github/blob/master/AI_POLICY.md. Failure to follow it will result in a closed PR (see AGENTS.md).

## What Geyser Is

Geyser is a proxy that lets Minecraft: Bedrock Edition players join Minecraft: Java Edition servers by translating packets between the two protocols in real time. It ships as plugins/mods for several server platforms plus a standalone proxy.

## Build & Test Commands

Java 21 is required (Gradle toolchains will auto-provision it).

```bash
# REQUIRED before first build: fetches the mappings and languages submodules
# (core/src/main/resources/mappings and .../languages). Builds fail without them.
git submodule update --init --recursive

# Full build; platform jars land in bootstrap/<platform>/build/libs/
./gradlew build

# Build a single platform (much faster during development)
./gradlew :spigot:build      # also: :fabric, :neoforge, :standalone, :bungeecord, :velocity, :viaproxy

# Run tests (tests live in core)
./gradlew :core:test

# Run a single test class
./gradlew :core:test --tests "org.geysermc.geyser.network.CodecProcessorTest"
```

The `:gametest` module (`bootstrap/mod/gametest`) contains Minecraft GameTests run through the mod loaders.

## Module Layout

- **`api/`** — the public Geyser API (`org.geysermc.geyser.api`) used by extensions and other GeyserMC projects. Depends on the separate base API repo (`GeyserMC/api`, via `libs.base.api`). If a PR needs base-API changes, fork that repo and create a branch with the **same name** as your Geyser branch — the pull-request workflow picks it up automatically.
- **`common/`** — shared Floodgate code (`org.geysermc.floodgate`): crypto, plugin messaging, shared utils.
- **`core/`** — nearly all actual logic: sessions, packet translators, registries, entities, inventories, commands, extension loading.
- **`ap/`** — annotation processors that generate metadata files listing annotated classes (e.g. `@Translator`, collision remappers, sound handlers, block entities) so they can be discovered at runtime without classpath scanning.
- **`bootstrap/`** — one thin entry-point module per platform (spigot, bungeecord, velocity, viaproxy, standalone, and `mod/` containing fabric + neoforge + shared mod code). Each implements `GeyserBootstrap`.
- **`build-logic/`** — Gradle convention plugins (`geyser.base-conventions`, `geyser.platform-conventions`, `geyser.shadow-conventions`, `geyser.modded-conventions`, etc.).

## Core Architecture

- **`GeyserImpl`** (core) is the central singleton, started by a platform-specific `GeyserBootstrap`.
- **`GeyserSession`** (`core/.../session/`) represents one connected Bedrock player and holds both connections: `UpstreamSession` (Bedrock client ↔ Geyser, using CloudburstMC Protocol) and `DownstreamSession` (Geyser ↔ Java server, using MCProtocolLib). Most translation state (caches, inventories, entity trackers) hangs off the session.
- **Packet translators** (`core/.../translator/protocol/`): each class extends `PacketTranslator<T>` and is annotated `@Translator(packet = SomePacket.class)`.
  - `translator/protocol/java/` — clientbound Java packets → Bedrock; classes are named `Java<PacketName>Translator`.
  - `translator/protocol/bedrock/` — serverbound Bedrock packets → Java; classes are named `Bedrock<PacketName>Translator`.
  - Registration is automatic via the `ap` annotation processor + `PacketTranslatorRegistry`; just create the annotated class.
- **Registries** (`core/.../registry/`): `Registries` and `BlockRegistries` hold static registries populated at startup by the `populator` classes, largely from JSON in the **mappings submodule** (`core/src/main/resources/mappings`). Bedrock-version-dependent data uses `VersionedRegistry`. New Minecraft version support usually means updating the mappings submodule pointer plus the populators.
- **Extensions**: third-party addons loaded by `GeyserExtensionLoader` against the `api` module; the event bus lives in `core/.../event/`.
- **Version support**: Geyser supports multiple Bedrock protocol versions against (generally) one Java version. Bedrock codecs are set up in `core/.../network/GameProtocol.java`.

## Code Conventions (from CONTRIBUTING.md)

- Follow the existing style: static finals capitalized, declare fields with the interface type but initialize with the implementation (`Int2IntMap map = new Int2IntOpenHashMap()`), modern switch syntax, Javadoc on non-obvious methods.
- Use Fastutil specialized collections (`Int2IntMap`, etc.) wherever primitive keys/values are stored — this codebase is very map-heavy and it matters.
- Nullability annotations come from `org.checkerframework.checker.nullness.qual` (`@Nullable` / `@NonNull`).
- Lombok is available in `core`.
- Scope guidance: fixes to Java↔Bedrock compatibility belong in Geyser; features requiring direct server access or exposing player info to developers belong in Floodgate; niche plugin integrations belong in separate extensions/plugins.
