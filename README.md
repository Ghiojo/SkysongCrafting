# SkysongCrafting

Custom crafting stations for the Skysong roleplay server, built on
[Skysong-Special-Items](https://github.com/Ghiojo/Skysong-Special-Items) and SkysongStats.

Players register workstation blocks (with an owner, members and a public/private toggle), open a
5×5 workbench or a smelter GUI on them, and craft from recipes defined in YAML. Outputs can be a
fixed item, a random roll, or a **composite template**: an ordered list of instructions that edits
one of the ingredients or builds a new item using data read from the others.

## Documentation

| Document | What it covers |
|---|---|
| [`docs/RECIPE_FORMAT.md`](docs/RECIPE_FORMAT.md) | Recipe files: shaped / shapeless patterns, tags, consumption suffixes (`^N`, `!keep`), marking, conditions |
| [`docs/COMPOSITE_FORMAT.md`](docs/COMPOSITE_FORMAT.md) | Composite output templates: steps, placeholders, failure behaviour |
| [`docs/DEVELOPMENT_STATUS.md`](docs/DEVELOPMENT_STATUS.md) | Continuation record: decisions, what is done, what is next |

Example files for every recipe and output type are in `src/main/resources/` and are copied to the
plugin's data folder on first run.

## Requirements

- Paper 1.21.11, Java 21
- Skysong_Items (API 1.3.0 or newer) and SkysongStats (API 1.0.0 or newer), both hard dependencies
- Output items are SkysongItems saved items (`/si saveitem`); SkysongCrafting has no `items/` folder
- MySQL for workstation storage (`config.yml`)

## Building

```
./gradlew build        # compiles and runs the unit tests
./gradlew build -x test
```

SkysongCrafting compiles against the public APIs of SkysongItems and SkysongStats, which JitPack
publishes from their repositories (`com.github.Ghiojo:Skysong-Special-Items:1.0.1`,
`com.github.Ghiojo:SkysongStats:1.0.0`). The output jar is in `build/libs/`.

## Public API

Other plugins can read workstations and react to crafts and workstations. Only
`org.skysongdev.skysongCrafting.api` is public API; it lives in the `api/` module, which JitPack
publishes on its own (like SkysongItems' api module). Its classes also ship inside the plugin jar.

```groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies { compileOnly 'com.github.Ghiojo.SkysongCrafting:api:<tag>' }
```

Add `SkysongCrafting` to `depend` or `softdepend` in your `plugin.yml`, then:

```java
SkysongCraftingApi crafting = SkysongCraftingApi.get();   // apiVersion(), workstations()

@EventHandler
public void onCraft(CraftFinishedEvent event) {
    Player player = event.getPlayer();
    String recipeId = event.getRecipe().id();             // e.g. "shaped/iron_sword"
    WorkstationInfo bench = event.getWorkstation();       // block, type, owner, members
    List<ItemStack> made = event.getResults();
}
```

| Type | What it does |
|---|---|
| `SkysongCraftingApi` | Entry point: `SkysongCraftingApi.get()`, `apiVersion()` (`1.1.0`), `workstations()` |
| `CraftFinishedEvent` | A craft finished: after the ingredients were consumed and the results placed. Player, `WorkstationInfo`, `RecipeInfo`, results, ingredients (copies as they were before the craft). Not fired for refused crafts |
| `RecipeInfo` | A recipe: `id` (its file's path under `recipes/` without extension, e.g. `shaped/iron_sword`), `name` (MiniMessage), `tableType` |
| `Workstations` | Read placed workstations: `at(block)` / `at(world, x, y, z)` and `all()`. Read-only, main thread, returns copies. Also registered on its own with the services manager (API 1.0.0) |
| `WorkstationInfo` | A workstation at the moment it was read: block (world, x, y, z), type id and name, owner, members, public flag, `canUse(uuid)` |
| `WorkstationCreateEvent` | After `/sc workbench create` saved a workstation |
| `WorkstationRemoveEvent` | After a workstation was removed; `getCause()` is `DELETED` (`/sc workbench delete`), `BROKEN` or `REPLACED` (a new one was created on its block; the create event follows) |
| `WorkstationAccessEvent` | A player who isn't owner, member or bypassing tried to open a private workstation; `setAllowed(true)` lets them in |

A workstation is identified by its block. Events fire on the main thread and can't be cancelled.
`events.CraftingEvent` is internal (it fires before the ingredients are used, so results can be
edited) and can change without notice; use `CraftFinishedEvent`.

## Commands

`/sc` (aliases `/skysongcrafting`, `/scraft`, `/skyc`)

| Subcommand | Permission |
|---|---|
| `workbench create <type> [player]` | `skysongcrafting.workbench.create` |
| `workbench delete` | `skysongcrafting.workbench.delete` |
| `workbench addmember <player>` / `removemember <player>` | `skysongcrafting.workbench.addmember` / `.removemember` |
| `workbench setowner <player>` | `skysongcrafting.workbench.setowner` |
| `workbench togglepublic <player>` | `skysongcrafting.workbench.togglepublic` |
| `reload` | `skysongcrafting.management.reload` |
| `forceclose` | `skysongcrafting.management.forcecloseinventories` |

Workbench commands act on the workstation block the player is looking at. Full permission list in
`src/main/resources/plugin.yml`.
