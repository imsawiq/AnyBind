# Version folders

The root `src/main` is the full Minecraft `1.21.1-1.21.8` source set. It contains both the shared mod code and the `1.21.1-1.21.8` API adapters, so opening the root project behaves like the base version.

`versions/1.21.9-1.21.11/src` contains only the adapters required by the newer input and list APIs. Its Gradle build reuses shared packages from the root `src/main` and excludes the root-only adapters.

Both 26.x builds consume the same implementation from `versions/shared/26.x`; their own `src` folders contain only `ClientScreens`, the one API boundary that actually differs between 26.1 and 26.2. The shared 26.x Gradle configuration also lives beside that source set.

Minecraft 26.1+ uses official, unobfuscated names and Java 25. Minecraft 26.2 moved screen ownership from `Minecraft` into `Minecraft.gui`; that difference is isolated in `ClientScreens` so the rest of the 26.x code stays aligned.

Use:

```powershell
.\build-version.bat 1.21.1-1.21.8
.\build-version.bat 1.21.9-1.21.11
.\build-version.bat 26.1-26.1.2
.\build-version.bat 26.2
.\build-all.bat
```

Aliases `26.1`, `26.1.1`, and `26.1.2` all build the `26.1-26.1.2` source set.
