# Tidy – Project Initialization Design

**Date:** 2026-03-27
**Approach:** Full scaffold with Yarn mappings (Approach A)

---

## 1. Gradle & Stonecutter

### Plugin versions
- Stonecutter: `dev.kikugie.stonecutter` `0.9`
- Fabric Loom: `net.fabricmc.fabric-loom-remap` `1.15-SNAPSHOT`

### settings.gradle.kts
- `pluginManagement` repos: mavenLocal, mavenCentral, gradlePluginPortal, `maven.fabricmc.net`, `maven.kikugie.dev/snapshots`
- Stonecutter block: `versions("1.18.2", "1.19.4", "1.20.1", "1.20.4", "1.21.1", "1.21.4", "1.21.11")`, `vcsVersion = "1.21.11"`
- `rootProject.name = "tidy"`

### stonecutter.gradle.kts (controller)
- `stonecutter active "1.21.11"`
- `stonecutter parameters`: swaps for `mod_version` and `minecraft`, dep reference for `fapi`

### build.gradle.kts (version template)
- `version = "${mod.version}+${sc.current.version}"`
- Java version: `>= 1.20.5` → Java 21, else → Java 17
- Mappings: Yarn via `"net.fabricmc:yarn:${property("deps.yarn")}:v2"`
- Dependencies: `minecraft`, `yarn`, `fabric-loader`, `fabric-api` (full jar)
- `loom`: `fabricModJsonPath`, shared `runDir = "../../run"`, mixin debug export
- `processResources`: expands `fabric.mod.json` and `*.mixins.json`

### gradle.properties (root)
```
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
org.gradle.configuration-cache=true

mod.version=0.1.0
mod.group=pl.inh.tidy
mod.id=tidy
mod.name=Tidy

deps.fabric_loader=0.18.5
```

### Per-version gradle.properties

Each in `versions/<mc>/gradle.properties`:

| MC version | deps.yarn          | deps.fabric_api        | mod.mc_dep              | Java |
|------------|--------------------|------------------------|-------------------------|------|
| 1.18.2     | 1.18.2+build.4     | 0.75.1+1.18.2          | >=1.18.2 <1.19          | 17   |
| 1.19.4     | 1.19.4+build.6     | 0.77.2+1.19.4          | >=1.19.4 <1.20          | 17   |
| 1.20.1     | 1.20.1+build.8     | 0.92.7+1.20.1          | >=1.20 <=1.20.1         | 17   |
| 1.20.4     | 1.20.4+build.10    | 0.97.3+1.20.4          | >=1.20.4 <=1.20.6       | 17   |
| 1.21.1     | 1.21.1+build.12    | 0.116.9+1.21.1         | >=1.21 <=1.21.1         | 21   |
| 1.21.4     | 1.21.4+build.13    | 0.119.2+1.21.4         | >=1.21.4 <=1.21.4       | 21   |
| 1.21.11    | 1.21.11+build.4    | 0.141.3+1.21.11        | >=1.21.11               | 21   |

---

## 2. Source Structure

Package root: `src/main/java/pl/inh/tidy/`

### Core
- `Tidy.java` — `ModInitializer`; calls config load, keybind registration, handler init

### sort/
- `SortStrategy.java` — interface: `void sort(List<ItemStack> slots, SortContext ctx)`
- `SortContext.java` — record: `PlayerEntity player, boolean lockHotbar`
- `SortHandler.java` — merges identical stacks, reads config strategy, dispatches
- `CategorySortStrategy.java` — orders: weapons → tools → ranged → armor → blocks → food → potions → misc (alpha within category)
- `AlphaSortStrategy.java` — sorts by `Item.toString()` alphabetically
- `CountSortStrategy.java` — sorts by stack size descending

### refill/
- `RefillHandler.java` — on tool break or stack exhaustion: finds matching item in inventory, swaps to hotbar slot
- `ToolTracker.java` — tracks last `ItemStack` held in main hand

### config/
- `TidyConfig.java` — fields: `String sortMode` (default `"category"`), `boolean lockHotbar` (true), `boolean autoRefill` (true), `boolean refillBlocks` (true), `List<SortRule> customRules`
- `SortRule.java` — record: `String match, int priority`

### keybind/
- `TidyKeybinds.java` — registers sort key (`R`, category `tidy`); triggers `SortHandler` on press

### mixin/
- `ScreenHandlerMixin.java` — `@Mixin(ScreenHandler.class)` stub; will inject sort button logic
- `PlayerEntityMixin.java` — `@Mixin(PlayerEntity.class)` stub; will inject tick hook for refill

---

## 3. Resources

### fabric.mod.json
- Template-expanded: `${id}`, `${version}`, `${name}`, `${minecraft}`
- `environment: client`
- Entrypoint: `pl.inh.tidy.Tidy`
- Author: `Inheritence`, license: `MIT`
- Mixin: `tidy.mixins.json`
- Depends: `fabricloader >=0.15`, `minecraft: ${minecraft}`, `java: >=17`

### tidy.mixins.json
- Package: `pl.inh.tidy.mixin`
- `compatibilityLevel: ${java}` (expanded by processResources)
- Mixins: `ScreenHandlerMixin`, `PlayerEntityMixin`

### assets/tidy/lang/en_us.json
- Empty `{}` placeholder

---

## 4. Git

- `git init`
- `.gitignore`: standard Gradle ignores + `.idea/`, `run/`, `*.iml`
- Initial commit with all scaffolded files

---

## Out of Scope

- Actual implementation of sort/refill logic (stubs only)
- Cloth Config GUI
- Minotaur / Modrinth publishing config
- CI/CD GitHub Actions
- Access widener (not needed for initial stubs)
