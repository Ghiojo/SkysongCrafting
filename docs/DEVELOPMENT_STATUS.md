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

## Milestone 1 — composite engine (core verified in-game 2026-10-23; awaiting PR approval)

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
- `javac` syntax pass over all sources in the assistant's environment (Paper API unavailable there).
- Owner built the branch and tested on the server (2026-10-23): a SHAPED_MARKING recipe with a
  composite template (fetch marked → cmd color add at placeholder index → cmd string add →
  tag add → save) produced the edited item with the expected data; a template with an unknown
  tag refused the craft with the step message and consumed nothing.
- Note while testing: items generated before the SkysongItems tag rename still carried the old
  `crafting_material` tag and were not recognised by any recipe — not a code issue.

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

## Milestone 2 — recipe side + corrections (verified in-game 2026-10-24; awaiting PR approval)

Branch: `feature/recipe-conditions-and-fixes` (off `master` @ eec7013). One commit per item.

### Done in this batch
- Workstation owner checks use `equals` (were `!=` on UUIDs); break-permission check before the
  DB delete; breaking removes only that world's reference.
- `workstations.yml` copied on first run (listed a non-existent folder; single-file skip rule fixed);
  `config.yml` key renamed to `crafting_key` (what the code reads).
- `plugin.yml` nodes aligned with the code (`skysongrafting` typo, `forcecloseinventories`,
  `workstation.use.bypass`, `workstation.destroy.others` declared).
- `CraftingEvent` carries the result stacks; `RandomIdListener` sets ids on them before placement
  (every result with an empty id gets its own UUID).
- `/sc reload` console-safe, reuses `InitializeFiles()`.
- Recipe conditions (`conditions:` list, any recipe type, marked-position indexing, evaluated via
  `PlaceholderParser`, re-checked at craft time, reason shown in button lore).
  `ConditionalShapedRecipeInstance` removed. Placeholders: `material`, `amount`.
  Inverted `table_type: ANY` check fixed.
- Consumption suffixes: `item^N` (charges) now works (was part of the match key, so it never
  matched); `item!keep` / `item^0` added; both work in shapeless `inputs`, one rule per ingredient
  id (conflicting duplicates refuse the recipe). Shared `RecipeInstance.consumeSlot`.
- `docs/RECIPE_FORMAT.md` added. Tests: `RecipeConditionTest`, `RecipeConditionParserTest`,
  `ConsumptionRuleTest` (37 tests in total).

### Checks performed
- `javac` syntax pass (Paper API unavailable to the assistant).
- Owner: `gradlew test` 37 passed; in-game (2026-10-24) conditions (button lore + craft-time
  refusal), `^N` charges, `!keep`, shapeless `*2^1` and the conflicting-duplicate refusal,
  `table_type: ANY`, `/sc reload` from console, `workstations.yml` on first run, owner access to
  private workstations, break refusal, random_id ids — all behaved as specified.

## Milestone 3 — Paper 1.21.11 (next)
- Bump `paper-api` in build.gradle (compile + test), check `CustomModelDataComponent`,
  `Registry.SOUND_EVENT`, `setItemModel`, `getItemName` against the 1.21.11 API, rebuild, re-run
  the T1-T8 test set.

## Later / optional
- Shapeless conditional recipes already covered (conditions work on any recipe type).
- Composite steps for name/lore editing, if wanted.
- MockBukkit for step-level unit tests, if wanted (new test dependency).

## Open questions for the owner
- Shapeless conditional recipes wanted?
