# Composite outputs

A composite output is a **template of instructions**. Instead of handing out a fixed item, the
recipe runs the steps in the template top to bottom and produces whatever they build. The same
engine covers "edit one of the ingredients", "make a new item using data from the ingredients",
and anything in between, so new crafting behaviours are written in YAML, not Java.

Files live in `plugins/SkysongCrafting/output/composite/`, with `.yml` or `.yaml` extension.
A recipe points at one with `output: "composite:<file name without extension>"`. Recipe files
themselves (patterns, marking, conditions, consumption) are described in `RECIPE_FORMAT.md`.

## Vocabulary

| Term | Meaning |
|---|---|
| **Marked item** | A recipe input the recipe marked as relevant (`marking_pattern` in `SHAPED_MARKING` / `SHAPELESS_MARKING` recipes). Marked items are numbered `0, 1, 2…` in grid order, counting only the marked ones. Steps always see a *copy*. |
| **Pivot** | The item the steps are currently editing. Set by a `fetch` step; every other step works on it. |
| **Placeholder** | A value read from an item at craft time instead of written in the file. |

## File layout

```yaml
output_strings:                    # shown in the recipe icon's lore (MiniMessage)
  - "<gray>Adds a layer to the shield"

steps:                             # executed in this order
  - type: fetch
    fetch_type: marked
    fetch_id: "0"
  - type: cmd
    cmd_type: color
    operation: set
    index: "marked:0;tag:layer_count"
    values: ["marked:1;tag:dye_color"]
  - type: tag
    operation: add
    tag_id: layer_count
    values: ["1"]
  - type: save
    amount: 1
```

`output_items` (SkysongItems saved item ids) is still accepted but is optional for composites.

## Steps

### `fetch` — choose the pivot
| Key | Values |
|---|---|
| `fetch_type` | `marked` (edit a copy of a marked input) or `generated` (start from a fresh SkysongItems saved item) |
| `fetch_id` | marked index (`"0"`), or the saved item's id as `/si loaditem` takes it (`pavise_shield`, `shields/pavise_shield`) |

