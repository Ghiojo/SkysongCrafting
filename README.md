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
- Skysong_Items 1.8.0 (hard dependency) and SkysongStats 1.2 on the server
- MySQL for workstation storage (`config.yml`)

## Building

```
./gradlew build        # compiles and runs the unit tests
./gradlew build -x test
```

The two Skysong plugin jars are read from `libs/` (`SkysongSpecialItems-1.8.0.jar`,
`SkysongStats-1.2.jar`). The output jar is in `build/libs/`.

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
