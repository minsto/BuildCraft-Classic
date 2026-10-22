# BuildCraft Classic

Unofficial port of [BuildCraft](https://github.com/BuildCraft/BuildCraft) to Minecraft 1.21.1.
This project is not maintained by SpaceToad or the BuildCraft team.

The gameplay is a rewrite of the classic loop: gears and a wrench, Stirling and combustion engines, item, fluid and power pipes, a tank, a pump, a mining well, a filler, a quarry, a builder, lasers, an assembly table, gates, and a zone planner with a robot. Machines that place or break blocks go through the `WorldTransforming` queue instead of editing the world in one tick.

## Licenses

* Code written for this port, and code derived from BuildCraft, is Mozilla Public License 2.0. See `LICENSE`.
* Textures, models and languages copied from BuildCraft stay under the Minecraft Mod Public License 1.0.1. The text is `buildcraft_resources/LICENSE.BUILDCRAFT`, copied unchanged. The MMPL is not an SPDX identifier. GitHub and Modrinth can use the file shipped in the repository. CurseForge may ask for a manual license declaration.
* `NOTICE` credits SpaceToad and the BuildCraft team.

The 8.0.0 jar does not contain a `sounds.json` or custom `.ogg` files. Machines reuse vanilla sound events (furnace crackle, stone break, beacon, bucket fill).

## Jars

One codebase, three player jars. A single jar cannot load on Fabric, Forge and NeoForge in 1.21.1.

| File | Use |
| --- | --- |
| `buildcraft-classic-fabric-1.21.1.jar` | Fabric Loader 0.19.5, Fabric API |
| `buildcraft-classic-forge-1.21.1.jar` | Forge 52.0.28 |
| `buildcraft-classic-neoforge-1.21.1.jar` | NeoForge 21.1.80 |
| `buildcraftclassic-api-1.0.0.jar` | Compile dependency for an addon |
| `buildcraftclassic-api-1.0.0-sources.jar` | API sources |
| `buildcraft-classic-common-1.21.1-sources.jar` | Common gameplay sources |

Architectury API 13 does not publish a Forge build for 1.21.1, so this repository uses a MultiLoader layout: vanilla code in `:common` and `:api`, with `:fabric`, `:forge` and `:neoforge` as the loader entries. The mod id on every loader is `buildcraftclassic` because Forge and NeoForge reject a hyphen. Asset namespaces stay the original ones (`buildcraftcore`, `buildcraftenergy`, `buildcrafttransport`, `buildcraftfactory`, `buildcraftbuilders`, `buildcraftsilicon`, `buildcraftrobotics`, `buildcraftlib`).

Do not put the API jar in the `mods` folder. The player jar already contains those classes.

## Build

Java 21 and Gradle 8.10 (the wrapper is included). Gradle 8.10 itself must run on Java 21, 22 or 23. The mod is compiled for Java 21 even if a newer JDK is the default on the machine: point `JAVA_HOME` at a JDK 21 before `./gradlew`.

```bash
./gradlew releaseJars
```

The jars are copied to `build/dist/`.

To play from the project:

```bash
./gradlew :fabric:runClient
./gradlew :forge:runClient
./gradlew :neoforge:runClient
```

## Addon API

Depend on `com.bcc:buildcraftclassic-api:1.0.0` as a compile-only dependency. Publish it to maven local with:

```bash
./gradlew :api:publishToMavenLocal
```

```gradle
dependencies {
    compileOnly "com.bcc:buildcraftclassic-api:1.0.0"
}
```

Energy is Minecraft Joules. `1 MJ` is `1_000_000` microjoules (`com.bcc.api.mj.Mj`).

`com.bcc.api.world.WorldTransforming` is the block-edit contract. Open a session on the server, enqueue `WorldOp`s, and call `tick`. The default budget is 32 blocks per machine per tick. Unloaded chunks are skipped. A chunk ticket covers the machine radius and is released when the session closes. The client never applies these edits. An addon that places or breaks many blocks has to use this queue.

## Portage non officiel

Les textures et les modèles viennent du jar BuildCraft 8.0.0 et restent sous MMPL. Le code nouveau est en MPL 2.0. Le nom affiché est BuildCraft Classic. Ce dépôt n'est pas le dépôt officiel.
