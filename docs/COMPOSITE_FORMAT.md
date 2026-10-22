# Composite outputs

A composite output is a **template of instructions**. Instead of handing out a fixed item, the
recipe runs the steps in the template top to bottom and produces whatever they build. The same
engine covers "edit one of the ingredients", "make a new item using data from the ingredients",
and anything in between, so new crafting behaviours are written in YAML, not Java.

Files live in `plugins/SkysongCrafting/output/composite/`, with `.yml` or `.yaml` extension.
A recipe points at one with `output: "composite:<file name without extension>"`.

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

`output_items` is still accepted for the icon lore but is optional for composites.

## Steps

### `fetch` — choose the pivot
| Key | Values |
|---|---|
| `fetch_type` | `marked` (edit a copy of a marked input) or `generated` (start from a SkysongItem file) |
| `fetch_id` | marked index (`"0"`) or item file name without extension |

### `tag` — edit a SkysongItems tag on the pivot
| Key | Values |
|---|---|
| `operation` | `set`, `add` (number tags only), `remove` |
| `tag_id` | a tag registered in SkysongItems' `config.yml` |
| `values` | list; number tags **sum** every entry, other tags take the first. Not needed for `remove`. |

### `property` — add or remove a SkysongItems property
| Key | Values |
|---|---|
| `operation` | `set`, `remove` |
| `property_id` | a property registered in SkysongItems |

Setting a property the item already has, or removing one it lacks, does nothing.

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

## Placeholders

Any `values` entry or `index` may be a placeholder. A placeholder contains a `;` and reads
`reference;data`:

| Part | Options |
|---|---|
| reference | `marked:N` — the N-th marked item · `pivot` — the item being edited |
| data | `tag:TAG_ID` · `cmd:float:I` · `cmd:string:I` · `cmd:boolean:I` · `cmd:color:I` (RGB integer) · `cmd:floats` / `cmd:strings` / `cmd:booleans` / `cmd:colors` (count) · `uuid` (random id) |

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

- The saved item is stored as SkysongItems data and regenerated when placed. Only SkysongItems
  data survives (name, lore, model, properties, registered tags, custom model data).
- Player heads and potions cannot be saved by composite outputs yet.
- Tags used in `tag`/`blend` steps and placeholders must exist in SkysongItems' `config.yml`.
