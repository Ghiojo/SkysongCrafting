# Development status

Continuation record for SkysongCrafting. Updated at the end of each milestone. Read this first
when resuming work; `docs/COMPOSITE_FORMAT.md` describes the composite template format.

## Goals and constraints (confirmed with the owner)

- Crafting plugin for the Skysong roleplay server, built on the owner's own SkysongItems (1.8.0)
  and SkysongStats (1.2) plugins. Paper 1.21.5 / Java 21 for now; Paper 1.21.11 after milestone 1.
- **Recipes validate** the grid (shaped, shapeless, marking, conditional). **Outputs produce**
  the result (basic, random, random_id, composite).
- **Composite is a general template engine**, not a feature for one item. New crafting behaviours
  are expressed as YAML instructions; Java changes only add step types or placeholders.
- Marked items are numbered by position among the marked items, not by grid slot.
- Rebuilding a saved item from SkysongItems data is acceptable (the server does not use
  durability or enchantments).
- Keep changes in SkysongCrafting; touch SkysongItems only for critical bugs.
- Workflow: feature branch per milestone, never push to `master`, PR only after owner approval,
  `type: message` commits (`feat:`, `fix:`, `docs:`, `refactor:`). Owner builds and tests in-game.
  Ask before adding dependencies or changing a YAML format. English for code and comments.

## Milestone 1 — composite engine (in progress)

Branch: `feature/composite-engine` (based on `master` @ 2a3e93f).

### Done in this batch (not yet built or tested by the owner)
- `OutputException` + `OutputStep.execute` contract; the sticky `doesGoToNextStep` flag is gone.
- `CompositeOutput` runs the step list, wraps failures as `Step N (type): message`, refuses an
  empty result.
- `AbstractWorkbench.CraftItem`: collect marked copies → run output → generate all result stacks
  → consume → place. Failure before consumption leaves the grid untouched and messages the player.
- `RecipeInstance.collectMarkedItems` split from `ConsumeMaterials` (all 5 recipe classes).
  Shapeless marking now compares crafting ids (previously compared against a `MaterialCount`
  object and never marked anything).
- `CompositeOutputParser`: list-based format (`steps:`, `fetch_id`, `values`, `cmd_type`), per-step
  validation, whole output refused on any error.
- Steps rewritten: fetch (`.yml`/`.yaml`), tag (int/float/bool/string), property (idempotent),
  cmd (colours, placeholder `index`, placeholders for every type, insert-at-index), blend
  (`tag_id` + `sources`), save (guards for non-SkysongItems, heads/potions, missing CMD component).
- `PlaceholderParser`: single failure path, bounds checks, typed helpers, `cmd:*s` counts.
- Loader: recipe output files found with `.yml` or `.yaml`; unknown output type or missing/failed
  output file skips the recipe with a warning instead of crashing.
- Removed dead code: `AmountType`, `TypeCastingUtils`, `ReferenceType`, `OutputType.ID`,
  `TYPE_MAP`, `AMOUNT_TYPE_MAP`, `COMPOSITE_REFERENCE_PATTERN`.
- Duplicate sound playback in `CraftItem` removed (the first call could NPE on an unknown sound).

### Checks performed
- `javac` syntax pass over all sources (Paper API unavailable in the assistant's environment, so
  only syntax was verifiable). No syntax errors. **Not compiled against Paper, not run.**

### Known risks to verify in-game
- `SaveStep` sets an empty custom model data component so `SkysongItem(ItemStack)` does not store
  `null`; confirm Paper keeps an empty component (`hasCustomModelDataComponent()` true). If not,
  the one-line fix belongs in SkysongItems: `SkysongItem(ItemStack)` should create an empty
  `CustomModelData` instead of `null`.
- `ItemMeta.getItemName()` is used in one log line in `CraftItem`.

### Remaining in milestone 1
- Owner: add `libs/SkysongSpecialItems-1.8.0.jar` (clean clone does not compile without it).
- Owner: build (`gradlew build`), run tests, test each step type in-game, report.
- (done) Example files fixed (`inputs` in shapeless/smelting, names, sounds); `shaped_marking_recipe.yaml` added.
- (done) `marking_pattern` accepts 5x5 rows like `pattern`, or a flat list.
- (done) JUnit 5 added; tests for `CompositeOutputParser`, `MaterialCount`/`inputToMaterialCountList`,
  `readMarkingPattern`. Run with `gradlew test`; `gradlew build -x test` skips them.
  Item/step logic needs a server (or MockBukkit) and is not unit-tested.

## Milestone 2 — recipe side + corrections (planned)
- Conditional recipes loadable from YAML (`conditions:` on marking recipes), indexed by marked
  position; empty marked slot should fail the condition.
- `workstations.yml` not copied on first run; UUID `!=` comparisons in `WorkstationListener`;
  permission-name mismatches with `plugin.yml`; DB row deleted before the break-permission check;
  `RandomIdListener` fires before outputs exist; `ReloadCommand` casts console to `Player`.
- Load-time warning for tags unknown to SkysongItems.

## Milestone 3 — Paper 1.21.11.

## Open questions for the owner
- Shapeless conditional recipes wanted?