### `tag` — edit a SkysongItems tag on the pivot
| Key | Values |
|---|---|
| `operation` | `set`, `add` (number tags only), `remove` |
| `tag_id` | a tag registered in SkysongItems' `config.yml` |
| `values` | list; number tags **sum** every entry, other tags take the first. Not needed for `remove`. |
| `duration` / `after` / `every` | optional, see [Timed changes](#timed-changes) |

### `trait` — add or remove a SkysongItems trait
| Key | Values |
|---|---|
| `operation` | `set`, `remove` |
| `trait_id` | a trait registered in SkysongItems (`/si trait add` lists them) |
| `duration` / `after` / `every` | optional, see [Timed changes](#timed-changes) |

Setting a trait the item already has, or removing one it lacks, does nothing.

SkysongItems used to call traits "properties". The old spelling, `type: property` with
`property_id`, still loads as a `trait` step so existing files keep working, but it is legacy:
use `trait` / `trait_id` in new files.

### `charges` / `hp` — edit the pivot's charges or HP
| Key | Values |
|---|---|
| `operation` | `set` (capped at the max), `add` (a negative amount takes off; stays between 0 and the max), `set_max`, `remove` (takes them off the item; only with `after` / `every`) |
| `values` | list, all entries summed (placeholders allowed). Not needed for `remove`. |
| `duration` / `after` / `every` | optional, see [Timed changes](#timed-changes) |

### `transform` — transform the item later
| Key | Values |
|---|---|
| `after` / `every` | required: the item moves to its next SkysongItems transformation once the time has passed (or each time). |

The craft can't transform the item right away: SkysongItems only transforms an item a player
holds. An item without a transformation is left as it is when the timer comes due.

### `timers` — cancel timers on the pivot
| Key | Values |
|---|---|
| `operation` | `clear` |
| `action` | optional: only timers with this action: `add_trait`, `remove_trait`, `set_tag`, `add_tag`, `remove_tag`, `set_charges`, `add_charges`, `set_max_charges`, `remove_charges`, `set_hp`, `add_hp`, `set_max_hp`, `remove_hp`, `transform` |
| `target` | optional: only timers about this tag or trait |

Without `action` and `target`, every timer on the item is cancelled. Use it for crafts such as
a repair that stops a trait from running out: `action: remove_trait`, `target: honed`.

### `cmd` — edit the pivot's custom model data
Custom model data holds four lists: floats, strings, booleans (flags) and colors.
| Key | Values |
|---|---|
| `cmd_type` | `float`, `string`, `boolean`, `color` |
| `operation` | `set` (replace entry at `index`), `add` (append, or insert at `index` if given), `remove` (delete entry at `index`) |
| `index` | number or placeholder |
| `values` | list; floats sum every entry, other kinds take the first. Not needed for `remove`. |

Colours accept `"255,0,0"`, `"#FF0000"` or an RGB integer (which is what colour tags store, so
`"marked:1;tag:dye_color"` works directly).

### `blend` — mix colours stored in tags
| Key | Values |
|---|---|
| `tag_id` | an **integer** tag holding an RGB colour (e.g. `dye_color`) |
| `sources` | two or more item references (`marked:N`, `pivot`) |

Writes the mixed colour to the same tag on the pivot.

### `save` — add the pivot to the results
| Key | Values |
|---|---|
| `amount` | stack size, default 1 |

A template without a `save` step is refused at load time. You may `save` several times
(e.g. fetch A, save, fetch B, save) to produce more than one item.

## Timed changes

SkysongItems can store **timers** on an item: changes it makes on its own once their time has
come. The `tag`, `trait`, `charges` and `hp` steps take **one** of these optional keys:

| Key | What the craft does |
|---|---|
| *(none)* | makes the change, for good |
| `duration: 24h` | makes the change, and SkysongItems **undoes it** once the time has passed |
| `after: 1d` | doesn't change the item; SkysongItems makes the change **once**, after the time |
| `every: 1mo` | doesn't change the item; SkysongItems makes the change **each time** the time passes |

Times are a number and a unit, combinable: `s`, `m`, `h`, `d`, `w`, `mo`, `y` (`30m`, `24h`,
`1d12h`), or a unit another plugin registered with SkysongItems. They count from the craft.
Values and placeholders are read at craft time, also for a change that happens later.

What each combination does, and which SkysongItems can't do:

| Step / operation | `duration` | `after` / `every` |
|---|---|---|
| `trait` `set` | trait removed when the time is up ¹ | trait added |
| `trait` `remove` | trait added back when the time is up ² | trait removed |
| `tag` `set` | tag **removed** when the time is up (not restored to an earlier value) | tag set to the value |
| `tag` `add` | the same amount taken back off; these stack | amount added (each time, for `every`) |
| `tag` `remove` | ✗ refused | tag removed |
| `charges` / `hp` `add` | the same amount taken back off; these stack | amount added (each time, for `every`) |
| `charges` / `hp` `set`, `set_max` | ✗ refused | value set |
| `charges` / `hp` `remove` | ✗ refused | charges / HP taken off |
| `transform` | ✗ refused | item transforms |

¹ If the item already has the trait **for good**, it keeps it for good: a craft never shortens a
trait the item owns. If it has it for a while, its time starts again from the craft.
² If the item didn't have the trait, nothing happens and nothing comes back later.

Like every SkysongItems timer, these run lazily: the change happens the next time a player deals
with the item after the time has passed. Saved items that already carry timers (`/si saveitem`
keeps them) bring them along when a craft creates them, nothing to configure.

A key or time that doesn't fit is refused when the file loads, naming the step. A unit
SkysongItems doesn't know refuses the craft (nothing is consumed).

```yaml
# Honing a blade: the marked sword gets "honed" for one day, and +2 to its sharpness tag
# that wears off after the same day.
steps:
  - type: fetch
    fetch_type: marked
    fetch_id: "0"
  - type: trait
    operation: set
    trait_id: honed
    duration: 24h
  - type: tag
    operation: add
    tag_id: sharpness
    values: ["2"]
    duration: 24h
  - type: save
```

```yaml
# A lantern that refills one charge every day, and ripens into its next form after a week.
steps:
  - type: fetch
    fetch_type: generated
    fetch_id: lanterns/oil_lantern
  - type: charges
    operation: add
    values: ["1"]
    every: 1d
  - type: transform
    after: 7d
  - type: save
```

The same works on a fresh item (`fetch_type: generated`), so a plain "make an item" recipe can
hand out an item with timers by using a composite output instead of a basic one.

## Placeholders

Any `values` entry or `index` may be a placeholder. A placeholder contains a `;` and reads
`reference;data`:

| Part | Options |
|---|---|
| reference | `marked:N` — the N-th marked item · `pivot` — the item being edited |
| data | `tag:TAG_ID` · `material` (e.g. SHIELD) · `amount` (stack size) · `cmd:float:I` · `cmd:string:I` · `cmd:boolean:I` · `cmd:color:I` (RGB integer) · `cmd:floats` / `cmd:strings` / `cmd:booleans` / `cmd:colors` (count) · `uuid` (random id) |

Examples: `marked:1;tag:dye_color`, `pivot;cmd:colors` (the next free colour slot), `pivot;uuid`.

## What happens when something is wrong

- **In the file** (unknown step type, missing key, bad enum): a warning naming the file and
  step number is logged at startup / `/sc reload`, and the whole output is skipped. Recipes
  that use it are not registered.
- **During a craft** (missing tag on an ingredient, index out of range, placeholder that
  cannot be resolved): the craft is refused, the player sees `Could not craft: Step 2 (cmd): …`,
  the same text is logged, and **nothing is consumed**. Materials are only taken after every
  step has succeeded and every result item has been generated.

## Limits

- The saved item is rebuilt by SkysongItems from the data it stores. Only SkysongItems data
  survives (name, lore, model, traits, registered tags, custom model data).
- Steps edit the pivot through SkysongItems' detached API: no lore redraw and no change events
  while the template runs; `trait` does not set a printable trait's value tags.
- `save` rebuilds the item from its SkysongItems template, which keeps each timer's **full**
  time rather than the time it had left. So crafting on an item that already carries a timer
  (for example a trait it got for 24h, 20h ago) starts that timer again from the craft. This is
  how SkysongItems' rebuild works; changing it would need a SkysongItems change.
- Player heads and potions cannot be saved by composite outputs yet.
- Tags used in `tag`/`blend` steps and placeholders must exist in SkysongItems (its `config.yml`,
  built-in, or registered by a plugin). The `version` tag can't be written.
